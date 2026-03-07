# FinanceFlow Kubernetes Deployment

Deploy FinanceFlow to a Kubernetes cluster using these manifests.

## Prerequisites

- [kubectl](https://kubernetes.io/docs/tasks/tools/) configured with cluster access
- [minikube](https://minikube.sigs.k8s.io/docs/start/) or [kind](https://kind.sigs.k8s.io/) for local development
- Docker images built for all services

## Build Docker Images (Local)

```bash
# From the project root
cd backend
docker build -f auth-service/Dockerfile -t financeflow/auth-service:latest .
docker build -f account-service/Dockerfile -t financeflow/account-service:latest .
docker build -f transaction-service/Dockerfile -t financeflow/transaction-service:latest .
docker build -f analytics-service/Dockerfile -t financeflow/analytics-service:latest .
docker build -f api-gateway/Dockerfile -t financeflow/api-gateway:latest .
cd ../frontend
docker build -t financeflow/frontend:latest .
```

For minikube, load images into the cluster:

```bash
minikube image load financeflow/auth-service:latest
minikube image load financeflow/account-service:latest
minikube image load financeflow/transaction-service:latest
minikube image load financeflow/analytics-service:latest
minikube image load financeflow/api-gateway:latest
minikube image load financeflow/frontend:latest
```

## Deploy

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Deploy config and secrets (update secret.yaml with real values first)
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy PostgreSQL
kubectl apply -f k8s/postgres/

# Wait for PostgreSQL to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n financeflow --timeout=120s

# Deploy backend services
kubectl apply -f k8s/auth-service/
kubectl apply -f k8s/account-service/
kubectl apply -f k8s/transaction-service/
kubectl apply -f k8s/analytics-service/
kubectl apply -f k8s/api-gateway/

# Deploy frontend
kubectl apply -f k8s/frontend/

# Deploy ingress and autoscaler
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

## Accessing the Application

### With minikube

```bash
# Get the API Gateway URL
minikube service api-gateway -n financeflow --url

# Or use port forwarding
kubectl port-forward svc/api-gateway 8080:8080 -n financeflow
kubectl port-forward svc/frontend 3000:80 -n financeflow
```

### With Ingress (requires ingress controller)

```bash
# Install nginx ingress controller on minikube
minikube addons enable ingress

# Access at http://localhost (or minikube IP)
minikube ip
```

## Verify Deployment

```bash
# Check all pods
kubectl get pods -n financeflow

# Check services
kubectl get svc -n financeflow

# Check HPA status
kubectl get hpa -n financeflow

# View logs for a service
kubectl logs -f deployment/auth-service -n financeflow
```

## Scaling

```bash
# Manual scaling
kubectl scale deployment transaction-service --replicas=3 -n financeflow

# The HPA automatically scales transaction-service between 2-5 replicas
# based on CPU utilization (target: 70%)
kubectl describe hpa transaction-service-hpa -n financeflow
```

## Cleanup

```bash
kubectl delete namespace financeflow
```
