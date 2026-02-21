import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { Box, Typography, useTheme } from '@mui/material';
import type { SpendingByCategory } from '../../types';

interface SpendingPieChartProps {
  data: SpendingByCategory[];
}

const COLORS = ['#2563eb', '#7c3aed', '#10b981', '#f59e0b', '#ef4444', '#06b6d4', '#ec4899', '#8b5cf6'];

export default function SpendingPieChart({ data }: SpendingPieChartProps) {
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
        <Typography color="text.secondary">No spending data available</Typography>
      </Box>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={350}>
      <PieChart>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          innerRadius={60}
          outerRadius={120}
          paddingAngle={2}
          dataKey="amount"
          nameKey="category"
          label={({ category, percentage }) => `${category}: ${percentage.toFixed(1)}%`}
          labelLine={false}
        >
          {data.map((_, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip
          formatter={(value: number) => formatCurrency(value)}
          contentStyle={{
            backgroundColor: theme.palette.background.paper,
            border: `1px solid ${theme.palette.divider}`,
            borderRadius: 8,
          }}
        />
        <Legend
          verticalAlign="bottom"
          height={36}
          formatter={(value) => (
            <span style={{ color: theme.palette.text.primary }}>{value}</span>
          )}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}
