# FinanceFlow Terraform Infrastructure

Infrastructure as Code for deploying FinanceFlow to AWS using ECS Fargate.

## Architecture

```
Internet
    |
    v
[ALB] (public subnets)
    |
    v
[API Gateway - ECS Fargate] (private subnets)
    |
    +---> [Auth Service]
    +---> [Account Service]
    +---> [Transaction Service]
    +---> [Analytics Service]
    |
    v
[RDS PostgreSQL] (private subnets)
```

## Prerequisites

- [AWS CLI v2](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) configured with credentials
- [Terraform >= 1.5](https://developer.hashicorp.com/terraform/downloads)
- An S3 bucket for Terraform state (`financeflow-terraform-state`)
- A DynamoDB table for state locking (`financeflow-terraform-locks`)

### Create state backend (one-time setup)

```bash
aws s3 mb s3://financeflow-terraform-state --region us-east-1

aws dynamodb create-table \
  --table-name financeflow-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1
```

## Usage

```bash
# Initialize Terraform
terraform init

# Plan with dev environment
terraform plan -var-file=environments/dev.tfvars \
  -var="db_password=YOUR_DB_PASSWORD" \
  -var="jwt_secret=YOUR_JWT_SECRET"

# Apply
terraform apply -var-file=environments/dev.tfvars \
  -var="db_password=YOUR_DB_PASSWORD" \
  -var="jwt_secret=YOUR_JWT_SECRET"

# Destroy (dev only)
terraform destroy -var-file=environments/dev.tfvars \
  -var="db_password=YOUR_DB_PASSWORD" \
  -var="jwt_secret=YOUR_JWT_SECRET"
```

## Modules

| Module | Description |
|-|-|
| **vpc** | VPC with 2 public + 2 private subnets, NAT Gateway, security groups |
| **ecs** | ECS Fargate cluster, task definitions, ECR repos, service discovery |
| **rds** | PostgreSQL 15 instance, subnet group, parameter group |
| **alb** | Application Load Balancer, target groups, HTTP/HTTPS listeners |

## Pushing Docker Images

After `terraform apply`, push images to ECR:

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and push each service
for service in auth-service account-service transaction-service analytics-service api-gateway frontend; do
  docker build -t financeflow/$service ./backend/$service/
  docker tag financeflow/$service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/financeflow/$service:latest
  docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/financeflow/$service:latest
done
```

## Estimated Monthly Cost (dev)

| Resource | Estimated Cost |
|-|-|
| ECS Fargate (6 tasks, 0.25 vCPU, 512MB) | ~$45 |
| RDS db.t3.micro (single-AZ) | ~$15 |
| ALB | ~$18 |
| NAT Gateway | ~$33 |
| ECR storage | ~$1 |
| CloudWatch Logs | ~$3 |
| **Total** | **~$115/month** |

## Security Notes

- Database credentials are passed as sensitive variables (never committed)
- RDS is deployed in private subnets, not publicly accessible
- ECS tasks run in private subnets with NAT Gateway for outbound access
- ALB handles TLS termination when a certificate is provided
- All storage is encrypted at rest
