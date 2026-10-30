output "keycloak_admin_password" {
  value     = random_password.keycloak_admin.result
  sensitive = true
}

output "services_client_secret" {
  value     = random_password.services_client.result
  sensitive = true
}

output "member_password" {
  value     = random_password.member.result
  sensitive = true
}

output "librarian_password" {
  value     = random_password.librarian.result
  sensitive = true
}
