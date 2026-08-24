import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      api.get('/auth/me')
        .then((res) => {
          setUser(res.data);
        })
        .catch(() => {
          logout();
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('token', jwtToken);
    setToken(jwtToken);
    setUser(userData);
    return res;
  };

  const register = async (name, email, password, role) => {
    const res = await api.post('/auth/register', { name, email, password, role });
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('token', jwtToken);
    setToken(jwtToken);
    setUser(userData);
    return res;
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.role === 'ADMIN',
        isOrganiser: user?.role === 'ORGANISER' || user?.role === 'ADMIN',
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
