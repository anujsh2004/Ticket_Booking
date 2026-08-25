import axios from 'axios';

const rawBase = (import.meta.env.VITE_API_URL || '').trim();
const cleanBase = rawBase.replace(/\/+$/, '').replace(/\/api$/, '');
const API_BASE = cleanBase ? `${cleanBase}/api` : '/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000, // 30 seconds timeout
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
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      return Promise.reject(
        new Error('Server took too long to respond. It may be waking up from sleep mode—please try again.')
      );
    }
    if (!error.response && error.request) {
      return Promise.reject(
        new Error('Unable to connect to the backend server. It may still be booting up.')
      );
    }
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);

export default api;

