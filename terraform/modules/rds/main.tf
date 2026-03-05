# -----------------------------------------------
# RDS Subnet Group
# -----------------------------------------------
resource "aws_db_subnet_group" "main" {
  name       = "financeflow-${var.environment}"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "financeflow-${var.environment}-db-subnet-group"
  }
}

# -----------------------------------------------
# RDS Parameter Group
# -----------------------------------------------
resource "aws_db_parameter_group" "postgres" {
  name   = "financeflow-${var.environment}-pg15"
  family = "postgres15"

  parameter {
    name  = "log_connections"
    value = "1"
  }

  parameter {
    name  = "log_disconnections"
    value = "1"
  }

  parameter {
    name  = "log_duration"
    value = "1"
  }

  parameter {
    name         = "max_connections"
    value        = "100"
    apply_method = "pending-reboot"
  }

  tags = {
    Name = "financeflow-${var.environment}-pg-params"
  }
}

# -----------------------------------------------
# RDS Security Group
# -----------------------------------------------
resource "aws_security_group" "rds" {
  name_prefix = "financeflow-${var.environment}-rds-"
  description = "Security group for RDS PostgreSQL instance"
  vpc_id      = var.vpc_id

  ingress {
    description     = "PostgreSQL access from ECS"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [var.ecs_security_group]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "financeflow-${var.environment}-rds-sg"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# -----------------------------------------------
# RDS Instance
# -----------------------------------------------
resource "aws_db_instance" "main" {
  identifier = "financeflow-${var.environment}"

  engine               = "postgres"
  engine_version       = "15.4"
  instance_class       = var.db_instance_class
  allocated_storage    = 20
  max_allocated_storage = 100
  storage_type         = "gp3"
  storage_encrypted    = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  parameter_group_name   = aws_db_parameter_group.postgres.name

  multi_az            = var.environment == "prod" ? true : false
  publicly_accessible = false

  backup_retention_period = var.environment == "prod" ? 7 : 1
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-sun:05:00"

  deletion_protection = var.environment == "prod" ? true : false
  skip_final_snapshot = var.environment == "prod" ? false : true
  final_snapshot_identifier = var.environment == "prod" ? "financeflow-${var.environment}-final" : null

  performance_insights_enabled = var.environment == "prod" ? true : false

  tags = {
    Name = "financeflow-${var.environment}-postgres"
  }
}
