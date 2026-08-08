FROM nginx:1.29-alpine

LABEL org.opencontainers.image.title="FORM — Training System"
LABEL org.opencontainers.image.description="Public web distribution of FORM, an independent research/side-project workout tracking and training analytics system."
LABEL org.opencontainers.image.source="https://github.com/dharan1007/trakiler"
LABEL org.opencontainers.image.url="https://dharan1007.github.io/trakiler/"
LABEL org.opencontainers.image.documentation="https://github.com/dharan1007/trakiler#readme"
LABEL org.opencontainers.image.licenses="LicenseRef-Proprietary"
LABEL org.opencontainers.image.vendor="Dharan"

COPY index.html /usr/share/nginx/html/index.html
COPY privacy.html /usr/share/nginx/html/privacy.html
COPY terms.html /usr/share/nginx/html/terms.html
COPY .nojekyll /usr/share/nginx/html/.nojekyll
COPY styles.css /usr/share/nginx/html/styles.css
COPY v3.css /usr/share/nginx/html/v3.css
COPY v4.css /usr/share/nginx/html/v4.css
COPY readability.css /usr/share/nginx/html/readability.css
COPY data.js /usr/share/nginx/html/data.js
COPY programs-v3.js /usr/share/nginx/html/programs-v3.js
COPY club-config.js /usr/share/nginx/html/club-config.js
COPY backend.js /usr/share/nginx/html/backend.js
COPY club.js /usr/share/nginx/html/club.js
COPY app.js /usr/share/nginx/html/app.js
COPY insights.js /usr/share/nginx/html/insights.js
COPY coach-v4.js /usr/share/nginx/html/coach-v4.js
COPY assist.js /usr/share/nginx/html/assist.js
COPY project-notice.js /usr/share/nginx/html/project-notice.js

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
