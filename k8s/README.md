# Kubernetes Deployment (Step-by-Step)

This guide deploys the app stack (`bankapp` + `mysql` + `ollama`) into a local Kind cluster.

## 0) Prerequisites

- Docker running
- `kubectl` installed
- `kind` installed

## 1) Build application image

```bash
docker build -t santoshpathak7456/ai-bankapp-devops_bankapp:k8s .
```

## 2) Create Kind cluster

```bash
kind create cluster --config setup-k8s/kind-config.yml
```

## 3) Load local image into Kind

```bash
kind load docker-image santoshpathak7456/ai-bankapp-devops_bankapp:k8s --name tws-cluster
```

## 4) Install metrics-server (required for HPA)

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl patch deployment metrics-server -n kube-system --type='json' -p='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
kubectl rollout status deployment/metrics-server -n kube-system --timeout=120s
```

## 5) Apply manifests in order

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configMap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/persistentVolume.yaml
kubectl apply -f k8s/pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/ollama-deployment.yaml
kubectl apply -f k8s/bankapp-deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

## 6) Verify deployment

```bash
kubectl get all -n bankapp
kubectl get pvc,pv -n bankapp
kubectl get hpa -n bankapp
kubectl top pods -n bankapp
```

## 7) Access application

Kind config maps node port `30080` to host `8080`.

- Open: `http://localhost:8080`

## 8) Test autoscaling

```bash
kubectl run load-test --image=busybox:1.36 -n bankapp -- sh -c "while true; do wget -q -O- http://bankapp-service:8080/actuator/health >/dev/null 2>&1; done"
kubectl get hpa -n bankapp -w
```

Cleanup load pod:

```bash
kubectl delete pod load-test -n bankapp
```
