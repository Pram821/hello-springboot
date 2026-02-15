#!/bin/bash
# Auto-restart port-forwards when they die

forward() {
  local name=$1 ns=$2 svc=$3 ports=$4
  while true; do
    echo "[$(date '+%H:%M:%S')] Starting $name on $ports..."
    kubectl port-forward svc/$svc -n $ns $ports 2>/dev/null
    echo "[$(date '+%H:%M:%S')] $name died, restarting in 2s..."
    sleep 2
  done
}

forward "App"   default      hello-springboot 8800:8080 &
forward "Argo"  argo         argo-server      2746:2746 &
forward "Kiali" istio-system kiali            20001:20001 &

echo ""
echo "✅ Port-forwards running:"
echo "   App:   http://localhost:8800"
echo "   Argo:  http://localhost:2746"
echo "   Kiali: http://localhost:20001"
echo ""
echo "Press Ctrl+C to stop all"
wait
