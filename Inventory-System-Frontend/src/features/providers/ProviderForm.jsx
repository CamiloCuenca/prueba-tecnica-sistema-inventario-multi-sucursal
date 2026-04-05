import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import LoadingSpinner from '../../components/LoadingSpinner';
import { useAuth } from '../../context/AuthContext';
import { useProviderForm } from './useProviderForm';

const inputClassName = 'w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20';

export default function ProviderForm({ providerId }) {
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const { mode, form, setField, loadingProvider, submitting, error, fieldErrors, submit } = useProviderForm(providerId);

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

    if (error.status && error.status !== 404) {
      toast.error(error.message);
    }
  }, [error, navigate]);

  useEffect(() => {
    if (!isAdmin) {
      toast.error('No tiene permisos para esta accion');
    }
  }, [isAdmin]);

  if (!isAdmin) {
    return (
      <div className="rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
        No tiene permisos para esta accion.
      </div>
    );
  }

  if (loadingProvider) {
    return <LoadingSpinner label="Cargando proveedor..." />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    const result = await submit();

    if (result?.ok) {
      toast.success(mode === 'edit' ? 'Proveedor actualizado exitosamente' : 'Proveedor creado exitosamente');
      navigate('/providers');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">{mode === 'edit' ? 'Editar proveedor' : 'Nuevo proveedor'}</h1>
        <p className="mt-1 text-sm text-gray-600">
          Completa la información básica del proveedor y guarda los cambios.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <Field label="Nombre" error={fieldErrors.name} required>
          <input
            type="text"
            value={form.name}
            onChange={(event) => setField('name', event.target.value)}
            className={inputClassName}
          />
        </Field>

        <Field label="Información de contacto" error={fieldErrors.contactInfo}>
          <textarea
            rows="4"
            value={form.contactInfo}
            onChange={(event) => setField('contactInfo', event.target.value)}
            className={inputClassName}
            placeholder="Ej: correo, teléfono, dirección o datos adicionales"
          />
        </Field>

        {error && error.status !== 401 && error.status !== 404 && (
          <div className="rounded border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error.message}</div>
        )}

        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={() => navigate('/providers')}
            className="rounded border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
            disabled={submitting}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="rounded bg-primary px-4 py-2 text-sm font-medium text-white hover:bg-primary-hover disabled:opacity-60"
            disabled={submitting}
          >
            {submitting ? 'Guardando...' : 'Guardar'}
          </button>
        </div>
      </form>
    </div>
  );
}

function Field({ label, error, required = false, children }) {
  return (
    <div>
      <label className="mb-1 block text-sm font-medium text-gray-700">
        {label}{required ? ' *' : ''}
      </label>
      {children}
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}