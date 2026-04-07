import { createContext, useContext, useEffect, useState } from "react";
import { decodeJWT } from "../utils/jwt";

const AuthContext = createContext();

const readAuthState = () => {
  const token = sessionStorage.getItem("token") || sessionStorage.getItem("authToken") || null;
  const payload = decodeJWT(token);
  const rawRole = payload?.role || payload?.authorities?.[0] || null;
  const role = rawRole ? String(rawRole).replace(/^ROLE_/, "").toUpperCase() : null;

  return {
    token,
    role,
    payload,
    isAdmin: role === "ADMIN",
  };
};

export function AuthProvider({ children }) {
  const [authState, setAuthState] = useState(readAuthState);

  useEffect(() => {
    const syncAuthState = () => setAuthState(readAuthState());

    window.addEventListener("auth-token-updated", syncAuthState);
    return () => window.removeEventListener("auth-token-updated", syncAuthState);
  }, []);

  const saveToken = (newToken) => {
    if (newToken) {
      sessionStorage.setItem("token", newToken);
      sessionStorage.removeItem("authToken");
    } else {
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("authToken");
    }

    window.dispatchEvent(new Event("auth-token-updated"));
  };

  return <AuthContext.Provider value={{ ...authState, setToken: saveToken, clearAuth: () => saveToken(null) }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

export default AuthContext;
