export type Role = "CUSTOMER" | "ADMIN";

export type User = {
  id: number;
  email: string;
  fullName: string;
  role: Role;
};

export type AuthResponse = {
  token: string;
  user: User;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  fullName: string;
  email: string;
  password: string;
};

export type HealthResponse = {
  status: string;
  service: string;
  timestamp: string;
};

export type ApiError = {
  message: string;
  errors?: Record<string, string>;
};
