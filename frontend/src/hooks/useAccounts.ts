import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import accountService from '../services/accountService';
import type { CreateAccountRequest, UpdateAccountRequest } from '../types';

export function useAccounts() {
  return useQuery({
    queryKey: ['accounts'],
    queryFn: accountService.getAccounts,
  });
}

export function useAccount(id: number) {
  return useQuery({
    queryKey: ['accounts', id],
    queryFn: () => accountService.getAccount(id),
    enabled: !!id,
  });
}

export function useAccountBalance(id: number) {
  return useQuery({
    queryKey: ['accounts', id, 'balance'],
    queryFn: () => accountService.getBalance(id),
    enabled: !!id,
  });
}

export function useCreateAccount() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateAccountRequest) => accountService.createAccount(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
    },
  });
}

export function useUpdateAccount() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateAccountRequest }) =>
      accountService.updateAccount(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['accounts', id] });
    },
  });
}

export function useDeleteAccount() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => accountService.deleteAccount(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
    },
  });
}
