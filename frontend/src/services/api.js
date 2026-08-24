import axios from 'axios';

const rawBase = (import.meta.env.VITE_API_URL || '').trim();
const cleanBase = rawBase.replace(/\/+$/, '').replace(/\/api$/, '');
const API_BASE = cleanBase ? `${cleanBase}/api` : '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Standardize error unwrapping
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);

export default api;
