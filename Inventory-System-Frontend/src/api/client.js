import axios from "axios";

export const api = axios.create({
  baseURL: "http://localhost:8080",
});

// Interceptor para adjuntar el token automáticamente
api.interceptors.request.use(
  (config) => {
    // Obtenemos el token (usualmente de sessionStorage o un estado global)
    const token = sessionStorage.getItem("token");

    // Si existe el token, lo añadimos a los headers
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);