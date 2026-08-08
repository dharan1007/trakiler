import { createClient } from 'npm:@supabase/supabase-js@2';

const allowedOrigins = new Set(['https://dharan1007.github.io','http://localhost:8000','http://127.0.0.1:8000']);
function cors(req: Request) {
  const origin = req.headers.get('origin') || '';
  const allowed = allowedOrigins.has(origin) ? origin : 'https://dharan1007.github.io';
  return {'Access-Control-Allow-Origin':allowed,'Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type','Access-Control-Allow-Methods':'POST, OPTIONS','Vary':'Origin'};
}
Deno.serve(async (req: Request) => {
  const headers = cors(req);
  if (req.method === 'OPTIONS') return new Response('ok',{headers});
  if (req.method !== 'POST') return new Response(JSON.stringify({error:'method_not_allowed'}),{status:405,headers:{...headers,'Content-Type':'application/json'}});
  try {
    const auth = req.headers.get('authorization') || '';
    const token = auth.startsWith('Bearer ') ? auth.slice(7) : '';
    if (!token) return new Response(JSON.stringify({error:'authentication_required'}),{status:401,headers:{...headers,'Content-Type':'application/json'}});
    const body = await req.json().catch(()=>({}));
    if (body?.confirm !== 'DELETE FORM ACCOUNT') return new Response(JSON.stringify({error:'confirmation_required'}),{status:400,headers:{...headers,'Content-Type':'application/json'}});
    const url = Deno.env.get('SUPABASE_URL');
    const service = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY');
    if (!url || !service) throw new Error('Server configuration missing');
    const admin = createClient(url,service,{auth:{persistSession:false,autoRefreshToken:false}});
    const {data,error} = await admin.auth.getUser(token);
    if (error || !data.user) return new Response(JSON.stringify({error:'invalid_session'}),{status:401,headers:{...headers,'Content-Type':'application/json'}});
    const deleted = await admin.auth.admin.deleteUser(data.user.id);
    if (deleted.error) throw deleted.error;
    return new Response(JSON.stringify({deleted:true}),{status:200,headers:{...headers,'Content-Type':'application/json','Cache-Control':'no-store'}});
  } catch (error) {
    console.error('FORM delete-account',error);
    return new Response(JSON.stringify({error:'delete_failed'}),{status:500,headers:{...headers,'Content-Type':'application/json','Cache-Control':'no-store'}});
  }
});
