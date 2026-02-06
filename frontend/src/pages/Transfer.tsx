import { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  MenuItem,
  Button,
  Grid,
  Alert,
  Snackbar,
  Divider,
  Stepper,
  Step,
  StepLabel,
  Paper,
} from '@mui/material';
import {
  SwapHoriz as TransferIcon,
  CheckCircle as SuccessIcon,
} from '@mui/icons-material';
import { useAccounts } from '../hooks/useAccounts';
import { useTransfer } from '../hooks/useTransactions';
import type { TransferRequest } from '../types';

const steps = ['Select Accounts', 'Enter Amount', 'Confirm'];

export default function Transfer() {
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState<TransferRequest>({
    fromAccountId: 0,
    toAccountId: 0,
    amount: 0,
    description: '',
  });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });
  const [transferComplete, setTransferComplete] = useState(false);

  const { data: accounts } = useAccounts();
  const transfer = useTransfer();

  const fromAccount = accounts?.find((a) => a.id === formData.fromAccountId);
  const toAccount = accounts?.find((a) => a.id === formData.toAccountId);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const handleNext = () => {
    setActiveStep((prev) => prev + 1);
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  const handleSubmit = async () => {
    try {
      await transfer.mutateAsync(formData);
      setTransferComplete(true);
      setSnackbar({ open: true, message: 'Transfer completed successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Transfer failed. Please try again.', severity: 'error' });
    }
  };

  const handleReset = () => {
    setActiveStep(0);
    setFormData({
      fromAccountId: 0,
      toAccountId: 0,
      amount: 0,
      description: '',
    });
    setTransferComplete(false);
  };

  const isStep1Valid = formData.fromAccountId && formData.toAccountId && formData.fromAccountId !== formData.toAccountId;
  const isStep2Valid = formData.amount > 0 && formData.amount <= (fromAccount?.balance || 0);

  const renderStepContent = () => {
    if (transferComplete) {
      return (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <SuccessIcon sx={{ fontSize: 80, color: 'success.main', mb: 2 }} />
          <Typography variant="h5" fontWeight={600} gutterBottom>
            Transfer Successful!
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>
            {formatCurrency(formData.amount)} has been transferred from {fromAccount?.accountName} to {toAccount?.accountName}
          </Typography>
          <Button variant="contained" onClick={handleReset}>
            Make Another Transfer
          </Button>
        </Box>
      );
    }

    switch (activeStep) {
      case 0:
        return (
          <Box>
            <Typography variant="h6" fontWeight={600} gutterBottom>
              Select Accounts
            </Typography>
            <Typography color="text.secondary" sx={{ mb: 3 }}>
              Choose the source and destination accounts for your transfer
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} md={5}>
                <TextField
                  fullWidth
                  select
                  label="From Account"
                  value={formData.fromAccountId || ''}
                  onChange={(e) => setFormData({ ...formData, fromAccountId: parseInt(e.target.value) })}
                >
                  <MenuItem value="" disabled>
                    Select source account
                  </MenuItem>
                  {accounts?.filter((a) => a.isActive).map((account) => (
                    <MenuItem key={account.id} value={account.id} disabled={account.id === formData.toAccountId}>
                      <Box>
                        <Typography>{account.accountName}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          Balance: {formatCurrency(account.balance)}
                        </Typography>
                      </Box>
                    </MenuItem>
                  ))}
                </TextField>
                {fromAccount && (
                  <Paper variant="outlined" sx={{ p: 2, mt: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      Available Balance
                    </Typography>
                    <Typography variant="h5" fontWeight={700}>
                      {formatCurrency(fromAccount.balance)}
                    </Typography>
                  </Paper>
                )}
              </Grid>
              <Grid item xs={12} md={2} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <TransferIcon sx={{ fontSize: 40, color: 'primary.main' }} />
              </Grid>
              <Grid item xs={12} md={5}>
                <TextField
                  fullWidth
                  select
                  label="To Account"
                  value={formData.toAccountId || ''}
                  onChange={(e) => setFormData({ ...formData, toAccountId: parseInt(e.target.value) })}
                >
                  <MenuItem value="" disabled>
                    Select destination account
                  </MenuItem>
                  {accounts?.filter((a) => a.isActive).map((account) => (
                    <MenuItem key={account.id} value={account.id} disabled={account.id === formData.fromAccountId}>
                      <Box>
                        <Typography>{account.accountName}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          Balance: {formatCurrency(account.balance)}
                        </Typography>
                      </Box>
                    </MenuItem>
                  ))}
                </TextField>
                {toAccount && (
                  <Paper variant="outlined" sx={{ p: 2, mt: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      Current Balance
                    </Typography>
                    <Typography variant="h5" fontWeight={700}>
                      {formatCurrency(toAccount.balance)}
                    </Typography>
                  </Paper>
                )}
              </Grid>
            </Grid>
          </Box>
        );

      case 1:
        return (
          <Box>
            <Typography variant="h6" fontWeight={600} gutterBottom>
              Enter Amount
            </Typography>
            <Typography color="text.secondary" sx={{ mb: 3 }}>
              Specify the amount to transfer and an optional description
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Amount"
                  type="number"
                  value={formData.amount || ''}
                  onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) || 0 })}
                  InputProps={{
                    startAdornment: <Typography sx={{ mr: 1 }}>$</Typography>,
                  }}
                  error={formData.amount > (fromAccount?.balance || 0)}
                  helperText={
                    formData.amount > (fromAccount?.balance || 0)
                      ? 'Amount exceeds available balance'
                      : `Available: ${formatCurrency(fromAccount?.balance || 0)}`
                  }
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Description (optional)"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="e.g., Monthly savings transfer"
                />
              </Grid>
            </Grid>
          </Box>
        );

      case 2:
        return (
          <Box>
            <Typography variant="h6" fontWeight={600} gutterBottom>
              Confirm Transfer
            </Typography>
            <Typography color="text.secondary" sx={{ mb: 3 }}>
              Review the details below before confirming your transfer
            </Typography>
            <Paper variant="outlined" sx={{ p: 3 }}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography color="text.secondary">From</Typography>
                    <Typography fontWeight={600}>{fromAccount?.accountName}</Typography>
                  </Box>
                  <Divider />
                </Grid>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography color="text.secondary">To</Typography>
                    <Typography fontWeight={600}>{toAccount?.accountName}</Typography>
                  </Box>
                  <Divider />
                </Grid>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography color="text.secondary">Amount</Typography>
                    <Typography variant="h5" fontWeight={700} color="primary">
                      {formatCurrency(formData.amount)}
                    </Typography>
                  </Box>
                  <Divider />
                </Grid>
                {formData.description && (
                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography color="text.secondary">Description</Typography>
                      <Typography>{formData.description}</Typography>
                    </Box>
                  </Grid>
                )}
              </Grid>
            </Paper>
            <Alert severity="info" sx={{ mt: 3 }}>
              After this transfer, your balances will be:
              <Box sx={{ mt: 1 }}>
                <Typography variant="body2">
                  {fromAccount?.accountName}: {formatCurrency((fromAccount?.balance || 0) - formData.amount)}
                </Typography>
                <Typography variant="body2">
                  {toAccount?.accountName}: {formatCurrency((toAccount?.balance || 0) + formData.amount)}
                </Typography>
              </Box>
            </Alert>
          </Box>
        );

      default:
        return null;
    }
  };

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          Transfer Money
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Move money between your accounts instantly
        </Typography>
      </Box>

      <Card>
        <CardContent sx={{ p: 4 }}>
          {!transferComplete && (
            <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
              {steps.map((label) => (
                <Step key={label}>
                  <StepLabel>{label}</StepLabel>
                </Step>
              ))}
            </Stepper>
          )}

          {renderStepContent()}

          {!transferComplete && (
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
              <Button
                disabled={activeStep === 0}
                onClick={handleBack}
              >
                Back
              </Button>
              {activeStep === steps.length - 1 ? (
                <Button
                  variant="contained"
                  onClick={handleSubmit}
                  disabled={transfer.isPending}
                >
                  {transfer.isPending ? 'Processing...' : 'Confirm Transfer'}
                </Button>
              ) : (
                <Button
                  variant="contained"
                  onClick={handleNext}
                  disabled={activeStep === 0 ? !isStep1Valid : !isStep2Valid}
                >
                  Continue
                </Button>
              )}
            </Box>
          )}
        </CardContent>
      </Card>

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
