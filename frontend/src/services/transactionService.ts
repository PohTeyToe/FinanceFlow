import api from './api';
import type {
  Transaction,
  DepositRequest,
  WithdrawRequest,
  TransferRequest,
  TransactionFilters,
  PagedResponse,
} from '../types';

export const transactionService = {
  async getTransactions(filters?: TransactionFilters): Promise<PagedResponse<Transaction>> {
    const params = new URLSearchParams();
    
    if (filters) {
      if (filters.accountId) params.append('accountId', filters.accountId.toString());
      if (filters.type) params.append('type', filters.type);
      if (filters.category) params.append('category', filters.category);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.page !== undefined) params.append('page', filters.page.toString());
      if (filters.size !== undefined) params.append('size', filters.size.toString());
    }

    const response = await api.get<PagedResponse<Transaction>>(`/transactions?${params.toString()}`);
    return response.data;
  },

  async getTransaction(id: number): Promise<Transaction> {
    const response = await api.get<Transaction>(`/transactions/${id}`);
    return response.data;
  },

  async deposit(data: DepositRequest): Promise<Transaction> {
    const response = await api.post<Transaction>('/transactions/deposit', data);
    return response.data;
  },

  async withdraw(data: WithdrawRequest): Promise<Transaction> {
    const response = await api.post<Transaction>('/transactions/withdraw', data);
    return response.data;
  },

  async transfer(data: TransferRequest): Promise<Transaction> {
    const response = await api.post<Transaction>('/transactions/transfer', data);
    return response.data;
  },
};

export default transactionService;
