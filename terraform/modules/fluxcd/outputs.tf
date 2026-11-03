output "git_repository" {
  value = var.github_repository
}

output "private_key_pem" {
  value = tls_private_key.flux.private_key_pem
}