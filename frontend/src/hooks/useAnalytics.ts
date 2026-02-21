import { useQuery } from '@tanstack/react-query';
import analyticsService from '../services/analyticsService';

interface SpendingByCategoryParams {
  accountId?: number;
  startDate?: string;
  endDate?: string;
}

interface MonthlyTrendParams {
  accountId?: number;
  months?: number;
}

export function useSpendingByCategory(params?: SpendingByCategoryParams) {
  return useQuery({
    queryKey: ['analytics', 'spending-by-category', params],
    queryFn: () => analyticsService.getSpendingByCategory(params),
  });
}

export function useMonthlyTrend(params?: MonthlyTrendParams) {
  return useQuery({
    queryKey: ['analytics', 'monthly-trend', params],
    queryFn: () => analyticsService.getMonthlyTrend(params),
  });
}

export function useAccountSummary() {
  return useQuery({
    queryKey: ['analytics', 'summary'],
    queryFn: analyticsService.getSummary,
  });
}

export function useIncomeVsExpenses() {
  return useQuery({
    queryKey: ['analytics', 'income-vs-expenses'],
    queryFn: analyticsService.getIncomeVsExpenses,
  });
}
