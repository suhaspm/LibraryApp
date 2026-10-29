output "repository" {
  value = module.repo.html_url
}

output "gitops_deploy_key_fingerprint" {
  value = module.repo.gitops_deploy_key_fingerprint
}
