import api from './api';
import type {
  SpendingByCategory,
  SpendingByCategoryResponse,
  MonthlyTrend,
  MonthlyTrendResponse,
  AccountSummaryResponse,
  IncomeVsExpensesResponse,
} from '../types';

interface SpendingByCategoryParams {
  accountId?: number;
  startDate?: string;
  endDate?: string;
}

interface MonthlyTrendParams {
  accountId?: number;
  months?: number;
}

export const analyticsService = {
  async getSpendingByCategory(params?: SpendingByCategoryParams): Promise<SpendingByCategory[]> {
    const searchParams = new URLSearchParams();
    
    if (params) {
      if (params.accountId) searchParams.append('accountId', params.accountId.toString());
      if (params.startDate) searchParams.append('startDate', params.startDate);
      if (params.endDate) searchParams.append('endDate', params.endDate);
    }

    const response = await api.get<SpendingByCategoryResponse>(`/analytics/spending-by-category?${searchParams.toString()}`);
    // Extract the categories array from the wrapper response
    return response.data.categories || [];
  },

  async getMonthlyTrend(params?: MonthlyTrendParams): Promise<MonthlyTrend[]> {
    const searchParams = new URLSearchParams();
    
    if (params) {
      if (params.accountId) searchParams.append('accountId', params.accountId.toString());
      if (params.months) searchParams.append('months', params.months.toString());
    }

    const response = await api.get<MonthlyTrendResponse>(`/analytics/monthly-trend?${searchParams.toString()}`);
    // Extract the months array from the wrapper response
    return response.data.months || [];
  },

  async getSummary(): Promise<AccountSummaryResponse> {
    const response = await api.get<AccountSummaryResponse>('/analytics/summary');
    return response.data;
  },

  async getIncomeVsExpenses(): Promise<IncomeVsExpensesResponse> {
    const response = await api.get<IncomeVsExpensesResponse>('/analytics/income-vs-expenses');
    return response.data;
  },
};

export default analyticsService;
