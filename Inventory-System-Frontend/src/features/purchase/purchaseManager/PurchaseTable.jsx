import { useState } from 'react';
import Table from '../../../components/Table';
import TablePaginator from '../../../components/TablePaginator';
import Modal from '../../../components/Modal';
import { usePurchases } from '../usePurchases';
import { getPurchaseById } from '../purchaseApi';

export default function PurchaseTable() {
  const [currentPage, setCurrentPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedPurchase, setSelectedPurchase] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);

  const { purchases, loading, error, pageInfo } = usePurchases({
    page: currentPage,
    size: 20,
    status: statusFilter,
  });

  const statusOptions = [
    { value: '', label: 'Todos los estados' },
    { value: 'PENDING', label: 'Pendiente' },
    { value: 'PARTIALLY_RECEIVED', label: 'Parcialmente recibido' },
    { value: 'RECEIVED', label: 'Recibido' },
    { value: 'CANCELLED', label: 'Cancelado' },
  ];

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handleStatusChange = (event) => {
    setStatusFilter(event.target.value);
    setCurrentPage(0);
  };

  const handleShowDetails = async ({ purchaseId }) => {
    if (!purchaseId) return;

    setModalOpen(true);
    setDetailLoading(true);
    setDetailError(null);
    setSelectedPurchase(null);

    try {
      const data = await getPurchaseById(purchaseId);
      setSelectedPurchase(data);
    } catch (err) {
      setDetailError(err?.message || 'Error al obtener los detalles de la compra');
    } finally {
      setDetailLoading(false);
    }
  };

  const transformedData = purchases.map((purchase) => ({
    id: purchase.id || '-',
    supplier: purchase.supplier || purchase.provider_id || purchase.providerId || 'Sin proveedor',
    status: purchase.status?.name || 'Desconocido',
    subtotal: `S/. ${Number(purchase.subtotal || 0).toFixed(2)}`,
    tax: `S/. ${Number(purchase.tax || 0).toFixed(2)}`,
    total: `S/. ${Number(purchase.total || 0).toFixed(2)}`,
    createdAt: purchase.createdAt ? new Date(purchase.createdAt).toLocaleDateString('es-PE') : '-',
  }));

  const formatCurrency = (value) => `S/. ${Number(value || 0).toFixed(2)}`;

  const closeModal = () => {
    setModalOpen(false);
    setSelectedPurchase(null);
    setDetailError(null);
    setDetailLoading(false);
  };

  return (
    <div className="w-full space-y-6">
      

      {purchases.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white p-4 rounded shadow">
            <p className="text-sm text-gray-600">Total de Compras</p>
            <p className="text-2xl font-bold text-blue-600">{pageInfo.totalElements}</p>
          </div>
          <div className="bg-white p-4 rounded shadow">
            <p className="text-sm text-gray-600">En esta página</p>
            <p className="text-2xl font-bold text-green-600">{purchases.length}</p>
          </div>
          <div className="bg-white p-4 rounded shadow">
            <p className="text-sm text-gray-600">Monto Total</p>
            <p className="text-2xl font-bold text-orange-600">
              {formatCurrency(purchases.reduce((sum, purchase) => sum + Number(purchase.total || 0), 0))}
            </p>
          </div>
        </div>
      )}


      <div className="bg-white p-4 rounded shadow flex flex-col sm:flex-row sm:items-end gap-4">
        <div className="w-full sm:w-72">
          <label className="block text-sm font-semibold text-gray-700 mb-2">Filtrar por estado</label>
          <select
            value={statusFilter}
            onChange={handleStatusChange}
            className="w-full border rounded p-2 focus:outline-none focus:ring-2"
          >
            {statusOptions.map((option) => (
              <option key={option.value || 'ALL'} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="bg-white rounded shadow overflow-hidden">
        {loading && purchases.length === 0 ? (
          <div className="w-full text-center py-8 text-gray-500">Cargando compras...</div>
        ) : error ? (
          <div className="w-full text-center py-8 text-red-500">{error}</div>
        ) : purchases.length > 0 ? (
          <>
            <Table
              data={transformedData}
              onAction={handleShowDetails}
              actionKey="id"
              actionPayload={(row) => ({ purchaseId: row.id })}
            />

            <TablePaginator
              page={pageInfo.currentPage}
              totalPages={pageInfo.totalPages}
              onPageChange={handlePageChange}
              isFirst={pageInfo.isFirst}
              isLast={pageInfo.isLast}
            />
          </>
        ) : (
          <div className="p-8 text-center text-gray-600">
            <p className="text-lg font-medium">No hay compras registradas</p>
            <p className="text-sm mt-2">Las compras aparecerán aquí cuando se creen</p>
          </div>
        )}
      </div>

      <Modal open={modalOpen} onClose={closeModal}>
        {detailLoading && <div className="text-center py-4 text-gray-500">Cargando detalles...</div>}

        {detailError && !detailLoading && <div className="text-center py-4 text-red-500">{detailError}</div>}

        {selectedPurchase && !detailLoading && !detailError && (
          <div className="space-y-5">
            <div>
              <h2 className="text-xl font-bold text-gray-900">Detalle de Compra</h2>
              <p className="text-sm text-gray-500">ID: {selectedPurchase.id}</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-gray-500">Proveedor</p>
                <p className="font-medium text-gray-900">
                  {selectedPurchase.supplier || selectedPurchase.provider_id || selectedPurchase.providerId || 'Sin proveedor'}
                </p>
              </div>
              <div>
                <p className="text-gray-500">Estado</p>
                <p className="font-medium text-gray-900">{selectedPurchase.status?.name || 'Desconocido'}</p>
              </div>
              <div>
                <p className="text-gray-500">Sucursal</p>
                <p className="font-medium text-gray-900">{selectedPurchase.branchId || '-'}</p>
              </div>
              <div>
                <p className="text-gray-500">Fecha de creación</p>
                <p className="font-medium text-gray-900">
                  {selectedPurchase.createdAt ? new Date(selectedPurchase.createdAt).toLocaleString('es-PE') : '-'}
                </p>
              </div>
              <div>
                <p className="text-gray-500">Subtotal</p>
                <p className="font-medium text-gray-900">{formatCurrency(selectedPurchase.subtotal)}</p>
              </div>
              <div>
                <p className="text-gray-500">Impuesto</p>
                <p className="font-medium text-gray-900">{formatCurrency(selectedPurchase.tax)}</p>
              </div>
              <div>
                <p className="text-gray-500">Descuento total</p>
                <p className="font-medium text-gray-900">{formatCurrency(selectedPurchase.discountTotal)}</p>
              </div>
              <div>
                <p className="text-gray-500">Total</p>
                <p className="font-medium text-gray-900">{formatCurrency(selectedPurchase.total)}</p>
              </div>
            </div>

            <div>
              <p className="text-sm text-gray-500 mb-2">Detalles</p>
              {Array.isArray(selectedPurchase.details) && selectedPurchase.details.length > 0 ? (
                <div className="overflow-x-auto border rounded-lg">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="text-left px-3 py-2">Producto</th>
                        <th className="text-left px-3 py-2">Cantidad</th>
                        <th className="text-left px-3 py-2">Precio Unit.</th>
                        <th className="text-left px-3 py-2">Descuento</th>
                        <th className="text-left px-3 py-2">Total línea</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedPurchase.details.map((detail) => (
                        <tr key={detail.id} className="border-t">
                          <td className="px-3 py-2">{detail.productId}</td>
                          <td className="px-3 py-2">{detail.orderedQuantity}</td>
                          <td className="px-3 py-2">{formatCurrency(detail.unitPrice)}</td>
                          <td className="px-3 py-2">{formatCurrency(detail.discount)}</td>
                          <td className="px-3 py-2">{formatCurrency(detail.lineTotal)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="text-sm text-gray-500">No hay detalles disponibles para esta compra.</div>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
