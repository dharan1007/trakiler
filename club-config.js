// FORM public backend configuration.
// Fill these only after a dedicated FORM Supabase project has been created and secured.
// The browser may contain a Supabase project URL and publishable/anon-compatible key.
// Never place a service-role or other server secret in this browser-delivered file.
window.FORM_SUPABASE = {
  url: "",
  key: ""
};

// Additive public product layers. Keeping these behind the already-loaded config file means the
// core logger still works independently while every screen receives the same readable typography,
// decision-support layer, exercise assistance and research/side-project notice.
(()=>{
  const loadCss=(href)=>{if(document.querySelector(`link[href="${href}"]`))return;const el=document.createElement('link');el.rel='stylesheet';el.href=href;document.head.appendChild(el)};
  const loadScript=(src,key)=>{if((key&&window[key])||document.querySelector(`script[src="${src}"]`))return;const el=document.createElement('script');el.src=src;el.defer=true;document.body.appendChild(el)};
  loadCss('v4.css');
  loadCss('readability.css');
  window.addEventListener('load',()=>{
    loadScript('coach-v4.js','FORM_COACH');
    loadScript('assist.js','FORM_ASSIST');
    loadScript('project-notice.js');
  },{once:true});
})();
