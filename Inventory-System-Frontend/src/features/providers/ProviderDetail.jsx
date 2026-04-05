import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import ConfirmModal from '../../components/ConfirmModal';
import LoadingSpinner from '../../components/LoadingSpinner';
import ProtectedButton from '../../components/ProtectedButton';
import { deleteProvider, mapProviderApiError } from './providersApi';
import { useProviderDetail } from './useProviderDetail';

const formatDate = (value) => {
  if (!value) return 'Sin dato';
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
};

export default function ProviderDetail({ providerId }) {
  const navigate = useNavigate();
  const { provider, loading, error } = useProviderDetail(providerId);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
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

  const handleDelete = async () => {
    if (!provider?.id) return;

    setDeleteSubmitting(true);
    setDeleteError(null);

    try {
      await deleteProvider(provider.id);
      toast.success('Proveedor eliminado exitosamente');
      navigate('/providers', { replace: true });
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

  if (loading) {
    return <LoadingSpinner label="Cargando detalle del proveedor..." />;
  }

  if (error && error.status === 404) {
    return (
      <div className="rounded border border-yellow-200 bg-yellow-50 px-4 py-3 text-sm text-yellow-800">
        Proveedor no encontrado.
      </div>
    );
  }

  if (!provider) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="mt-1 text-2xl font-bold text-gray-900">Detalle del proveedor</h1>
        </div>

        <div className="flex flex-wrap gap-2">
          <Link
            to={`/providers/${provider.id}/products`}
            state={{ providerName: provider.name }}
            className="rounded border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
          >
            Ver productos
          </Link>
          <ProtectedButton
            requiredRoles={['ADMIN']}
            className="rounded border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
            onClick={() => navigate(`/providers/${provider.id}/edit`)}
          >
            Editar
          </ProtectedButton>
          <ProtectedButton
            requiredRoles={['ADMIN']}
            className="rounded bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
            onClick={() => setDeleteModalOpen(true)}
          >
            Borrar
          </ProtectedButton>
        </div>
      </div>

      <div className="grid gap-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm md:grid-cols-2">
        <DetailItem label="Nombre" value={provider.name} />
        <DetailItem label="Información de contacto" value={provider.contactInfo || 'Sin dato'} />
        <DetailItem label="Creado" value={formatDate(provider.createdAt)} />
      </div>

      <ConfirmModal
        open={deleteModalOpen}
        title="Confirmar borrado"
        description={`¿Seguro que desea eliminar el proveedor ${provider.name}? Esta acción no se puede deshacer.`}
        confirmLabel="Eliminar"
        submitting={deleteSubmitting}
        error={deleteError}
        onClose={() => setDeleteModalOpen(false)}
        onConfirm={handleDelete}
      />
    </div>
  );
}

function DetailItem({ label, value }) {
  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wide text-gray-500">{label}</p>
      <p className="mt-1 text-sm text-gray-900">{value}</p>
    </div>
  );
}