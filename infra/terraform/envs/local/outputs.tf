output "kube_context" {
  description = "Use with: kubectl --context <value>"
  value       = module.cluster.context
}

output "app_url" {
  value = "http://localhost  (sign in as member1 or librarian1; passwords: terraform output -raw member_password / librarian_password)"
}

output "member_password" {
  value     = module.secrets.member_password
  sensitive = true
}

output "librarian_password" {
  value     = module.secrets.librarian_password
  sensitive = true
}

output "keycloak_admin_password" {
  description = "Admin console: http://localhost/auth/admin (user: admin)"
  value       = module.secrets.keycloak_admin_password
  sensitive   = true
}

output "services_client_secret" {
  description = "For the client-credentials exercise in docs/labs/03-authn-authz.md"
  value       = module.secrets.services_client_secret
  sensitive   = true
}

output "argocd_ui" {
  description = "How to reach the Argo CD UI."
  value       = "kubectl -n argocd port-forward svc/argocd-server 8088:80, then open http://localhost:8088 (user: admin, password: kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d)"
}
