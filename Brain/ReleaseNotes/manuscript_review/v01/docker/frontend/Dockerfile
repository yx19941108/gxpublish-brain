FROM nginx:1.23.4

LABEL maintainer="Codex"

COPY config/nginx/nginx.conf /etc/nginx/nginx.conf
COPY artifacts/frontend/dist/ /usr/share/nginx/html/
