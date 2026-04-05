
import { decodeJWT } from '../utils/jwt';
import { UsersTabs } from '../features/users';
import { getRoleFromToken } from '../utils/tokenUtils';

export default function UsersPage() {
	const token = sessionStorage.getItem('token') || sessionStorage.getItem('authToken');
	const payload = decodeJWT(token);
	const role = getRoleFromToken() || payload?.role || null;

	if (role !== 'ADMIN') {
		return (
			<div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
				No tienes permisos para esta accion.
			</div>
		);
	}

	return (
		<div className="p-6">
			<h1 className="text-2xl font-bold mb-4 text-text">Usuarios</h1>
			<UsersTabs />
		</div>
	);
}
