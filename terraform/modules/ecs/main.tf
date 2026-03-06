# -----------------------------------------------
# ECS Cluster
# -----------------------------------------------
resource "aws_ecs_cluster" "main" {
  name = "financeflow-${var.environment}"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "financeflow-${var.environment}-cluster"
  }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name = aws_ecs_cluster.main.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    base              = 1
    weight            = 100
    capacity_provider = "FARGATE"
  }
}

# -----------------------------------------------
# ECR Repositories
# -----------------------------------------------
resource "aws_ecr_repository" "services" {
  for_each = var.services

  name                 = "financeflow/${each.key}"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name    = "financeflow-${each.key}"
    Service = each.key
  }
}

resource "aws_ecr_lifecycle_policy" "services" {
  for_each = aws_ecr_repository.services

  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# -----------------------------------------------
# IAM Roles
# -----------------------------------------------
resource "aws_iam_role" "ecs_task_execution" {
  name = "financeflow-${var.environment}-ecs-execution"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_task_execution_ecr" {
  name = "ecr-pull-policy"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role" "ecs_task" {
  name = "financeflow-${var.environment}-ecs-task"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

# -----------------------------------------------
# CloudWatch Log Groups
# -----------------------------------------------
resource "aws_cloudwatch_log_group" "services" {
  for_each = var.services

  name              = "/ecs/financeflow-${var.environment}/${each.key}"
  retention_in_days = 30

  tags = {
    Service = each.key
  }
}

# -----------------------------------------------
# ECS Task Definitions
# -----------------------------------------------
resource "aws_ecs_task_definition" "services" {
  for_each = var.services

  family                   = "financeflow-${var.environment}-${each.key}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = "${aws_ecr_repository.services[each.key].repository_url}:latest"
      essential = true

      portMappings = [
        {
          containerPort = each.value.port
          protocol      = "tcp"
        }
      ]

      environment = each.key == "frontend" ? [
        {
          name  = "REACT_APP_API_URL"
          value = "http://localhost:8080"
        }
      ] : [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "docker"
        },
        {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql://${var.db_endpoint}:5432/${var.db_name}"
        },
        {
          name  = "SPRING_DATASOURCE_USERNAME"
          value = var.db_username
        },
        {
          name  = "SPRING_DATASOURCE_PASSWORD"
          value = var.db_password
        },
        {
          name  = "JWT_SECRET"
          value = var.jwt_secret
        },
        {
          name  = "AUTH_SERVICE_URL"
          value = "http://auth-service.financeflow.local:8081"
        },
        {
          name  = "ACCOUNT_SERVICE_URL"
          value = "http://account-service.financeflow.local:8082"
        },
        {
          name  = "TRANSACTION_SERVICE_URL"
          value = "http://transaction-service.financeflow.local:8083"
        },
        {
          name  = "ANALYTICS_SERVICE_URL"
          value = "http://analytics-service.financeflow.local:8084"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services[each.key].name
          "awslogs-region"        = data.aws_region.current.name
          "awslogs-stream-prefix" = "ecs"
        }
      }

      healthCheck = each.key == "frontend" ? {
        command     = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 30
      } : {
        command     = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:${each.value.port}/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = {
    Service = each.key
  }
}

data "aws_region" "current" {}

# -----------------------------------------------
# ECS Services
# -----------------------------------------------
resource "aws_ecs_service" "api_gateway" {
  name            = "financeflow-${var.environment}-api-gateway"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.services["api-gateway"].arn
  desired_count   = var.services["api-gateway"].desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.alb_target_group
    container_name   = "api-gateway"
    container_port   = 8080
  }

  depends_on = [aws_ecs_task_definition.services]
}

resource "aws_ecs_service" "backend_services" {
  for_each = {
    for k, v in var.services : k => v
    if k != "api-gateway" && k != "frontend"
  }

  name            = "financeflow-${var.environment}-${each.key}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.services[each.key].arn
  desired_count   = each.value.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services[each.key].arn
  }

  depends_on = [aws_ecs_task_definition.services]
}

# -----------------------------------------------
# Service Discovery
# -----------------------------------------------
resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "financeflow.local"
  vpc  = var.vpc_id

  tags = {
    Name = "financeflow-${var.environment}-namespace"
  }
}

resource "aws_service_discovery_service" "services" {
  for_each = {
    for k, v in var.services : k => v
    if k != "api-gateway" && k != "frontend"
  }

  name = each.key

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

# -----------------------------------------------
# Security Group for ECS Tasks
# -----------------------------------------------
resource "aws_security_group" "ecs_tasks" {
  name_prefix = "financeflow-${var.environment}-ecs-tasks-"
  description = "Security group for ECS Fargate tasks"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Traffic from ALB"
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [var.alb_security_group]
  }

  ingress {
    description = "Inter-service communication"
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    self        = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "financeflow-${var.environment}-ecs-tasks-sg"
  }

  lifecycle {
    create_before_destroy = true
  }
}
