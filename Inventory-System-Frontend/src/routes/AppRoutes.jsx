import { useState } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import MenuOpenRoundedIcon from "@mui/icons-material/MenuOpenRounded";
import LoginPage from "../pages/LoginPage";
import DashboardPage from "../pages/DashboardPage";
import InventarioPage from "../pages/InventarioPage";
import Sidebar from "../components/Sidebar";
import SalesPage from '../pages/SalesPage';
import PurchasePage from '../pages/PurchasePage';
import TransfersPage from '../pages/TransfersPage';
import UsersPage from '../pages/UsersPage';
import BranchesPage from '../pages/BranchesPage';

function ProtectedRoute({ children }) {
  const token = sessionStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function MainLayout({ children }) {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    sessionStorage.removeItem("token");
    navigate("/login", { replace: true });
  };

  return (
    <div className="flex min-h-screen bg-gray-50 dark:bg-neutral-950">
      <Sidebar collapsed={isSidebarCollapsed} />
      <div className="flex flex-1 min-w-0 flex-col">
        <header className="sticky top-0 z-20 flex items-center justify-between border-b border-gray-200 bg-white/95 px-4 py-3 backdrop-blur-sm dark:border-neutral-800 dark:bg-neutral-900/95 sm:px-6">
          <button
            type="button"
            onClick={() => setIsSidebarCollapsed((prev) => !prev)}
            className="inline-flex items-center gap-2 rounded-md border border-gray-200 px-3 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-100 dark:border-neutral-700 dark:text-gray-200 dark:hover:bg-neutral-800"
            aria-label={isSidebarCollapsed ? "Expandir sidebar" : "Colapsar sidebar"}
          >
            {isSidebarCollapsed ? <MenuRoundedIcon fontSize="small" /> : <MenuOpenRoundedIcon fontSize="small" />}
            <span className="hidden sm:inline">{isSidebarCollapsed ? "Expandir" : "Colapsar"}</span>
          </button>

          <button
            type="button"
            onClick={handleLogout}
            className="inline-flex items-center gap-2 rounded-md bg-primary px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-hover"
            aria-label="Cerrar sesión"
          >
            <LogoutRoundedIcon fontSize="small" />
            <span className="hidden sm:inline">Cerrar sesión</span>
          </button>
        </header>

        <main className="flex-1 p-4 sm:p-6">{children}</main>
      </div>
    </div>
  );
}

const AppRoutes = () => (
  <Router>
    <Routes>
      <Route
        path="/login"
        element={sessionStorage.getItem("token") ? <Navigate to="/dashboard" replace /> : <LoginPage />}
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <MainLayout>
              <DashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/inventario"
        element={
          <ProtectedRoute>
            <MainLayout>
              <InventarioPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/ventas"
        element={
          <ProtectedRoute>
            <MainLayout>
              <SalesPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/compras"
        element={
          <ProtectedRoute>
            <MainLayout>
              <PurchasePage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/transacciones"
        element={
          <ProtectedRoute>
            <MainLayout>
              <TransfersPage/>
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/usuarios"
        element={
          <ProtectedRoute>
            <MainLayout>
              <UsersPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/sucursales"
        element={
          <ProtectedRoute>
            <MainLayout>
              <BranchesPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Redirige la raíz al login */}
      <Route path="/" element={<Navigate to="/login" replace />} />
    </Routes>
  </Router>
);

export default AppRoutes;
