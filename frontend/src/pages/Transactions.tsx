import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  MenuItem,
  Grid,
  Pagination,
  Skeleton,
  Chip,
  InputAdornment,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Snackbar,
} from '@mui/material';
import {
  FilterList as FilterIcon,
  Clear as ClearIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { useTransactions, useDeposit, useWithdraw } from '../hooks/useTransactions';
import { useAccounts } from '../hooks/useAccounts';
import TransactionList from '../components/TransactionList';
import { ErrorAlert } from '../components/common';
import { TransactionType, type TransactionFilters } from '../types';

const transactionTypes = [
  { value: '', label: 'All Types' },
  { value: TransactionType.DEPOSIT, label: 'Deposits' },
  { value: TransactionType.WITHDRAWAL, label: 'Withdrawals' },
  { value: TransactionType.TRANSFER, label: 'Transfers' },
];

const categories = [
  'Food & Dining',
  'Shopping',
  'Transportation',
  'Bills & Utilities',
  'Entertainment',
  'Health',
  'Travel',
  'Income',
  'Transfer',
  'Other',
];

export default function Transactions() {
  const [filters, setFilters] = useState<TransactionFilters>({
    page: 0,
    size: 10,
  });
  const [showFilters, setShowFilters] = useState(false);
  const [depositDialogOpen, setDepositDialogOpen] = useState(false);
  const [withdrawDialogOpen, setWithdrawDialogOpen] = useState(false);
  const [transactionForm, setTransactionForm] = useState({
    accountId: 0,
    amount: 0,
    description: '',
    category: '',
  });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  const { data: accounts } = useAccounts();
  
  // Set default accountId to first account when accounts load
  useEffect(() => {
    if (accounts?.[0]?.id && !filters.accountId) {
      setFilters(prev => ({ ...prev, accountId: accounts[0].id }));
    }
  }, [accounts, filters.accountId]);
  
  const { data: transactionsData, isLoading, error, refetch } = useTransactions(filters);
  const deposit = useDeposit();
  const withdraw = useWithdraw();

  const handleFilterChange = (key: keyof TransactionFilters, value: string | number) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value || undefined,
      page: key !== 'page' ? 0 : (value as number),
    }));
  };

  const clearFilters = () => {
    setFilters({ page: 0, size: 10, accountId: filters.accountId });
  };

  const hasActiveFilters = filters.accountId || filters.type || filters.category || filters.startDate || filters.endDate;

  const handleDepositSubmit = async () => {
    try {
      await deposit.mutateAsync({
        accountId: transactionForm.accountId,
        amount: transactionForm.amount,
        description: transactionForm.description,
        category: transactionForm.category,
      });
      setDepositDialogOpen(false);
      setTransactionForm({ accountId: 0, amount: 0, description: '', category: '' });
      setSnackbar({ open: true, message: 'Deposit successful', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Deposit failed', severity: 'error' });
    }
  };

  const handleWithdrawSubmit = async () => {
    try {
      await withdraw.mutateAsync({
        accountId: transactionForm.accountId,
        amount: transactionForm.amount,
        description: transactionForm.description,
        category: transactionForm.category,
      });
      setWithdrawDialogOpen(false);
      setTransactionForm({ accountId: 0, amount: 0, description: '', category: '' });
      setSnackbar({ open: true, message: 'Withdrawal successful', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Withdrawal failed. Check your balance.', severity: 'error' });
    }
  };

  const openDepositDialog = () => {
    setTransactionForm({ accountId: accounts?.[0]?.id || 0, amount: 0, description: '', category: '' });
    setDepositDialogOpen(true);
  };

  const openWithdrawDialog = () => {
    setTransactionForm({ accountId: accounts?.[0]?.id || 0, amount: 0, description: '', category: '' });
    setWithdrawDialogOpen(true);
  };

  return (
    <Box>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 4,
          flexWrap: 'wrap',
          gap: 2,
        }}
      >
        <Box>
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Transactions
          </Typography>
          <Typography variant="body1" color="text.secondary">
            View and manage your transaction history
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<AddIcon />} onClick={openDepositDialog}>
            Deposit
          </Button>
          <Button variant="outlined" color="error" startIcon={<AddIcon />} onClick={openWithdrawDialog}>
            Withdraw
          </Button>
        </Box>
      </Box>

      {/* Filters Toggle */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Button
          startIcon={<FilterIcon />}
          onClick={() => setShowFilters(!showFilters)}
          variant={showFilters ? 'contained' : 'outlined'}
        >
          Filters
        </Button>
        {hasActiveFilters && (
          <Chip
            label="Clear filters"
            onDelete={clearFilters}
            deleteIcon={<ClearIcon />}
            size="small"
          />
        )}
        {transactionsData && (
          <Typography variant="body2" color="text.secondary">
            {transactionsData.totalElements} transaction{transactionsData.totalElements !== 1 ? 's' : ''} found
          </Typography>
        )}
      </Box>

      {/* Filters */}
      {showFilters && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  select
                  label="Account"
                  value={filters.accountId || ''}
                  onChange={(e) => handleFilterChange('accountId', e.target.value ? parseInt(e.target.value) : 0)}
                  size="small"
                >
                  <MenuItem value="">All Accounts</MenuItem>
                  {accounts?.map((account) => (
                    <MenuItem key={account.id} value={account.id}>
                      {account.accountName}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  select
                  label="Type"
                  value={filters.type || ''}
                  onChange={(e) => handleFilterChange('type', e.target.value)}
                  size="small"
                >
                  {transactionTypes.map((type) => (
                    <MenuItem key={type.value} value={type.value}>
                      {type.label}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  select
                  label="Category"
                  value={filters.category || ''}
                  onChange={(e) => handleFilterChange('category', e.target.value)}
                  size="small"
                >
                  <MenuItem value="">All Categories</MenuItem>
                  {categories.map((cat) => (
                    <MenuItem key={cat} value={cat}>
                      {cat}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  label="Start Date"
                  type="date"
                  value={filters.startDate || ''}
                  onChange={(e) => handleFilterChange('startDate', e.target.value)}
                  size="small"
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  label="End Date"
                  type="date"
                  value={filters.endDate || ''}
                  onChange={(e) => handleFilterChange('endDate', e.target.value)}
                  size="small"
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}

      {error && (
        <ErrorAlert
          message="Failed to load transactions"
          onRetry={() => refetch()}
        />
      )}

      {/* Transactions List */}
      <Card>
        <CardContent>
          {isLoading ? (
            <Box>
              {[1, 2, 3, 4, 5].map((i) => (
                <Box key={i} sx={{ display: 'flex', gap: 2, py: 2, borderBottom: 1, borderColor: 'divider' }}>
                  <Skeleton variant="circular" width={48} height={48} />
                  <Box sx={{ flex: 1 }}>
                    <Skeleton variant="text" width="40%" />
                    <Skeleton variant="text" width="25%" />
                  </Box>
                  <Skeleton variant="text" width={80} />
                </Box>
              ))}
            </Box>
          ) : (
            <TransactionList transactions={transactionsData?.content || []} />
          )}
        </CardContent>
      </Card>

      {/* Pagination */}
      {transactionsData && transactionsData.totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Pagination
            count={transactionsData.totalPages}
            page={(filters.page || 0) + 1}
            onChange={(_, page) => handleFilterChange('page', page - 1)}
            color="primary"
            showFirstButton
            showLastButton
          />
        </Box>
      )}

      {/* Deposit Dialog */}
      <Dialog open={depositDialogOpen} onClose={() => setDepositDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Make a Deposit</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            select
            label="Account"
            value={transactionForm.accountId}
            onChange={(e) => setTransactionForm({ ...transactionForm, accountId: parseInt(e.target.value) })}
            sx={{ mt: 2 }}
          >
            {accounts?.map((account) => (
              <MenuItem key={account.id} value={account.id}>
                {account.accountName} ({account.accountNumber})
              </MenuItem>
            ))}
          </TextField>
          <TextField
            fullWidth
            label="Amount"
            type="number"
            value={transactionForm.amount || ''}
            onChange={(e) => setTransactionForm({ ...transactionForm, amount: parseFloat(e.target.value) || 0 })}
            sx={{ mt: 2 }}
            InputProps={{
              startAdornment: <InputAdornment position="start">$</InputAdornment>,
            }}
          />
          <TextField
            fullWidth
            label="Description (optional)"
            value={transactionForm.description}
            onChange={(e) => setTransactionForm({ ...transactionForm, description: e.target.value })}
            sx={{ mt: 2 }}
          />
          <TextField
            fullWidth
            select
            label="Category"
            value={transactionForm.category}
            onChange={(e) => setTransactionForm({ ...transactionForm, category: e.target.value })}
            sx={{ mt: 2 }}
          >
            {categories.map((cat) => (
              <MenuItem key={cat} value={cat}>
                {cat}
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setDepositDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleDepositSubmit}
            disabled={!transactionForm.accountId || !transactionForm.amount || deposit.isPending}
          >
            {deposit.isPending ? 'Processing...' : 'Deposit'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Withdraw Dialog */}
      <Dialog open={withdrawDialogOpen} onClose={() => setWithdrawDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Make a Withdrawal</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            select
            label="Account"
            value={transactionForm.accountId}
            onChange={(e) => setTransactionForm({ ...transactionForm, accountId: parseInt(e.target.value) })}
            sx={{ mt: 2 }}
          >
            {accounts?.map((account) => (
              <MenuItem key={account.id} value={account.id}>
                {account.accountName} (Balance: ${account.balance.toFixed(2)})
              </MenuItem>
            ))}
          </TextField>
          <TextField
            fullWidth
            label="Amount"
            type="number"
            value={transactionForm.amount || ''}
            onChange={(e) => setTransactionForm({ ...transactionForm, amount: parseFloat(e.target.value) || 0 })}
            sx={{ mt: 2 }}
            InputProps={{
              startAdornment: <InputAdornment position="start">$</InputAdornment>,
            }}
          />
          <TextField
            fullWidth
            label="Description (optional)"
            value={transactionForm.description}
            onChange={(e) => setTransactionForm({ ...transactionForm, description: e.target.value })}
            sx={{ mt: 2 }}
          />
          <TextField
            fullWidth
            select
            label="Category"
            value={transactionForm.category}
            onChange={(e) => setTransactionForm({ ...transactionForm, category: e.target.value })}
            sx={{ mt: 2 }}
          >
            {categories.map((cat) => (
              <MenuItem key={cat} value={cat}>
                {cat}
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setWithdrawDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            color="error"
            onClick={handleWithdrawSubmit}
            disabled={!transactionForm.accountId || !transactionForm.amount || withdraw.isPending}
          >
            {withdraw.isPending ? 'Processing...' : 'Withdraw'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
