import { Card, CardContent, Typography, Box, Chip, IconButton, Menu, MenuItem } from '@mui/material';
import {
  MoreVert as MoreIcon,
  AccountBalance as CheckingIcon,
  Savings as SavingsIcon,
  CreditCard as CreditIcon,
  TrendingUp as InvestmentIcon,
} from '@mui/icons-material';
import { useState } from 'react';
import type { Account, AccountType } from '../../types';

interface AccountCardProps {
  account: Account;
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
}

const accountTypeConfig: Record<AccountType, { icon: React.ReactNode; color: string; gradient: string }> = {
  CHECKING: {
    icon: <CheckingIcon />,
    color: '#2563eb',
    gradient: 'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)',
  },
  SAVINGS: {
    icon: <SavingsIcon />,
    color: '#10b981',
    gradient: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)',
  },
  CREDIT: {
    icon: <CreditIcon />,
    color: '#ef4444',
    gradient: 'linear-gradient(135deg, #ef4444 0%, #f87171 100%)',
  },
  INVESTMENT: {
    icon: <InvestmentIcon />,
    color: '#7c3aed',
    gradient: 'linear-gradient(135deg, #7c3aed 0%, #8b5cf6 100%)',
  },
};

// Default config for unknown account types
const defaultConfig = {
  icon: <CheckingIcon />,
  color: '#6b7280',
  gradient: 'linear-gradient(135deg, #6b7280 0%, #9ca3af 100%)',
};

export default function AccountCard({ account, onClick, onEdit, onDelete }: AccountCardProps) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const config = accountTypeConfig[account.accountType] || defaultConfig;

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleEdit = () => {
    handleMenuClose();
    onEdit?.();
  };

  const handleDelete = () => {
    handleMenuClose();
    onDelete?.();
  };

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(amount);
  };

  return (
    <Card
      onClick={onClick}
      sx={{
        cursor: onClick ? 'pointer' : 'default',
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': onClick
          ? {
              transform: 'translateY(-4px)',
              boxShadow: 4,
            }
          : {},
      }}
    >
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box
            sx={{
              width: 48,
              height: 48,
              borderRadius: 2,
              background: config.gradient,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'white',
            }}
          >
            {config.icon}
          </Box>
          {(onEdit || onDelete) && (
            <>
              <IconButton size="small" onClick={handleMenuOpen}>
                <MoreIcon />
              </IconButton>
              <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
                {onEdit && <MenuItem onClick={handleEdit}>Edit</MenuItem>}
                {onDelete && <MenuItem onClick={handleDelete}>Deactivate</MenuItem>}
              </Menu>
            </>
          )}
        </Box>
        <Typography variant="h6" fontWeight={600} gutterBottom>
          {account.accountName}
        </Typography>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          {account.accountNumber}
        </Typography>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2 }}>
          <Typography variant="h5" fontWeight={700}>
            {formatCurrency(account.balance, account.currency)}
          </Typography>
          <Chip
            label={account.accountType}
            size="small"
            sx={{
              bgcolor: `${config.color}15`,
              color: config.color,
              fontWeight: 600,
            }}
          />
        </Box>
        {!account.isActive && (
          <Chip
            label="Inactive"
            size="small"
            color="error"
            sx={{ mt: 1 }}
          />
        )}
      </CardContent>
    </Card>
  );
}
