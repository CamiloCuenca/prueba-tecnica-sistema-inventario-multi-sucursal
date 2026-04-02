


import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import IconButton from "@mui/material/IconButton";
import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";
import { useLogin } from "./useLogin";
import { decodeJWT } from "../../utils/jwt";



const LoginForm = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [form, setForm] = useState({ email: "", password: "" });
  const { handleLogin, loading, error } = useLogin();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const data = await handleLogin(form);
    if (data && data.token) {
      const payload = decodeJWT(data.token);
      
        navigate("/dashboard");
     
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-neutral-950 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white dark:bg-neutral-900 p-8 rounded-lg shadow-lg">
        <div>
          <h2 className="mt-2 text-center text-3xl font-extrabold text-gray-900 dark:text-white">Iniciar Sesión</h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="rounded-md shadow-sm -space-y-px">
            <div className="mb-4">
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-200">Correo electrónico</label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={form.email}
                onChange={handleChange}
                className="appearance-none rounded relative block w-full px-3 py-2 border border-gray-300 dark:border-neutral-700 placeholder-gray-500 dark:placeholder-neutral-400 text-gray-900 dark:text-white bg-white dark:bg-neutral-800 focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm"
                placeholder="correo@ejemplo.com"
                disabled={loading}
              />
            </div>
            <div className="mb-6 relative">
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-200">Contraseña</label>
              <div className="relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  autoComplete="current-password"
                  required
                  value={form.password}
                  onChange={handleChange}
                  className="appearance-none rounded relative block w-full px-3 py-2 border border-gray-300 dark:border-neutral-700 placeholder-gray-500 dark:placeholder-neutral-400 text-gray-900 dark:text-white bg-white dark:bg-neutral-800 focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm pr-10"
                  placeholder="••••••••"
                  style={{ paddingRight: 40 }}
                  disabled={loading}
                />
                <IconButton
                  onClick={() => setShowPassword((v) => !v)}
                  edge="end"
                  aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                  className="!absolute right-2 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-300"
                  tabIndex={-1}
                >
                  {showPassword ? <VisibilityOff /> : <Visibility />}
                </IconButton>
              </div>
            </div>
          </div>
          {error && <div className="text-red-500 text-sm mb-2">{error}</div>}
          <div>
            <button
              type="submit"
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md bg-primary text-white hover:bg-primary-dark focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
              disabled={loading}
            >
              {loading ? "Cargando..." : "Ingresar"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoginForm;
