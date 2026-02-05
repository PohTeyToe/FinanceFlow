import { Grid, Card, CardContent, Typography, Box, Button, Skeleton } from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  AccountBalance as BalanceIcon,
  Receipt as TransactionIcon,
  ArrowForward as ArrowIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAccounts } from '../hooks/useAccounts';
import { useTransactions } from '../hooks/useTransactions';
import { useAccountSummary } from '../hooks/useAnalytics';
import { useAuth } from '../hooks/useAuth';
import AccountCard from '../components/AccountCard';
import TransactionList from '../components/TransactionList';
import { ErrorAlert } from '../components/common';

interface StatCardProps {
  title: string;
  value: string;
  icon: React.ReactNode;
  color: string;
  trend?: {
    value: number;
    isPositive: boolean;
  };
}

function StatCard({ title, value, icon, color, trend }: StatCardProps) {
  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Box>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {title}
            </Typography>
            <Typography variant="h4" fontWeight={700}>
              {value}
            </Typography>
            {trend && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 1 }}>
                {trend.isPositive ? (
                  <TrendingUpIcon sx={{ fontSize: 18, color: 'success.main' }} />
                ) : (
                  <TrendingDownIcon sx={{ fontSize: 18, color: 'error.main' }} />
                )}
                <Typography
                  variant="body2"
                  sx={{ color: trend.isPositive ? 'success.main' : 'error.main' }}
                >
                  {trend.value}%
                </Typography>
              </Box>
            )}
          </Box>
          <Box
            sx={{
              width: 48,
              height: 48,
              borderRadius: 2,
              bgcolor: `${color}15`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: color,
            }}
          >
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
}

function StatCardSkeleton() {
  return (
    <Card>
      <CardContent>
        <Skeleton variant="text" width={100} />
        <Skeleton variant="text" width={150} height={48} />
        <Skeleton variant="text" width={80} />
      </CardContent>
    </Card>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data: accounts, isLoading: accountsLoading, error: accountsError } = useAccounts();
  const { data: summary, isLoading: summaryLoading } = useAccountSummary();
  
  // Get the first account's ID for fetching recent transactions
  const firstAccountId = accounts?.[0]?.id;
  const { data: transactionsData, isLoading: transactionsLoading } = useTransactions(
    firstAccountId ? { accountId: firstAccountId, size: 5 } : undefined
  );

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  // Use values from the summary response
  const totalBalance = summary?.totalBalance || 0;
  const totalIncome = summary?.thisMonthIncome || 0;
  const totalExpenses = summary?.thisMonthSpending || 0;
  const spendingChangePercent = summary?.comparedToLastMonth || 0;

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          Welcome back, {user?.firstName}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Here's an overview of your financial status
        </Typography>
      </Box>

      {/* Stats Grid */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} lg={3}>
          {accountsLoading || summaryLoading ? (
            <StatCardSkeleton />
          ) : (
            <StatCard
              title="Total Balance"
              value={formatCurrency(totalBalance)}
              icon={<BalanceIcon />}
              color="#2563eb"
            />
          )}
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          {summaryLoading ? (
            <StatCardSkeleton />
          ) : (
            <StatCard
              title="Total Income"
              value={formatCurrency(totalIncome)}
              icon={<TrendingUpIcon />}
              color="#10b981"
              trend={{ value: 12, isPositive: true }}
            />
          )}
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          {summaryLoading ? (
            <StatCardSkeleton />
          ) : (
            <StatCard
              title="Total Expenses"
              value={formatCurrency(totalExpenses)}
              icon={<TrendingDownIcon />}
              color="#ef4444"
              trend={spendingChangePercent !== 0 ? { 
                value: Math.abs(spendingChangePercent), 
                isPositive: spendingChangePercent < 0 // Less spending is positive
              } : undefined}
            />
          )}
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          {summaryLoading ? (
            <StatCardSkeleton />
          ) : (
            <StatCard
              title="Top Category"
              value={summary?.topSpendingCategory || 'N/A'}
              icon={<TransactionIcon />}
              color="#7c3aed"
            />
          )}
        </Grid>
      </Grid>

      {accountsError && (
        <ErrorAlert
          message="Failed to load accounts"
          onRetry={() => window.location.reload()}
        />
      )}

      <Grid container spacing={3}>
        {/* Accounts */}
        <Grid item xs={12} lg={8}>
          <Card>
            <CardContent>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  mb: 3,
                }}
              >
                <Typography variant="h6" fontWeight={600}>
                  Your Accounts
                </Typography>
                <Button
                  endIcon={<ArrowIcon />}
                  onClick={() => navigate('/accounts')}
                >
                  View All
                </Button>
              </Box>
              {accountsLoading ? (
                <Grid container spacing={2}>
                  {[1, 2, 3].map((i) => (
                    <Grid item xs={12} sm={6} key={i}>
                      <Skeleton variant="rounded" height={160} />
                    </Grid>
                  ))}
                </Grid>
              ) : accounts && accounts.length > 0 ? (
                <Grid container spacing={2}>
                  {accounts.slice(0, 4).map((account) => (
                    <Grid item xs={12} sm={6} key={account.id}>
                      <AccountCard
                        account={account}
                        onClick={() => navigate(`/accounts`)}
                      />
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Box sx={{ py: 4, textAlign: 'center' }}>
                  <Typography color="text.secondary" gutterBottom>
                    No accounts yet
                  </Typography>
                  <Button
                    variant="contained"
                    onClick={() => navigate('/accounts')}
                  >
                    Create Your First Account
                  </Button>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Transactions */}
        <Grid item xs={12} lg={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  mb: 2,
                }}
              >
                <Typography variant="h6" fontWeight={600}>
                  Recent Transactions
                </Typography>
                <Button
                  endIcon={<ArrowIcon />}
                  onClick={() => navigate('/transactions')}
                >
                  View All
                </Button>
              </Box>
              {transactionsLoading ? (
                <Box>
                  {[1, 2, 3, 4, 5].map((i) => (
                    <Box key={i} sx={{ display: 'flex', gap: 2, py: 1.5 }}>
                      <Skeleton variant="circular" width={40} height={40} />
                      <Box sx={{ flex: 1 }}>
                        <Skeleton variant="text" width="60%" />
                        <Skeleton variant="text" width="40%" />
                      </Box>
                      <Skeleton variant="text" width={60} />
                    </Box>
                  ))}
                </Box>
              ) : (
                <TransactionList
                  transactions={transactionsData?.content || []}
                />
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
