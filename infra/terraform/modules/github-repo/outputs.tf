output "html_url" {
  value = github_repository.this.html_url
}

output "gitops_deploy_key_fingerprint" {
  description = "Compare with the fingerprint GitHub shows under Settings > Deploy keys."
  value       = tls_private_key.gitops.public_key_fingerprint_sha256
}
