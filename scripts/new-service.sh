#!/usr/bin/env bash
# Golden path: create a new Spring Boot service wired into build, CI and GitOps.
#   scripts/new-service.sh rental-service
set -euo pipefail

name="${1:?usage: $0 <name>-service}"
[[ "$name" =~ ^[a-z][a-z0-9]*(-[a-z0-9]+)*-service$ ]] || { echo "name must be kebab-case and end in -service" >&2; exit 1; }

root="$(cd "$(dirname "$0")/.." && pwd)"
target="$root/services/$name"
[[ -e "$target" ]] && { echo "$target already exists" >&2; exit 1; }

base="${name%-service}"                                        # rental-service -> rental
package="${base//-/}"                                          # partner-feed -> partnerfeed
class="$(echo "$base" | sed -E 's/(^|-)([a-z])/\U\2/g')"        # partner-feed -> PartnerFeed

cp -r "$root/templates/spring-service" "$target"
mv "$target/values.yaml" "$root/deploy/gitops/apps/local/values/$name.yaml"

# Rename placeholder directories and files, deepest first.
find "$target" -depth -name '*__*' | while read -r path; do
  dir="$(dirname "$path")"; file="$(basename "$path")"
  file="${file//__PACKAGE__/$package}"; file="${file//__CLASS__/$class}"
  mv "$path" "$dir/$file"
done
grep -rl '__' "$target" | xargs sed -i -e "s/__SERVICE__/$name/g" -e "s/__PACKAGE__/$package/g" -e "s/__CLASS__/$class/g"

printf '\n  - name: %s\n    kind: service\n' "$name" >> "$root/deploy/gitops/apps/local/apps.yaml"

cat <<MSG
Created $name:
  services/$name/                                   code, Dockerfile (auto-included by settings.gradle.kts)
  deploy/gitops/apps/local/values/$name.yaml        deployment values
  deploy/gitops/apps/local/apps.yaml                Argo CD application entry (appended)
Next: ./gradlew :services:$name:build, then open a PR. CI detects the new service automatically.
MSG
