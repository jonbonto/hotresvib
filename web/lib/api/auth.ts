import { fetcher } from './client';
import type { 
  LoginRequest, 
  RegisterRequest, 
  AuthResponse,
  User
} from '../types/user';

export async function login(data: LoginRequest): Promise<AuthResponse> {
  return fetcher<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  return fetcher<AuthResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function getCurrentUser(): Promise<User> {
  return fetcher<User>('/users/me');
}

export async function refreshToken(): Promise<{ token: string }> {
  return fetcher<{ token: string }>('/auth/refresh', {
    method: 'POST',
  });
}
