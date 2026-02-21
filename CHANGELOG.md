# Changelog

## [Unreleased] - 2026-03

### Added
- Redis caching layer for analytics queries with TTL-based invalidation
- Swagger/OpenAPI documentation for all service endpoints
- Terraform modules for AWS infrastructure (VPC, ECS, RDS, ALB)
- ECS Fargate task definitions and service configurations
- Kubernetes deployment manifests with HPA and Ingress
- Architecture decisions documentation in README
- Known issues and roadmap section
- Makefile for common development commands
- Environment variable example file
- Input validation constraints on transaction DTOs
- Claude Code project configuration

### Changed
- Strengthened test assertions in transaction service tests
- Improved local development setup documentation
- Cleaned up documentation and code comments

### Fixed
- CORS configuration now allows localhost:5173 for frontend development
