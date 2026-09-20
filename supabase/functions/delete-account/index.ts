import { createClient } from 'npm:@supabase/supabase-js@2.95.0';

const allowedOrigins = new Set([
  'https://dharan1007.github.io',
  'http://localhost:8000',
  'http://127.0.0.1:8000',
]);

function corsHeaders(req: Request): Record<string,string> {
  const origin = req.headers.get('origin') || '';
  const headers: Record<string,string> = {
    'Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type',
    'Access-Control-Allow-Methods':'POST, OPTIONS',
    'Vary':'Origin',
  };
  if (allowedOrigins.has(origin)) headers['Access-Control-Allow-Origin'] = origin;
  return headers;
}

function json(body: unknown, status: number, headers: Record<string,string>) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {...headers, 'Content-Type':'application/json', 'Cache-Control':'no-store'},
  });
}

Deno.serve(async (req: Request) => {
  const headers = corsHeaders(req);
  const origin = req.headers.get('origin') || '';
  if (origin && !allowedOrigins.has(origin)) return json({error:'origin_not_allowed'},403,headers);
  if (req.method === 'OPTIONS') return new Response(null,{status:204,headers});
  if (req.method !== 'POST') return json({error:'method_not_allowed'},405,headers);

  try {
    const auth = req.headers.get('authorization') || '';
    const token = auth.startsWith('Bearer ') ? auth.slice(7) : '';
    if (!token) return json({error:'authentication_required'},401,headers);

    const body = await req.json().catch(()=>({}));
    if (body?.confirm !== 'DELETE FORM ACCOUNT') return json({error:'confirmation_required'},400,headers);

    const url = Deno.env.get('SUPABASE_URL');
    const service = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY');
    if (!url || !service) throw new Error('Server configuration missing');

    const admin = createClient(url,service,{auth:{persistSession:false,autoRefreshToken:false}});
    const {data,error} = await admin.auth.getUser(token);
    if (error || !data.user) return json({error:'invalid_session'},401,headers);

    const deleted = await admin.auth.admin.deleteUser(data.user.id);
    if (deleted.error) throw deleted.error;
    return json({deleted:true},200,headers);
  } catch (error) {
    console.error('FORM delete-account', error instanceof Error ? error.message : 'unknown');
    return json({error:'delete_failed'},500,headers);
  }
});
