import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "../pages/LoginPage";

// Aquí se agregarán más rutas en el futuro
const AppRoutes = () => (
  <Router>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      {/* Redirige la raíz al login */}
      <Route path="/" element={<Navigate to="/login" replace />} />
    </Routes>
  </Router>
);

export default AppRoutes;
