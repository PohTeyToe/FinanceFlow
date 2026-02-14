# Security Policy

## Security Measures

FinanceFlow implements several security best practices:

- **JWT Authentication** - Stateless token-based auth with short expiry
- **Refresh Token Rotation** - New refresh token issued on each use
- **BCrypt Password Hashing** - Industry-standard password storage
- **Input Validation** - All inputs validated with Bean Validation
- **CORS Configuration** - Restricted cross-origin requests
- **SQL Injection Prevention** - Parameterized queries via JPA

## Reporting a Vulnerability

This is a portfolio/demo project and not intended for production use with real financial data.

If you discover a security issue:

1. **Do not** open a public issue
2. Email me at abdullahsf2001@gmail.com
3. Include steps to reproduce

## Disclaimer

This project is for **educational and portfolio purposes only**. It should not be used to handle real financial transactions or sensitive data without significant additional security review and hardening.
