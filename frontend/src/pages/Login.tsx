import { useState } from 'react';
import { useNavigate, Link as RouterLink, useLocation } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Link,
  Alert,
  InputAdornment,
  IconButton,
} from '@mui/material';
import {
  Visibility,
  VisibilityOff,
  AccountBalance as LogoIcon,
} from '@mui/icons-material';
import { useAuth } from '../hooks/useAuth';
import type { LoginRequest } from '../types';

export default function Login() {
  const [formData, setFormData] = useState<LoginRequest>({
    email: '',
    password: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login(formData);
      navigate(from, { replace: true });
    } catch (err) {
      setError('Invalid email or password. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: 2,
      }}
    >
      <Card sx={{ maxWidth: 440, width: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Box
              sx={{
                width: 56,
                height: 56,
                borderRadius: 3,
                background: 'linear-gradient(135deg, #2563eb 0%, #7c3aed 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                mx: 'auto',
                mb: 2,
              }}
            >
              <LogoIcon sx={{ color: 'white', fontSize: 32 }} />
            </Box>
            <Typography variant="h4" fontWeight={700} gutterBottom>
              Welcome Back
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sign in to continue to FinanceFlow
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <Box sx={{ mb: 2, p: 2, bgcolor: 'action.hover', borderRadius: 2 }}>
            <Typography variant="caption" color="text.secondary">
              Demo credentials:
            </Typography>
            <Typography variant="body2" fontWeight={500}>
              john.doe@example.com / password123
            </Typography>
          </Box>

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Email Address"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              required
              sx={{ mb: 2 }}
              autoComplete="email"
            />
            <TextField
              fullWidth
              label="Password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              value={formData.password}
              onChange={handleChange}
              required
              sx={{ mb: 3 }}
              autoComplete="current-password"
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <Button
              type="submit"
              variant="contained"
              fullWidth
              size="large"
              disabled={isLoading}
              sx={{ mb: 2 }}
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </Button>
          </form>

          <Typography variant="body2" textAlign="center" color="text.secondary">
            Don't have an account?{' '}
            <Link component={RouterLink} to="/register" fontWeight={600}>
              Sign Up
            </Link>
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
