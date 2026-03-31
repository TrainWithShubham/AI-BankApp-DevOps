output "public_ip" {
  value = aws_instance.k8s_server.public_ip
}
