import { useEffect, useState } from 'react';
import Table from '../../../components/Table';
import TablePaginator from '../../../components/TablePaginator';
import Modal from '../../../components/Modal';
import { usePurchases } from '../usePurchases';
import { getPurchaseById, receivePurchase } from '../purchaseApi';

const getCurrentLocalDateTime = () => {
  const now = new Date();
  const timezoneOffsetMs = now.getTimezoneOffset() * 60000;
  return new Date(now.getTime() - timezoneOffsetMs).toISOString().slice(0, 16);
};

export default function PurchaseTable() {
  const [currentPage, setCurrentPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedPurchase, setSelectedPurchase] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);
  const [showReceiveForm, setShowReceiveForm] = useState(false);
  const [receivedItems, setReceivedItems] = useState({});
  const [receivedAt, setReceivedAt] = useState(() => getCurrentLocalDateTime());
  const [receiveSubmitting, setReceiveSubmitting] = useState(false);
  const [receiveError, setReceiveError] = useState(null);
  const [receiveSuccess, setReceiveSuccess] = useState(null);

  const { purchases, loading, error, pageInfo, refetchPurchases } = usePurchases({
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

  const getErrorMessage = (err, fallbackMessage) => {
    if (typeof err === 'string') return err;
    if (err?.message) return err.message;
    if (err?.error) return err.error;
    return fallbackMessage;
  };

  const initializeReceiveForm = (purchase) => {
    const nextItems = {};

    if (Array.isArray(purchase?.details)) {
      purchase.details.forEach((detail) => {
        if (detail?.id) {
          nextItems[detail.id] = Number(detail.orderedQuantity || 0);
        }
      });
    }

    setReceivedItems(nextItems);
    setReceivedAt(getCurrentLocalDateTime());
    setReceiveError(null);
    setReceiveSuccess(null);
  };

  const resetReceiveForm = () => {
    setShowReceiveForm(false);
    setReceivedItems({});
    setReceivedAt(getCurrentLocalDateTime());
    setReceiveSubmitting(false);
    setReceiveError(null);
    setReceiveSuccess(null);
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
      initializeReceiveForm(data);
    } catch (err) {
      setDetailError(getErrorMessage(err, 'Error al obtener los detalles de la compra'));
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    if (!modalOpen) {
      resetReceiveForm();
    }
  }, [modalOpen]);

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
    resetReceiveForm();
  };

  const handleReceivedQuantityChange = (detailId, value, maxAllowed) => {
    const parsedValue = Number(value);

    if (Number.isNaN(parsedValue)) {
      setReceivedItems((prev) => ({ ...prev, [detailId]: 0 }));
      return;
    }

    const normalizedValue = Math.max(0, Math.min(parsedValue, Number(maxAllowed || 0)));
    setReceivedItems((prev) => ({ ...prev, [detailId]: normalizedValue }));
  };

  const handleSubmitReceive = async (event) => {
    event.preventDefault();

    if (!selectedPurchase?.id) {
      setReceiveError('No se pudo identificar la compra a recibir.');
      return;
    }

    if (!Array.isArray(selectedPurchase.details) || selectedPurchase.details.length === 0) {
      setReceiveError('La compra no tiene detalles para registrar recepción.');
      return;
    }

    const items = selectedPurchase.details.map((detail) => ({
      purchaseDetailId: detail.id,
      quantityReceived: Number(receivedItems[detail.id] ?? 0),
    }));

    if (items.some((item) => !item.purchaseDetailId)) {
      setReceiveError('Hay ítems sin identificador de detalle en la compra.');
      return;
    }

    const payload = {
      items,
      receivedAt: new Date(receivedAt || new Date().toISOString()).toISOString(),
    };

    setReceiveSubmitting(true);
    setReceiveError(null);
    setReceiveSuccess(null);

    try {
      const response = await receivePurchase(selectedPurchase.id, payload);
      const updatedPurchase = response?.id ? response : await getPurchaseById(selectedPurchase.id);

      setSelectedPurchase(updatedPurchase);
      initializeReceiveForm(updatedPurchase);
      setReceiveSuccess('La recepción de la compra se registró correctamente.');
      await refetchPurchases();
    } catch (err) {
      setReceiveError(getErrorMessage(err, 'Error al registrar la recepción de la compra'));
    } finally {
      setReceiveSubmitting(false);
    }
  };

  const rawStatus = selectedPurchase?.status;
  const statusValue = typeof rawStatus === 'string' ? rawStatus : rawStatus?.code || rawStatus?.name || '';
  const normalizedStatus = String(statusValue).toUpperCase();
  const canReceivePurchase = ['PENDING', 'PARTIALLY_RECEIVED'].includes(normalizedStatus);
  const purchaseModalClassName = showReceiveForm
    ? 'min-w-[320px] w-[96vw] max-w-[1400px] max-h-[92vh] overflow-y-auto'
    : 'min-w-[320px] w-[92vw] max-w-4xl max-h-[90vh] overflow-y-auto';

  useEffect(() => {
    if (!canReceivePurchase && showReceiveForm) {
      setShowReceiveForm(false);
    }
  }, [canReceivePurchase, showReceiveForm]);

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

      <Modal open={modalOpen} onClose={closeModal} contentClassName={purchaseModalClassName}>
        {detailLoading && <div className="text-center py-4 text-gray-500">Cargando detalles...</div>}

        {detailError && !detailLoading && <div className="text-center py-4 text-red-500">{detailError}</div>}

        {selectedPurchase && !detailLoading && !detailError && (
          <div className="space-y-5">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <h2 className="text-xl font-bold text-gray-900">Detalle de Compra</h2>
                <p className="text-sm text-gray-500">ID: {selectedPurchase.id}</p>
              </div>

              <button
                type="button"
                onClick={() => {
                  if (!canReceivePurchase) return;
                  setShowReceiveForm((prev) => !prev);
                  setReceiveError(null);
                  setReceiveSuccess(null);
                }}
                className={`rounded px-4 py-2 text-sm font-medium ${
                  canReceivePurchase ? 'bg-primary text-white' : 'bg-gray-200 text-gray-500 cursor-not-allowed'
                }`}
                disabled={!canReceivePurchase}
                title={!canReceivePurchase ? 'Solo se puede registrar llegada para compras en estado PENDING' : ''}
              >
                {showReceiveForm ? 'Ocultar recepción' : 'Registrar llegada'}
              </button>
            </div>

            <div className={showReceiveForm ? 'grid grid-cols-1 lg:grid-cols-2 gap-6' : 'space-y-5'}>
              <div className="space-y-5">
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

              {showReceiveForm && (
                <div className="border rounded-lg p-4 bg-gray-50">
                  <h3 className="text-base font-semibold text-gray-900 mb-4">Registrar recepción</h3>

                  <form className="space-y-4" onSubmit={handleSubmitReceive}>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Fecha y hora de recepción</label>
                      <input
                        type="datetime-local"
                        value={receivedAt}
                        onChange={(event) => setReceivedAt(event.target.value)}
                        className="w-full border rounded p-2"
                        required
                      />
                    </div>

                    <div>
                      <p className="text-sm font-medium text-gray-700 mb-2">Cantidades recibidas por producto</p>

                      {Array.isArray(selectedPurchase.details) && selectedPurchase.details.length > 0 ? (
                        <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
                          {selectedPurchase.details.map((detail) => (
                            <div key={detail.id} className="border rounded p-3 bg-white">
                              <p className="text-sm font-medium text-gray-900">Producto: {detail.productId}</p>
                              <p className="text-xs text-gray-500 mb-2">Cantidad solicitada: {detail.orderedQuantity}</p>

                              <label className="block text-xs text-gray-600 mb-1">Cantidad recibida</label>
                              <input
                                type="number"
                                min={0}
                                max={Number(detail.orderedQuantity || 0)}
                                step="1"
                                value={receivedItems[detail.id] ?? 0}
                                onChange={(event) =>
                                  handleReceivedQuantityChange(detail.id, event.target.value, detail.orderedQuantity)
                                }
                                className="w-full border rounded p-2"
                                required
                              />
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-sm text-gray-500">No hay ítems para registrar recepción.</p>
                      )}
                    </div>

                    {receiveError && <p className="text-sm text-red-500">{receiveError}</p>}
                    {receiveSuccess && <p className="text-sm text-green-600">{receiveSuccess}</p>}

                    <div className="flex justify-end">
                      <button
                        type="submit"
                        disabled={receiveSubmitting}
                        className="rounded px-4 py-2 bg-primary text-white text-sm font-medium disabled:opacity-60"
                      >
                        {receiveSubmitting ? 'Registrando...' : 'Confirmar recepción'}
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
