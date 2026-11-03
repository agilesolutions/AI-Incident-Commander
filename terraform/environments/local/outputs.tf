output "postgresql_host" {
  value = module.postgresql.connection_host
}

output "postgresql_port" {
  value = module.postgresql.port
}

output "mongodb_host" {
  value = module.mongodb.connection_host
}

output "mongodb_port" {
  value = module.mongodb.port
}

output "mongodb_database" {
  value = module.mongodb.database
}

output "qdrant_host" {
  value = module.qdrant.internal_http_url
}

output "qdrant_port" {
  value = module.qdrant.http_port
}
