#!/bin/bash

echo "Testing Bank AI App..."

response=$(curl -s http://localhost:8080/actuator/health)

if [[ "$response" == *"UP"* ]]; then
  echo "✅ Test Passed"
  exit 0
else
  echo "❌ Test Failed"
  exit 1
fi
