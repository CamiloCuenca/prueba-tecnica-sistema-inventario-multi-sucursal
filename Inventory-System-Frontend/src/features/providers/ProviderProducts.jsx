import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import LoadingSpinner from '../../components/LoadingSpinner';
import TablePaginator from '../../components/TablePaginator';
import { useEffect } from 'react';
import { useProviderProducts } from './useProviderProducts';

const moneyFormatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

export default function ProviderProducts({ providerId }) {
  const navigate = useNavigate();
  const { products, pageInfo, page, setPage, search, setSearch, active, setActive, loading, error } = useProviderProducts(providerId);

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

  return (
    <div className="space-y-6">
      <div>
        <h1 className="mt-1 text-2xl font-bold text-gray-900">Productos por proveedor</h1>
        <p className="mt-1 text-sm text-gray-600">Explora los productos asociados a este proveedor.</p>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
        <div className="mb-4 grid gap-3 md:grid-cols-2">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Buscar por nombre o SKU</label>
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Nombre o SKU"
              className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Estado</label>
            <select
              value={active}
              onChange={(event) => setActive(event.target.value)}
              className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            >
              <option value="">Todos</option>
              <option value="true">Activos</option>
              <option value="false">Inactivos</option>
            </select>
          </div>
        </div>

        {loading ? (
          <LoadingSpinner label="Cargando productos..." />
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Nombre</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">SKU</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Categoría</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Stock</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">Precio referencia</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {products.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-8 text-center text-sm text-gray-500">
                      No hay productos para mostrar.
                    </td>
                  </tr>
                ) : (
                  products.map((product) => (
                    <tr key={product.id} className="hover:bg-gray-50">
                      <td className="px-4 py-4 text-sm font-medium text-gray-900">{product.name}</td>
                      <td className="px-4 py-4 text-sm text-gray-700">{product.sku || 'Sin dato'}</td>
                      <td className="px-4 py-4 text-sm text-gray-700">{product.category || 'Sin dato'}</td>
                      <td className="px-4 py-4 text-sm text-gray-700">{product.stock ?? 0}</td>
                      <td className="px-4 py-4 text-sm text-gray-700">
                        {product.defaultReferencePrice != null ? moneyFormatter.format(Number(product.defaultReferencePrice)) : 'Sin dato'}
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
    </div>
  );
}