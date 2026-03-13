#!/bin/bash
# automated-ollama-setup.sh
# This script is designed to be used as AWS EC2 User Data for the AI Tier.

# Export HOME for cloud-init environment to prevent Ollama panic
export HOME=/root

# 1. Install Ollama
curl -fsSL https://ollama.com/install.sh | sh

# 2. Wait for Ollama to be available
sleep 10

# 3. Configure Ollama to listen on all interfaces (0.0.0.0)
# Create the systemd drop-in directory
mkdir -p /etc/systemd/system/ollama.service.d

# Write the override configuration
cat <<EOF > /etc/systemd/system/ollama.service.d/override.conf
[Service]
Environment="OLLAMA_HOST=0.0.0.0"
EOF

# 4. Reload and Restart the service
systemctl daemon-reload
systemctl restart ollama

# 6. Wait for Ollama server to be ready (Health Check)
echo "Waiting for Ollama server to start..."
MAX_RETRIES=30
RETRY_COUNT=0
while ! curl -s http://localhost:11434/api/tags > /dev/null; do
    RETRY_COUNT=$((RETRY_COUNT+1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo "Ollama server failed to start in time."
        exit 1
    fi
    sleep 2
done

# 7. Pull the required model
echo "Pulling tinyllama model..."
ollama pull tinyllama

echo "Ollama setup complete and listening on port 11434"
