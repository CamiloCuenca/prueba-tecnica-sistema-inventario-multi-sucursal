import { api } from "../../api/client";

export const login = async ({ email, password }) => {
  const response = await api.post("/auth/login", { email, password });
  return response.data;
};
