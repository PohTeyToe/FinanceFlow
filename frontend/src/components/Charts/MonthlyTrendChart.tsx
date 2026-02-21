import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { Box, Typography, useTheme } from '@mui/material';
import type { MonthlyTrend } from '../../types';

interface MonthlyTrendChartProps {
  data: MonthlyTrend[];
}

export default function MonthlyTrendChart({ data }: MonthlyTrendChartProps) {
  const theme = useTheme();

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(value);
  };

  // Month is in "YYYY-MM" format from backend
  const chartData = data.map((item) => ({
    ...item,
    name: item.month,
  }));

  if (data.length === 0) {
    return (
      <Box sx={{ py: 8, textAlign: 'center' }}>
        <Typography color="text.secondary">No trend data available</Typography>
      </Box>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={350}>
      <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
        <XAxis
          dataKey="name"
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
        <Bar dataKey="income" name="Income" fill="#10b981" radius={[4, 4, 0, 0]} />
        <Bar dataKey="expenses" name="Expenses" fill="#ef4444" radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}
