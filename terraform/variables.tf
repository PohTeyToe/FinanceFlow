variable "environment" {
  description = "Deployment environment (dev, staging, prod)"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod."
  }
}

variable "aws_region" {
  description = "AWS region for resource deployment"
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

# -----------------------------------------------
# Database
# -----------------------------------------------
variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "financeflow"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "PostgreSQL master password"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

# -----------------------------------------------
# Application
# -----------------------------------------------
variable "jwt_secret" {
  description = "JWT signing secret (Base64 encoded, min 256 bits)"
  type        = string
  sensitive   = true
}

variable "domain_name" {
  description = "Domain name for the application (optional)"
  type        = string
  default     = ""
}

variable "certificate_arn" {
  description = "ACM certificate ARN for HTTPS (optional, required if domain_name is set)"
  type        = string
  default     = ""
}

# -----------------------------------------------
# Service configuration
# -----------------------------------------------
variable "services" {
  description = "Configuration for each microservice"
  type = map(object({
    cpu           = number
    memory        = number
    desired_count = number
    port          = number
  }))
  default = {
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
      desired_count = 2
      port          = 8083
    }
    analytics-service = {
      cpu           = 256
      memory        = 512
      desired_count = 1
      port          = 8084
    }
    api-gateway = {
      cpu           = 512
      memory        = 1024
      desired_count = 2
      port          = 8080
    }
    frontend = {
      cpu           = 256
      memory        = 512
      desired_count = 2
      port          = 80
    }
  }
}
