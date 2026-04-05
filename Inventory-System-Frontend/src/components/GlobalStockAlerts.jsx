import { useEffect, useRef } from "react";
import { Toaster, toast } from "sonner";
import { api } from "../api/client";
import { decodeJWT } from "../utils/jwt";

const POLL_INTERVAL_MS = 5 * 60 * 1000;
const ALLOWED_ROLES = new Set(["ADMIN", "MANAGER"]);

function getAuthPayload() {
  const token = sessionStorage.getItem("token") || sessionStorage.getItem("authToken");
  if (!token) return null;

  const payload = decodeJWT(token);
  if (!payload) return null;

  const nowInSeconds = Math.floor(Date.now() / 1000);
  if (typeof payload.exp === "number" && payload.exp <= nowInSeconds) {
    return null;
  }

  return payload;
}

function getRole(payload) {
  const rawRole = payload?.role || payload?.authorities?.[0] || "";
  if (!rawRole) return null;
  return String(rawRole).replace(/^ROLE_/, "").toUpperCase();
}

function buildAlertKey(alert) {
  const productId = alert?.productId ?? alert?.id ?? alert?.sku ?? "unknown";
  const branchId = alert?.branchId ?? alert?.warehouseId ?? "global";
  return `${productId}:${branchId}`;
}

function getAlertList(responseData) {
  if (Array.isArray(responseData)) return responseData;
  if (Array.isArray(responseData?.content)) return responseData.content;
  return [];
}

export default function GlobalStockAlerts() {
  const dismissedAlertsRef = useRef(new Set());
  const visibleAlertsRef = useRef(new Set());

  useEffect(() => {
    let cancelled = false;

    const fetchLowStockAlerts = async () => {
      const payload = getAuthPayload();
      const role = getRole(payload);

      if (!payload || !ALLOWED_ROLES.has(role)) {
        return;
      }

      try {
        const response = await api.get("/api/metrics/low-stock-alerts");
        const alerts = getAlertList(response.data);

        alerts.forEach((alert) => {
          const alertKey = buildAlertKey(alert);
          if (
            dismissedAlertsRef.current.has(alertKey) ||
            visibleAlertsRef.current.has(alertKey)
          ) {
            return;
          }

          visibleAlertsRef.current.add(alertKey);

          const productName = alert?.productName || "Producto sin nombre";
          const currentStock = alert?.currentStock ?? alert?.stockActual ?? "-";

          const toastId = toast.custom(
            () => (
              <div className="w-full max-w-sm rounded-md border border-amber-200 bg-white p-4 shadow-lg">
                <p className="text-sm font-semibold text-gray-900">Stock bajo detectado</p>
                <p className="mt-1 text-sm text-gray-700">Producto: {productName}</p>
                <p className="mt-1 text-sm text-gray-700">Stock actual: {currentStock}</p>
                <button
                  type="button"
                  className="mt-3 rounded bg-gray-900 px-3 py-1.5 text-xs font-medium text-white hover:bg-black"
                  onClick={() => toast.dismiss(toastId)}
                >
                  Cerrar
                </button>
              </div>
            ),
            {
              duration: Infinity,
              position: "top-right",
              onDismiss: () => {
                visibleAlertsRef.current.delete(alertKey);
                dismissedAlertsRef.current.add(alertKey);
              },
              onAutoClose: () => {
                visibleAlertsRef.current.delete(alertKey);
              },
            }
          );
        });
      } catch (error) {
        if (!cancelled) {
          // Silencia errores de polling para no bloquear la UI.
          console.error("Error obteniendo alertas de stock bajo:", error);
        }
      }
    };

    fetchLowStockAlerts();
    const intervalId = setInterval(fetchLowStockAlerts, POLL_INTERVAL_MS);

    const handleAuthTokenUpdated = () => {
      fetchLowStockAlerts();
    };

    window.addEventListener("auth-token-updated", handleAuthTokenUpdated);

    return () => {
      cancelled = true;
      clearInterval(intervalId);
      window.removeEventListener("auth-token-updated", handleAuthTokenUpdated);
    };
  }, []);

  return <Toaster position="top-right" richColors />;
}