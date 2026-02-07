import { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  TextField,
  MenuItem,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
} from '@mui/material';
import {
  useSpendingByCategory,
  useMonthlyTrend,
  useAccountSummary,
} from '../hooks/useAnalytics';
import { useAccounts } from '../hooks/useAccounts';
import { SpendingPieChart, MonthlyTrendChart, IncomeExpensesChart } from '../components/Charts';
import { ErrorAlert } from '../components/common';

export default function Analytics() {
  const [selectedAccountId, setSelectedAccountId] = useState<number | undefined>(undefined);
  const [months, setMonths] = useState(6);

  const { data: accounts } = useAccounts();
  const {
    data: spendingData,
    isLoading: spendingLoading,
    error: spendingError,
  } = useSpendingByCategory({ accountId: selectedAccountId });
  const {
    data: trendData,
    isLoading: trendLoading,
    error: trendError,
  } = useMonthlyTrend({ accountId: selectedAccountId, months });
  const {
    data: summaryData,
    isLoading: summaryLoading,
    error: summaryError,
  } = useAccountSummary();

  // Transform MonthlyTrend data for the IncomeExpenses area chart
  // The backend /income-vs-expenses returns a single aggregate, not time series,
  // so we reuse the monthly trend data which already has income vs expenses per month
  const incomeExpensesData = trendData?.map((item) => ({
    period: item.month,
    income: item.income,
    expenses: item.expenses,
    savings: item.netSavings,
    savingsRate: item.income > 0 ? (item.netSavings / item.income) * 100 : 0,
  })) || [];

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  // Use totals from summary response
  const totals = summaryData ? {
    totalBalance: summaryData.totalBalance,
    totalIncome: summaryData.thisMonthIncome,
    totalExpenses: summaryData.thisMonthSpending,
  } : { totalBalance: 0, totalIncome: 0, totalExpenses: 0 };

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          Analytics
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Track your spending patterns and financial trends
        </Typography>
      </Box>

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={6} md={4}>
              <TextField
                fullWidth
                select
                label="Account"
                value={selectedAccountId || ''}
                onChange={(e) => setSelectedAccountId(e.target.value ? parseInt(e.target.value) : undefined)}
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
            <Grid item xs={12} sm={6} md={4}>
              <TextField
                fullWidth
                select
                label="Time Period"
                value={months}
                onChange={(e) => setMonths(parseInt(e.target.value))}
                size="small"
              >
                <MenuItem value={3}>Last 3 months</MenuItem>
                <MenuItem value={6}>Last 6 months</MenuItem>
                <MenuItem value={12}>Last 12 months</MenuItem>
              </TextField>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Summary Stats */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Total Balance
              </Typography>
              {summaryLoading ? (
                <Skeleton variant="text" width={100} height={40} />
              ) : (
                <Typography variant="h5" fontWeight={700}>
                  {formatCurrency(totals?.totalBalance || 0)}
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Total Income
              </Typography>
              {summaryLoading ? (
                <Skeleton variant="text" width={100} height={40} />
              ) : (
                <Typography variant="h5" fontWeight={700} color="success.main">
                  {formatCurrency(totals?.totalIncome || 0)}
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Total Expenses
              </Typography>
              {summaryLoading ? (
                <Skeleton variant="text" width={100} height={40} />
              ) : (
                <Typography variant="h5" fontWeight={700} color="error.main">
                  {formatCurrency(totals?.totalExpenses || 0)}
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Net Savings
              </Typography>
              {summaryLoading ? (
                <Skeleton variant="text" width={100} height={40} />
              ) : (
                <Typography
                  variant="h5"
                  fontWeight={700}
                  color={(totals?.totalIncome || 0) - (totals?.totalExpenses || 0) >= 0 ? 'success.main' : 'error.main'}
                >
                  {formatCurrency((totals?.totalIncome || 0) - (totals?.totalExpenses || 0))}
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Charts Grid */}
      <Grid container spacing={3}>
        {/* Spending by Category */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Spending by Category
              </Typography>
              {spendingError ? (
                <ErrorAlert message="Failed to load spending data" />
              ) : spendingLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                  <Skeleton variant="circular" width={240} height={240} />
                </Box>
              ) : (
                <SpendingPieChart data={spendingData || []} />
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Monthly Trend */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Monthly Income vs Expenses
              </Typography>
              {trendError ? (
                <ErrorAlert message="Failed to load trend data" />
              ) : trendLoading ? (
                <Skeleton variant="rounded" height={350} />
              ) : (
                <MonthlyTrendChart data={trendData || []} />
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Income vs Expenses Area Chart */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Income & Expenses Trend
              </Typography>
              {trendLoading ? (
                <Skeleton variant="rounded" height={350} />
              ) : (
                <IncomeExpensesChart data={incomeExpensesData} />
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Account Summary Table */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Account Summary
              </Typography>
              {summaryError ? (
                <ErrorAlert message="Failed to load account summary" />
              ) : summaryLoading ? (
                <Skeleton variant="rounded" height={200} />
              ) : (
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Account</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell align="right">Balance</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {summaryData?.accounts?.map((account) => (
                        <TableRow key={account.accountId}>
                          <TableCell>
                            <Typography fontWeight={500}>{account.accountName}</Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={account.accountType}
                              size="small"
                              sx={{ textTransform: 'capitalize' }}
                            />
                          </TableCell>
                          <TableCell align="right">
                            <Typography fontWeight={600}>
                              {formatCurrency(account.balance)}
                            </Typography>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
