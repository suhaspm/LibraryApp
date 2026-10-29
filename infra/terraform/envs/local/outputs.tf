output "kube_context" {
  description = "Use with: kubectl --context <value>"
  value       = module.cluster.context
}

output "argocd_ui" {
  description = "How to reach the Argo CD UI until the gateway exists (week 3)."
  value       = "kubectl -n argocd port-forward svc/argocd-server 8088:80, then open http://localhost:8088 (user: admin, password: kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d)"
}
