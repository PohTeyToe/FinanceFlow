import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { Box, Typography, useTheme } from '@mui/material';
import type { IncomeVsExpenses } from '../../types';

interface IncomeExpensesChartProps {
  data: IncomeVsExpenses[];
}

export default function IncomeExpensesChart({ data }: IncomeExpensesChartProps) {
  const theme = useTheme();

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(value);
  };

  if (data.length === 0) {
    return (
      <Box sx={{ py: 8, textAlign: 'center' }}>
        <Typography color="text.secondary">No data available</Typography>
      </Box>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={350}>
      <AreaChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
        <defs>
          <linearGradient id="incomeGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
            <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
          </linearGradient>
          <linearGradient id="expensesGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#ef4444" stopOpacity={0.3} />
            <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
        <XAxis
          dataKey="period"
          stroke={theme.palette.text.secondary}
          fontSize={12}
        />
        <YAxis
          stroke={theme.palette.text.secondary}
          fontSize={12}
          tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
        />
        <Tooltip
          formatter={(value: number) => formatCurrency(value)}
          contentStyle={{
            backgroundColor: theme.palette.background.paper,
            border: `1px solid ${theme.palette.divider}`,
            borderRadius: 8,
          }}
        />
        <Legend />
        <Area
          type="monotone"
          dataKey="income"
          name="Income"
          stroke="#10b981"
          fill="url(#incomeGradient)"
          strokeWidth={2}
        />
        <Area
          type="monotone"
          dataKey="expenses"
          name="Expenses"
          stroke="#ef4444"
          fill="url(#expensesGradient)"
          strokeWidth={2}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}
