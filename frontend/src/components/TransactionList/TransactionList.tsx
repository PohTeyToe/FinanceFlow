import {
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Typography,
  Box,
  Chip,
  Divider,
} from '@mui/material';
import {
  ArrowDownward as DepositIcon,
  ArrowUpward as WithdrawIcon,
  SwapHoriz as TransferIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import type { Transaction, TransactionType } from '../../types';

interface TransactionListProps {
  transactions: Transaction[];
  showDate?: boolean;
}

const transactionConfig: Record<TransactionType, { icon: React.ReactNode; color: string; label: string }> = {
  DEPOSIT: {
    icon: <DepositIcon />,
    color: '#10b981',
    label: 'Deposit',
  },
  WITHDRAWAL: {
    icon: <WithdrawIcon />,
    color: '#ef4444',
    label: 'Withdrawal',
  },
  TRANSFER: {
    icon: <TransferIcon />,
    color: '#2563eb',
    label: 'Transfer',
  },
};

// Default config for unknown transaction types
const defaultTransactionConfig = {
  icon: <TransferIcon />,
  color: '#6b7280',
  label: 'Transaction',
};

export default function TransactionList({ transactions, showDate = true }: TransactionListProps) {
  const formatCurrency = (amount: number, currency: string, type: TransactionType) => {
    const sign = type === 'DEPOSIT' ? '+' : '-';
    return `${sign}${new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(Math.abs(amount))}`;
  };

  if (transactions.length === 0) {
    return (
      <Box sx={{ py: 4, textAlign: 'center' }}>
        <Typography color="text.secondary">No transactions found</Typography>
      </Box>
    );
  }

  return (
    <List disablePadding>
      {transactions.map((transaction, index) => {
        const config = transactionConfig[transaction.type] || defaultTransactionConfig;
        return (
          <Box key={transaction.id}>
            <ListItem
              sx={{
                py: 2,
                px: 0,
              }}
            >
              <ListItemAvatar>
                <Avatar
                  sx={{
                    bgcolor: `${config.color}15`,
                    color: config.color,
                  }}
                >
                  {config.icon}
                </Avatar>
              </ListItemAvatar>
              <ListItemText
                primary={
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body1" fontWeight={500}>
                      {transaction.description || config.label}
                    </Typography>
                    {transaction.category && (
                      <Chip
                        label={transaction.category}
                        size="small"
                        sx={{ height: 20, fontSize: '0.7rem' }}
                      />
                    )}
                  </Box>
                }
                secondary={
                  showDate
                    ? format(new Date(transaction.createdAt), 'MMM dd, yyyy • h:mm a')
                    : transaction.referenceNumber
                }
              />
              <Box sx={{ textAlign: 'right' }}>
                <Typography
                  variant="body1"
                  fontWeight={600}
                  sx={{
                    color: transaction.type === 'DEPOSIT' ? 'success.main' : 'error.main',
                  }}
                >
                  {formatCurrency(transaction.amount, transaction.currency, transaction.type)}
                </Typography>
                <Chip
                  label={transaction.status}
                  size="small"
                  color={
                    transaction.status === 'COMPLETED'
                      ? 'success'
                      : transaction.status === 'PENDING'
                      ? 'warning'
                      : 'error'
                  }
                  sx={{ height: 20, fontSize: '0.65rem', mt: 0.5 }}
                />
              </Box>
            </ListItem>
            {index < transactions.length - 1 && <Divider />}
          </Box>
        );
      })}
    </List>
  );
}
