#!/bin/bash
# Argo Workflows Setup Script for Staging/Production
# Run this script to set up the full Argo CI pipeline

set -e

echo "=== Step 1: Create namespace ==="
kubectl apply -f namespace.yaml

echo ""
echo "=== Step 2: Install Argo Workflows ==="
kubectl apply -n argo -f https://github.com/argoproj/argo-workflows/releases/download/v3.5.5/install.yaml
kubectl wait --for=condition=established crd/workflows.argoproj.io --timeout=60s
kubectl wait --for=condition=available deployment/workflow-controller -n argo --timeout=120s

echo ""
echo "=== Step 3: Install Argo Events ==="
kubectl create namespace argo-events --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -n argo-events -f https://raw.githubusercontent.com/argoproj/argo-events/stable/manifests/install.yaml

echo ""
echo "=== Step 4: Apply RBAC ==="
kubectl apply -f rbac.yaml

echo ""
echo "=== Step 5: Create Docker Hub credentials ==="
echo "NOTE: You must create the dockerhub-credentials secret manually:"
echo "  kubectl create secret generic dockerhub-credentials -n argo \\"
echo "    --from-literal=config.json='{\"auths\":{\"https://index.docker.io/v1/\":{\"auth\":\"<base64 user:token>\"}}}'"
echo ""
read -p "Have you created the secret? (y/n): " confirm
if [ "$confirm" != "y" ]; then
  echo "Please create the secret and re-run."
  exit 1
fi

echo ""
echo "=== Step 6: Create GitHub access secret ==="
echo "NOTE: You must create the github-access secret manually:"
echo "  kubectl create secret generic github-access -n argo \\"
echo "    --from-literal=token=<github-token> \\"
echo "    --from-literal=secret=<webhook-secret>"
echo ""

echo ""
echo "=== Step 7: Apply Argo Server ==="
kubectl apply -f argo-server.yaml

echo ""
echo "=== Step 8: Apply WorkflowTemplate ==="
kubectl apply -f workflow-template.yaml

echo ""
echo "=== Step 9: Apply Events (EventBus, EventSource, Sensor) ==="
kubectl apply -f events.yaml

echo ""
echo "=== DONE! ==="
echo "Argo UI: kubectl port-forward -n argo svc/argo-server 2746:2746"
echo "Then open: http://localhost:2746"
