# syntax=docker/dockerfile:1.7
# CI runs `npm ci && npm run build` first, then: docker build services/web-ui
# Same pattern as the Java services: build outside, package only the output.
FROM nginxinc/nginx-unprivileged:1.30-alpine

# The base image is only rebuilt every few weeks, so its Alpine packages lag behind security
# fixes (the CI scan caught a critical brotli CVE this way). Upgrading at build time picks up
# Alpine's patches; removing curl drops a tool nginx never uses but an attacker would.
USER root
RUN apk upgrade --no-cache && apk del --no-cache curl

ENV NGINX_ENVSUBST_OUTPUT_DIR=/tmp \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1 \
    CATALOG_URL=http://catalog-service:8080

COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY dist/ /usr/share/nginx/html/

# Fixed non-root UID, the same one the Helm chart enforces for every service.
USER 10001:10001
EXPOSE 8080 8081
