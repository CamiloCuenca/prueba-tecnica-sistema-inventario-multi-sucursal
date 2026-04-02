// Decodifica un JWT sin validar la firma (solo para lectura de payload)
export function decodeJWT(token) {
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    return JSON.parse(atob(payload));
  } catch {
    return null;
  }
}
