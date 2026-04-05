import React from "react";
import AppRoutes from "./routes/AppRoutes";
import { AuthProvider } from "./context/AuthContext";

// App solo compone las rutas, sin lógica ni UI propia
const App = () => (
	<AuthProvider>
		<AppRoutes />
	</AuthProvider>
);

export default App;
