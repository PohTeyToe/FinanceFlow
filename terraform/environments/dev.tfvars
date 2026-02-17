environment = "dev"
aws_region  = "us-east-1"

# VPC
vpc_cidr           = "10.0.0.0/16"
availability_zones = ["us-east-1a", "us-east-1b"]

# Database (provide actual values via CLI or environment variables)
db_name           = "financeflow"
db_username       = "financeflow_admin"
db_instance_class = "db.t3.micro"

# Services
services = {
  auth-service = {
    cpu           = 256
    memory        = 512
    desired_count = 1
    port          = 8081
  }
  account-service = {
    cpu           = 256
    memory        = 512
    desired_count = 1
    port          = 8082
  }
  transaction-service = {
    cpu           = 256
    memory        = 512
    desired_count = 1
    port          = 8083
  }
  analytics-service = {
    cpu           = 256
    memory        = 512
    desired_count = 1
    port          = 8084
  }
  api-gateway = {
    cpu           = 256
    memory        = 512
    desired_count = 1
    port          = 8080
  }
  frontend = {
    cpu           = 256
    memory        = 512
    desired_count = 1
    port          = 80
  }
}
