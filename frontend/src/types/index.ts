// User types
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

// Account types
export enum AccountType {
  CHECKING = 'CHECKING',
  SAVINGS = 'SAVINGS',
  CREDIT = 'CREDIT',
  INVESTMENT = 'INVESTMENT',
}

export interface Account {
  id: number;
  userId: number;
  accountNumber: string;
  accountName: string;
  accountType: AccountType;
  balance: number;
  currency: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAccountRequest {
  accountName: string;
  accountType: AccountType;
  currency?: string;
  initialDeposit?: number;
}

export interface UpdateAccountRequest {
  accountName: string;
}

// Transaction types
export enum TransactionType {
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  TRANSFER = 'TRANSFER',
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export interface Transaction {
  id: number;
  accountId: number;
  toAccountId?: number;
  type: TransactionType;
  amount: number;
  currency: string;
  description?: string;
  category?: string;
  status: TransactionStatus;
  referenceNumber: string;
  createdAt: string;
  updatedAt: string;
}

export interface DepositRequest {
  accountId: number;
  amount: number;
  description?: string;
  category?: string;
}

export interface WithdrawRequest {
  accountId: number;
  amount: number;
  description?: string;
  category?: string;
}

export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  description?: string;
}

export interface TransactionFilters {
  accountId?: number;
  type?: TransactionType;
  category?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// Analytics types
export interface SpendingByCategory {
  category: string;
  amount: number;
  percentage: number;
  transactionCount: number;
}

// Wrapper response from backend
export interface SpendingByCategoryResponse {
  startDate: string;
  endDate: string;
  totalSpending: number;
  categories: SpendingByCategory[];
}

export interface MonthlyTrend {
  month: string;
  income: number;
  expenses: number;
  netSavings: number;
}

// Wrapper response from backend
export interface MonthlyTrendResponse {
  numberOfMonths: number;
  months: MonthlyTrend[];
}

export interface AccountInfo {
  accountId: string;
  accountName: string;
  balance: number;
  accountType: string;
}

export interface AccountSummaryResponse {
  totalBalance: number;
  accounts: AccountInfo[];
  thisMonthSpending: number;
  thisMonthIncome: number;
  topSpendingCategory: string;
  comparedToLastMonth: number;
}

// Legacy type for backward compatibility
export interface AccountSummary {
  accountId: number;
  accountName: string;
  accountType: AccountType;
  currentBalance: number;
  totalIncome: number;
  totalExpenses: number;
  transactionCount: number;
}

// Time series data for charts (derived from MonthlyTrend)
export interface IncomeVsExpensesData {
  period: string;
  income: number;
  expenses: number;
  savings: number;
}

// Backend response for income-vs-expenses endpoint (single aggregate)
export interface IncomeVsExpensesResponse {
  startDate: string;
  endDate: string;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  savingsRate: number;
}

// Legacy type for backward compatibility
export interface IncomeVsExpenses {
  period: string;
  income: number;
  expenses: number;
  savings: number;
  savingsRate: number;
}

// API Error
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path?: string;
}
