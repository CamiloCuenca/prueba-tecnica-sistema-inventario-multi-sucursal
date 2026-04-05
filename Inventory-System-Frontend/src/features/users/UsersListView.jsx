import TablePaginator from '../../components/TablePaginator';

const formatDateTime = (value) => {
  if (!value) return '-';
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return String(value);
  return parsed.toLocaleString('es-PE');
};

export default function UsersListView({
  users,
  loading,
  error,
  pageInfo,
  search,
  roleFilter,
  onSearchChange,
  onRoleFilterChange,
  onCreate,
  onEdit,
  onDelete,
  onPageChange,
}) {
  return (
    <div className="space-y-4">
      <div className="bg-white rounded-lg shadow p-4 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Buscar</label>
            <input
              type="text"
              placeholder="Nombre o email"
              value={search}
              onChange={(event) => onSearchChange(event.target.value)}
              className="w-full border rounded px-3 py-2"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Rol</label>
            <select
              value={roleFilter}
              onChange={(event) => onRoleFilterChange(event.target.value)}
              className="w-full border rounded px-3 py-2"
            >
              <option value="">Todos</option>
              <option value="ADMIN">ADMIN</option>
              <option value="MANAGER">MANAGER</option>
              <option value="OPERATOR">OPERATOR</option>
            </select>
          </div>
        </div>

        <button
          type="button"
          onClick={onCreate}
          className="rounded bg-primary text-white px-4 py-2 text-sm font-medium"
        >
          Crear usuario
        </button>
      </div>

      <div className="bg-white rounded-lg shadow p-4 space-y-4">
        <h3 className="text-base font-semibold text-gray-900">Lista de usuarios</h3>

        {loading ? (
          <div className="text-center py-8 text-gray-500">Cargando usuarios...</div>
        ) : error ? (
          <div className="text-center py-8 text-red-600">{error}</div>
        ) : users.length === 0 ? (
          <div className="text-center py-8 text-gray-600">No hay usuarios para mostrar.</div>
        ) : (
          <div className="overflow-x-auto border rounded-lg">
            <table className="w-full text-sm">
              <thead className="bg-gray-100">
                <tr>
                  <th className="text-left px-3 py-2">Name</th>
                  <th className="text-left px-3 py-2">Email</th>
                  <th className="text-left px-3 py-2">Role</th>
                  <th className="text-left px-3 py-2">Branch</th>
                  <th className="text-left px-3 py-2">CreatedAt</th>
                  <th className="text-left px-3 py-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id} className="border-t">
                    <td className="px-3 py-2">{user.name || '-'}</td>
                    <td className="px-3 py-2">{user.email || '-'}</td>
                    <td className="px-3 py-2">{user.role || '-'}</td>
                    <td className="px-3 py-2">{user.branchId || '-'}</td>
                    <td className="px-3 py-2 whitespace-nowrap">{formatDateTime(user.createdAt)}</td>
                    <td className="px-3 py-2">
                      <div className="flex gap-2">
                        <button
                          type="button"
                          onClick={() => onEdit(user)}
                          className="rounded px-3 py-1 bg-primary text-white text-xs font-medium"
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => onDelete(user)}
                          className="rounded px-3 py-1 bg-red-600 text-white text-xs font-medium"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <TablePaginator
          page={pageInfo.currentPage}
          totalPages={pageInfo.totalPages}
          onPageChange={onPageChange}
          isFirst={pageInfo.isFirst}
          isLast={pageInfo.isLast}
        />
      </div>
    </div>
  );
}
