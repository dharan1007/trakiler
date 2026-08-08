// FORM public backend configuration.
// Fill these only after a dedicated FORM Supabase project has been created and secured.
// The browser may contain a Supabase project URL and publishable/anon-compatible key.
// Never place a service-role or other server secret in this browser-delivered file.
window.FORM_SUPABASE = {
  url: "",
  key: ""
};

// FORM v4 is intentionally loaded as an additive module so the core workout logger remains
// independently usable. The stylesheet is safe to load immediately; the decision engine waits
// until the existing workout/progress modules have initialized.
(()=>{
  const css=document.createElement('link');css.rel='stylesheet';css.href='v4.css';document.head.appendChild(css);
  window.addEventListener('load',()=>{
    if(window.FORM_COACH)return;
    const js=document.createElement('script');js.src='coach-v4.js';js.defer=true;document.body.appendChild(js);
  },{once:true});
})();
