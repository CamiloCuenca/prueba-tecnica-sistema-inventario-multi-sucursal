import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ProtectedButton from '../../components/ProtectedButton';
import ConfirmModal from '../../components/ConfirmModal';
import LoadingSpinner from '../../components/LoadingSpinner';
import TablePaginator from '../../components/TablePaginator';
import { deleteProvider, mapProviderApiError } from './providersApi';
import { useProviderList } from './useProviderList';
import { toast } from 'sonner';

export default function ProviderList() {
  const navigate = useNavigate();
  const { providers, pageInfo, page, setPage, search, setSearch, loading, error, refetch } = useProviderList();
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [providerToDelete, setProviderToDelete] = useState(null);
  const [deleteSubmitting, setDeleteSubmitting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  useEffect(() => {
    if (!error) return;

    if (error.status === 401) {
      toast.error(error.message);
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('authToken');
      window.dispatchEvent(new Event('auth-token-updated'));
      navigate('/login', { replace: true });
      return;
    }

    toast.error(error.message);
  }, [error, navigate]);

  const openDeleteModal = (provider) => {
    setProviderToDelete(provider);
    setDeleteError(null);
    setDeleteModalOpen(true);
  };

  const closeDeleteModal = () => {
    setDeleteModalOpen(false);
    setProviderToDelete(null);
    setDeleteError(null);
  };

  const handleDelete = async () => {
    if (!providerToDelete?.id) return;

    setDeleteSubmitting(true);
    setDeleteError(null);

    try {
      await deleteProvider(providerToDelete.id);
      toast.success('Proveedor eliminado exitosamente');
      closeDeleteModal();
      await refetch();
    } catch (err) {
      const mapped = mapProviderApiError(err, 'No se pudo eliminar el proveedor');
      setDeleteError(mapped.message);

      if (mapped.status === 401) {
        toast.error(mapped.message);
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('authToken');
        window.dispatchEvent(new Event('auth-token-updated'));
        navigate('/login', { replace: true });
        return;
      }

      toast.error(mapped.message);
    } finally {
      setDeleteSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Proveedores</h1>
          <p className="mt-1 text-sm text-gray-600">Listado paginado de proveedores registrados en el sistema.</p>
        </div>

        <ProtectedButton
          requiredRoles={['ADMIN']}
          className="rounded bg-primary px-4 py-2 text-sm font-medium text-white hover:bg-primary-hover"
          onClick={() => navigate('/providers/new')}
        >
          Nuevo proveedor
        </ProtectedButton>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="w-full sm:max-w-md">
            <label className="mb-1 block text-sm font-medium text-gray-700">Buscar por nombre o información de contacto</label>
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Nombre o contacto"
              className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          <button
            type="button"
            onClick={() => refetch()}
            className="self-start rounded border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
          >
            Refrescar
          </button>
        </div>

        {loading ? (
          <LoadingSpinner label="Cargando proveedores..." />
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Nombre</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Información de contacto</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Creado</th>
                  <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-600">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {providers.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-4 py-8 text-center text-sm text-gray-500">
                      No hay proveedores para mostrar.
                    </td>
                  </tr>
                ) : (
                  providers.map((provider) => (
                    <tr key={provider.id} className="hover:bg-gray-50">
                      <td className="px-4 py-4 align-top">
                        <div className="font-medium text-gray-900">{provider.name}</div>
                      </td>
                      <td className="px-4 py-4 align-top text-sm text-gray-700 whitespace-pre-line">
                        {provider.contactInfo || 'Sin información de contacto'}
                      </td>
                      <td className="px-4 py-4 align-top text-sm text-gray-700">
                        {provider.createdAt ? new Date(provider.createdAt).toLocaleDateString('es-CO') : 'Sin dato'}
                      </td>
                      <td className="px-4 py-4 text-right align-top">
                        <div className="flex flex-wrap justify-end gap-2">
                          <Link
                            to={`/providers/${provider.id}`}
                            className="rounded border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
                          >
                            Ver
                          </Link>
                          <ProtectedButton
                            requiredRoles={['ADMIN']}
                            className="rounded border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
                            onClick={() => navigate(`/providers/${provider.id}/edit`)}
                          >
                            Editar
                          </ProtectedButton>
                          <ProtectedButton
                            requiredRoles={['ADMIN']}
                            className="rounded bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700"
                            onClick={() => openDeleteModal(provider)}
                          >
                            Borrar
                          </ProtectedButton>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        <TablePaginator
          page={page}
          totalPages={pageInfo.totalPages}
          onPageChange={setPage}
          isFirst={pageInfo.isFirst}
          isLast={pageInfo.isLast}
        />
      </div>

      <ConfirmModal
        open={deleteModalOpen}
        title="Confirmar borrado"
        description={
          providerToDelete
            ? `¿Seguro que desea eliminar el proveedor ${providerToDelete.name}? Esta acción no se puede deshacer.`
            : ''
        }
        confirmLabel="Eliminar"
        submitting={deleteSubmitting}
        error={deleteError}
        onClose={closeDeleteModal}
        onConfirm={handleDelete}
      />
    </div>
  );
}