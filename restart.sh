#!/bin/bash

# Mogako Docker Restart Script
# This script stops, rebuilds, and restarts all Docker services

set -e  # Exit on any error

echo "🚀 Mogako Docker Restart Script"
echo "================================"

# Function to print colored output
print_step() {
    echo -e "\n\033[1;34m🔄 $1\033[0m"
}

print_success() {
    echo -e "\033[1;32m✅ $1\033[0m"
}

print_error() {
    echo -e "\033[1;31m❌ $1\033[0m"
}

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "docker-compose is not installed or not in PATH"
    exit 1
fi

# Step 1: Stop and remove containers
print_step "Stopping and removing containers..."
docker-compose down

# Step 2: Remove old images (optional, uncomment if needed)
# print_step "Removing old images..."
# docker-compose down --rmi all

# Step 3: Build new images
print_step "Building new images..."
docker-compose build --no-cache

# Step 4: Start services
print_step "Starting services..."
docker-compose up -d

# Step 5: Wait for services to start
print_step "Waiting for services to start..."
sleep 5

# Step 6: Check service status
print_step "Checking service status..."
docker-compose ps

# Step 7: Show logs
print_step "Showing recent logs..."
docker-compose logs --tail=10

print_success "Restart completed!"
echo ""
echo "📋 Available services:"
echo "   • PostgreSQL: localhost:5432"
echo "   • Auth API: http://localhost:8080"
echo "   • Auth API Docs: http://localhost:8080/docs"
echo "   • Calendar API: http://localhost:8081"
echo "   • Calendar API Docs: http://localhost:8081/docs"
echo ""
echo "📝 Useful commands:"
echo "   • View logs: docker-compose logs -f [service-name]"
echo "   • Stop services: docker-compose down"
echo "   • Check status: docker-compose ps"