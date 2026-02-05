import { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Skeleton,
  Alert,
  Snackbar,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useAccounts, useCreateAccount, useUpdateAccount, useDeleteAccount } from '../hooks/useAccounts';
import AccountCard from '../components/AccountCard';
import { ErrorAlert } from '../components/common';
import { AccountType, type CreateAccountRequest, type Account } from '../types';

const accountTypes = [
  { value: AccountType.CHECKING, label: 'Checking Account' },
  { value: AccountType.SAVINGS, label: 'Savings Account' },
  { value: AccountType.CREDIT, label: 'Credit Card' },
  { value: AccountType.INVESTMENT, label: 'Investment Account' },
];

export default function Accounts() {
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  const [formData, setFormData] = useState<CreateAccountRequest>({
    accountName: '',
    accountType: AccountType.CHECKING,
    initialDeposit: 0,
  });
  const [editName, setEditName] = useState('');

  const { data: accounts, isLoading, error, refetch } = useAccounts();
  const createAccount = useCreateAccount();
  const updateAccount = useUpdateAccount();
  const deleteAccount = useDeleteAccount();

  const handleCreateOpen = () => {
    setFormData({
      accountName: '',
      accountType: AccountType.CHECKING,
      initialDeposit: 0,
    });
    setCreateDialogOpen(true);
  };

  const handleEditOpen = (account: Account) => {
    setSelectedAccount(account);
    setEditName(account.accountName);
    setEditDialogOpen(true);
  };

  const handleDeleteOpen = (account: Account) => {
    setSelectedAccount(account);
    setDeleteDialogOpen(true);
  };

  const handleCreateSubmit = async () => {
    try {
      await createAccount.mutateAsync(formData);
      setCreateDialogOpen(false);
      setSnackbar({ open: true, message: 'Account created successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to create account', severity: 'error' });
    }
  };

  const handleEditSubmit = async () => {
    if (!selectedAccount) return;
    try {
      await updateAccount.mutateAsync({
        id: selectedAccount.id,
        data: { accountName: editName },
      });
      setEditDialogOpen(false);
      setSnackbar({ open: true, message: 'Account updated successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to update account', severity: 'error' });
    }
  };

  const handleDeleteSubmit = async () => {
    if (!selectedAccount) return;
    try {
      await deleteAccount.mutateAsync(selectedAccount.id);
      setDeleteDialogOpen(false);
      setSnackbar({ open: true, message: 'Account deactivated successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to deactivate account', severity: 'error' });
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const totalBalance = accounts?.reduce((sum, acc) => sum + acc.balance, 0) || 0;
  const activeAccounts = accounts?.filter((acc) => acc.isActive) || [];

  return (
    <Box>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 4,
        }}
      >
        <Box>
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Accounts
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage your bank accounts and cards
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreateOpen}
        >
          Add Account
        </Button>
      </Box>

      {/* Total Balance Card */}
      <Card sx={{ mb: 4, background: 'linear-gradient(135deg, #2563eb 0%, #7c3aed 100%)' }}>
        <CardContent sx={{ color: 'white', py: 4 }}>
          <Typography variant="body1" sx={{ opacity: 0.9 }} gutterBottom>
            Total Balance
          </Typography>
          <Typography variant="h3" fontWeight={700}>
            {isLoading ? <Skeleton width={200} sx={{ bgcolor: 'rgba(255,255,255,0.2)' }} /> : formatCurrency(totalBalance)}
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.8, mt: 1 }}>
            Across {activeAccounts.length} active account{activeAccounts.length !== 1 ? 's' : ''}
          </Typography>
        </CardContent>
      </Card>

      {error && (
        <ErrorAlert
          message="Failed to load accounts"
          onRetry={() => refetch()}
        />
      )}

      {/* Accounts Grid */}
      <Grid container spacing={3}>
        {isLoading ? (
          [1, 2, 3, 4].map((i) => (
            <Grid item xs={12} sm={6} md={4} key={i}>
              <Skeleton variant="rounded" height={180} />
            </Grid>
          ))
        ) : accounts && accounts.length > 0 ? (
          accounts.map((account) => (
            <Grid item xs={12} sm={6} md={4} key={account.id}>
              <AccountCard
                account={account}
                onEdit={() => handleEditOpen(account)}
                onDelete={() => handleDeleteOpen(account)}
              />
            </Grid>
          ))
        ) : (
          <Grid item xs={12}>
            <Card>
              <CardContent sx={{ py: 8, textAlign: 'center' }}>
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  No accounts yet
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  Create your first account to start tracking your finances
                </Typography>
                <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreateOpen}>
                  Create Account
                </Button>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>

      {/* Create Account Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Account</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Account Name"
            value={formData.accountName}
            onChange={(e) => setFormData({ ...formData, accountName: e.target.value })}
            sx={{ mt: 2 }}
          />
          <TextField
            fullWidth
            select
            label="Account Type"
            value={formData.accountType}
            onChange={(e) => setFormData({ ...formData, accountType: e.target.value as AccountType })}
            sx={{ mt: 2 }}
          >
            {accountTypes.map((type) => (
              <MenuItem key={type.value} value={type.value}>
                {type.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            fullWidth
            label="Initial Deposit"
            type="number"
            value={formData.initialDeposit}
            onChange={(e) => setFormData({ ...formData, initialDeposit: parseFloat(e.target.value) || 0 })}
            sx={{ mt: 2 }}
            InputProps={{
              startAdornment: <Typography sx={{ mr: 1 }}>$</Typography>,
            }}
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleCreateSubmit}
            disabled={!formData.accountName || createAccount.isPending}
          >
            {createAccount.isPending ? 'Creating...' : 'Create Account'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit Account Dialog */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Account</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Account Name"
            value={editName}
            onChange={(e) => setEditName(e.target.value)}
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleEditSubmit}
            disabled={!editName || updateAccount.isPending}
          >
            {updateAccount.isPending ? 'Saving...' : 'Save Changes'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Deactivate Account</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to deactivate "{selectedAccount?.accountName}"? This action can be undone by contacting support.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            color="error"
            onClick={handleDeleteSubmit}
            disabled={deleteAccount.isPending}
          >
            {deleteAccount.isPending ? 'Deactivating...' : 'Deactivate'}
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
