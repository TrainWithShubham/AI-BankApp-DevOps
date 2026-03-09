#!/bin/bash
# Quick test script for BankApp

echo "🚀 Starting BankApp with Docker Compose..."
docker compose up -d

echo ""
echo "⏳ Waiting for services to be ready (30 seconds)..."
sleep 30

echo ""
echo "📊 Service Status:"
docker compose ps

echo ""
echo "🔍 Testing health endpoint..."
curl -s http://localhost:8080/actuator/health | grep -q "UP" && echo "✅ App is healthy!" || echo "❌ App health check failed"

echo ""
echo "🌐 Application is running at: http://localhost:8080"
echo ""
echo "📝 To view logs: docker compose logs -f"
echo "🛑 To stop: docker compose down"
