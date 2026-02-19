import api from './api';
import type { Account, CreateAccountRequest, UpdateAccountRequest } from '../types';

export const accountService = {
  async getAccounts(): Promise<Account[]> {
    const response = await api.get<Account[]>('/accounts');
    return response.data;
  },

  async getAccount(id: number): Promise<Account> {
    const response = await api.get<Account>(`/accounts/${id}`);
    return response.data;
  },

  async getBalance(id: number): Promise<{ balance: number; currency: string }> {
    const response = await api.get<{ balance: number; currency: string }>(`/accounts/${id}/balance`);
    return response.data;
  },

  async createAccount(data: CreateAccountRequest): Promise<Account> {
    const response = await api.post<Account>('/accounts', data);
    return response.data;
  },

  async updateAccount(id: number, data: UpdateAccountRequest): Promise<Account> {
    const response = await api.put<Account>(`/accounts/${id}`, data);
    return response.data;
  },

  async deleteAccount(id: number): Promise<void> {
    await api.delete(`/accounts/${id}`);
  },
};

export default accountService;
