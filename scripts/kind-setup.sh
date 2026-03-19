#!/bin/bash

# Kind Cluster Setup Script - Minimal
# focus strictly on Kind cluster creation and public IP detection

CLUSTER_NAME="bankapp-kind-cluster"

echo "Creating Kind Cluster: $CLUSTER_NAME..."
# Create kind cluster with 80 and 443 ports forwarded for the Gateway
cat <<EOF | kind create cluster --name $CLUSTER_NAME --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  image: kindest/node:v1.35.0
  extraPortMappings:
  - containerPort: 30080
    hostPort: 80
    protocol: TCP
  - containerPort: 30443
    hostPort: 443
    protocol: TCP
EOF

# Detect Public IP for advice
PUBLIC_IP=$(curl -s ifconfig.me)

echo "Kind Setup Complete!"
echo "--------------------------------------------------"
echo "Public IP: $PUBLIC_IP"
echo "Access points (via your Public IP):"
echo "BankApp: http://$PUBLIC_IP.nip.io/"
echo "Nginx Demo: http://$PUBLIC_IP.nip.io/nginx"
echo "ArgoCD UI: http://$PUBLIC_IP:8081 (requires port-forward as per README)"
echo "--------------------------------------------------"
echo "NEXT STEPS (refer to README.md):"
echo "1. Update charts/bankapp/values.yaml with your nip.io host"
echo "2. Install Gateway API CRDs"
echo "3. Install Envoy Gateway via Helm"
echo "4. Deploy the application"
echo "--------------------------------------------------"