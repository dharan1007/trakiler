-- Atomic workout + set synchronization.
-- Apply after supabase/schema.sql. RLS remains authoritative because this function is SECURITY INVOKER.

create or replace function public.form_sync_workout(
  p_workout jsonb,
  p_sets jsonb default '[]'::jsonb
) returns uuid
language plpgsql
security invoker
set search_path = ''
as $$
declare
  v_uid uuid := (select auth.uid());
  v_workout_id uuid;
begin
  if v_uid is null then
    raise exception 'authentication required';
  end if;

  if jsonb_typeof(p_workout) <> 'object' then
    raise exception 'p_workout must be an object';
  end if;
  if jsonb_typeof(coalesce(p_sets, '[]'::jsonb)) <> 'array' then
    raise exception 'p_sets must be an array';
  end if;
  if coalesce(nullif(p_workout->>'client_id',''),'') = '' then
    raise exception 'client_id is required';
  end if;
  if jsonb_array_length(coalesce(p_sets, '[]'::jsonb)) > 100 then
    raise exception 'too many sets';
  end if;

  insert into public.form_workouts(
    user_id,
    client_id,
    program_name,
    day_name,
    ended_at,
    elapsed_sec,
    completed_sets,
    total_sets,
    total_volume_kg,
    notes,
    updated_at
  )
  values(
    v_uid,
    left(p_workout->>'client_id',120),
    left(coalesce(p_workout->>'program_name',''),200),
    left(coalesce(p_workout->>'day_name',''),200),
    coalesce((p_workout->>'ended_at')::timestamptz, now()),
    greatest(0, least(coalesce((p_workout->>'elapsed_sec')::integer,0),86400)),
    greatest(0, least(coalesce((p_workout->>'completed_sets')::integer,0),100)),
    greatest(0, least(coalesce((p_workout->>'total_sets')::integer,0),100)),
    greatest(0, least(coalesce((p_workout->>'total_volume_kg')::numeric,0),5000000)),
    left(coalesce(p_workout->>'notes',''),4000),
    now()
  )
  on conflict(user_id,client_id)
  do update set
    program_name = excluded.program_name,
    day_name = excluded.day_name,
    ended_at = excluded.ended_at,
    elapsed_sec = excluded.elapsed_sec,
    completed_sets = excluded.completed_sets,
    total_sets = excluded.total_sets,
    total_volume_kg = excluded.total_volume_kg,
    notes = excluded.notes,
    updated_at = now()
  returning id into v_workout_id;

  delete from public.form_workout_sets
  where workout_id = v_workout_id and user_id = v_uid;

  insert into public.form_workout_sets(
    workout_id,
    user_id,
    set_index,
    exercise_id,
    exercise_name,
    weight_kg,
    reps,
    rir
  )
  select
    v_workout_id,
    v_uid,
    greatest(1, least(coalesce((item->>'set_index')::integer, ordinality::integer),100)),
    left(coalesce(item->>'exercise_id',''),100),
    left(coalesce(item->>'exercise_name',''),100),
    greatest(0, least(coalesce((item->>'weight_kg')::numeric,0),2000)),
    greatest(0, least(coalesce((item->>'reps')::integer,0),500)),
    greatest(0, least(coalesce((item->>'rir')::numeric,0),10))
  from jsonb_array_elements(coalesce(p_sets,'[]'::jsonb)) with ordinality as s(item, ordinality);

  return v_workout_id;
end;
$$;

revoke all on function public.form_sync_workout(jsonb,jsonb) from public, anon;
grant execute on function public.form_sync_workout(jsonb,jsonb) to authenticated;
