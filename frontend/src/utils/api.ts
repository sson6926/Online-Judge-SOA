// Central API client — all requests go through /api/* → Nginx → Gateway → Services
// Never call backend services directly from frontend

const BASE_URL = '/api';

// ─── Auth helpers ────────────────────────────────────────────────────────────

export function getToken(): string | null {
  return localStorage.getItem('token');
}

export function setToken(token: string): void {
  localStorage.setItem('token', token);
}

export function clearToken(): void {
  localStorage.removeItem('token');
}

// ─── Base fetch wrapper ──────────────────────────────────────────────────────

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(error.message || `HTTP ${res.status}`);
  }

  // 204 No Content
  if (res.status === 204) return undefined as T;
  return res.json();
}

// ─── Types ───────────────────────────────────────────────────────────────────

export interface Problem {
  id: string;
  title: string;
  description: string;
  time_limit: number;
  memory_limit: number;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
}

export interface Submission {
  id: string;
  problem_id: string;
  user_id: string;
  code: string;
  language: string;
  status: 'PENDING' | 'RUNNING' | 'FINISHED';
  verdict: 'AC' | 'WA' | 'TLE' | 'MLE' | 'RTE' | 'CE' | 'SE' | null;
  time_used: number | null;
  memory_used: number | null;
  error_details: string | null;
  submitted_at: string;
  finished_at: string | null;
}

export interface LoginResponse {
  token: string;
  user: {
    id: string;
    username: string;
    email: string;
  };
}

// ─── Problem Service ─────────────────────────────────────────────────────────

export const problemApi = {
  getAll: (): Promise<Problem[]> =>
    request('/problems'),

  getById: (id: string): Promise<Problem> =>
    request(`/problems/${id}`),
};

// ─── Submission Service ──────────────────────────────────────────────────────

export const submissionApi = {
  submit: (data: {
    problem_id: string;
    user_id: string;
    code: string;
    language: string;
  }): Promise<Submission> =>
    request('/submissions', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getById: (id: string): Promise<Submission> =>
    request(`/submissions/${id}`),

  getAll: (): Promise<Submission[]> =>
    request('/submissions'),
};

// ─── User Service ────────────────────────────────────────────────────────────

export const userApi = {
  login: (username: string, password: string): Promise<LoginResponse> =>
    request('/users/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),

  register: (username: string, email: string, password: string): Promise<LoginResponse> =>
    request('/users/signup', {
      method: 'POST',
      body: JSON.stringify({ username, email, password }),
    }),
};
