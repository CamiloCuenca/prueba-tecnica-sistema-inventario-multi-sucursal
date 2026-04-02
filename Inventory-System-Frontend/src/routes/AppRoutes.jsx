import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import DashboardPage from "../pages/DashboardPage";
import InventarioPage from "../pages/InventarioPage";
import Sidebar from "../components/Sidebar";

function MainLayout({ children }) {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="flex-1 bg-gray-50 dark:bg-neutral-950 p-6">{children}</main>
    </div>
  );
}

const AppRoutes = () => (
  <Router>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/dashboard"
        element={
          <MainLayout>
            <DashboardPage />
          </MainLayout>
        }
      />
      <Route
        path="/inventario"
        element={
          <MainLayout>
            <InventarioPage />
          </MainLayout>
        }
      />
      {/* Redirige la raíz al login */}
      <Route path="/" element={<Navigate to="/login" replace />} />
    </Routes>
  </Router>
);

export default AppRoutes;
