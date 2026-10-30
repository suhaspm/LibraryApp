# syntax=docker/dockerfile:1.7
# CI runs `npm ci && npm run build` first, then: docker build services/web-ui
# Same pattern as the Java services: build outside, package only the output.
FROM nginxinc/nginx-unprivileged:1.30-alpine

ENV NGINX_ENVSUBST_OUTPUT_DIR=/tmp \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1 \
    CATALOG_URL=http://catalog-service:8080

COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY dist/ /usr/share/nginx/html/

# Fixed non-root UID, the same one the Helm chart enforces for every service.
USER 10001:10001
EXPOSE 8080 8081
