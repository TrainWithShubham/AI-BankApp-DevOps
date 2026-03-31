provider "aws" {
  region = "ap-south-1"
}

resource "aws_security_group" "k8s_sg" {
  name = "k8s-sg"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 6443
    to_port     = 6443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 30000
    to_port     = 32767
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "k8s_server" {
  ami           = "ami-0f58b397bc5c1f2e8"
  instance_type = "t2.medium"
  key_name      = var.key_name

  vpc_security_group_ids = [aws_security_group.k8s_sg.id]

  user_data = <<-EOF
              #!/bin/bash
              apt update -y
              apt install -y docker.io curl
              systemctl start docker
              systemctl enable docker

              # Install K3s
              curl -sfL https://get.k3s.io | sh -

              # Wait for cluster
              sleep 60
              EOF
}
