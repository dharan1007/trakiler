(()=>{
  'use strict';
  const CONTACT='dharan.poduvu@gmail.com';
  const footer=document.querySelector('.footer');
  if(footer){
    footer.innerHTML=`<strong>FORM is an independent research and side project.</strong> It is a free training log and educational decision-support experiment, not medical advice, a medical device, a certified coaching service, or a guarantee of results. Calculations, rankings, estimates and suggestions are context-dependent and should not be used as a substitute for qualified medical, physiotherapy, dietetic or coaching judgment. No advertising or sale of user data is built into FORM. <a href="privacy.html">Privacy</a> · <a href="terms.html">Terms</a> · <a href="mailto:${CONTACT}">Contact</a>`;
  }
  const description='FORM is an independent research/side-project workout tracker with editable training programs, private-first progression analytics, exercise intelligence and optional decision support. Educational use only; no outcome guarantees.';
  const desc=document.querySelector('meta[name="description"]');
  if(desc)desc.setAttribute('content',description);
  document.documentElement.dataset.projectType='research-side-project';
})();
