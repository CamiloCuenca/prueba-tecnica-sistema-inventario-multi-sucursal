import { useState } from "react";
import { login } from "./authApi";

export function useLogin() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

 const handleLogin = async (credentials) => {
  setLoading(true);
  setError(null);
  try {
    const data = await login(credentials);
    // Guarda el token en SessionStorage
    if (data.token) {
      sessionStorage.setItem("token", data.token);
      window.dispatchEvent(new Event("auth-token-updated"));
    }
    setLoading(false);
    return data;
  } catch (err) {
    setError(err.response?.data?.message || "Error de autenticación");
    setLoading(false);
    return null;
  }
};

  return { handleLogin, loading, error };
}
