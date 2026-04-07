import React, { useState } from 'react';
import Modal from '../../../components/Modal';
import dayjs from 'dayjs';
import { useReceiveTransfer } from '../useReceiveTransfer';


export default function ReceiveTransferModal({ open, onClose, transfer, loading, error }) {
  const [showReceiveForm, setShowReceiveForm] = useState(false);
  const [receivedItems, setReceivedItems] = useState({});
  const { handleReceive, loading: receiveLoading, error: receiveError, success } = useReceiveTransfer();

  // Inicializa cantidades recibidas al abrir el formulario
  React.useEffect(() => {
    if (showReceiveForm && transfer?.items) {
      const initial = {};
      transfer.items.forEach((item) => {
        initial[item.productId] = item.receivedQuantity ?? item.quantityConfirmed ?? 0;
      });
      setReceivedItems(initial);
    }
  }, [showReceiveForm, transfer]);

  const handleReceivedQuantityChange = (productId, value, max) => {
    let val = Number(value);
    if (val < 0) val = 0;
    if (val > max) val = max;
    setReceivedItems((prev) => ({ ...prev, [productId]: val }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!transfer) return;
    const items = transfer.items.map((item) => ({
      productId: item.productId,
      receivedQuantity: Number(receivedItems[item.productId] ?? 0),
    }));
    await handleReceive({ transferId: transfer.id, items });
  };

  return (
    <Modal open={open} onClose={onClose} title={null} maxWidth="max-w-6xl">
      {loading && <div className="text-center py-4 text-gray-500">Cargando detalles...</div>}
      {error && !loading && <div className="text-center py-4 text-red-600">{error}</div>}
      {transfer && !loading && !error && (
        <div className="w-full max-w-5xl mx-auto flex flex-col lg:flex-row gap-6 max-h-[80vh]">
          {/* Info principal */}
          <div className="w-full lg:w-3/5 min-w-0 space-y-5 overflow-auto pb-2" style={{maxHeight: '75vh'}}>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <h2 className="text-xl font-bold text-gray-900">Detalle de Transferencia</h2>
                <p className="text-sm text-gray-500">ID: {transfer.id}</p>
              </div>
              <button
                type="button"
                onClick={() => setShowReceiveForm((prev) => !prev)}
                className={`rounded px-4 py-2 text-sm font-medium ${showReceiveForm ? 'bg-gray-200 text-gray-500' : 'bg-green-600 text-white hover:bg-green-700'}`}
              >
                {showReceiveForm ? 'Ocultar recepción' : 'Confirmar Recepción'}
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-gray-500">Sucursal Origen</p>
                <p className="font-medium text-gray-900">{transfer.originBranchId}</p>
              </div>
              <div>
                <p className="text-gray-500">Sucursal Destino</p>
                <p className="font-medium text-gray-900">{transfer.destinationBranchId}</p>
              </div>
              <div>
                <p className="text-gray-500">Estado</p>
                <p className="font-medium text-gray-900">{transfer.status}</p>
              </div>
              <div>
                <p className="text-gray-500">Transportista</p>
                <p className="font-medium text-gray-900">{transfer.carrier || '-'}</p>
              </div>
              <div>
                <p className="text-gray-500">Prioridad de Ruta</p>
                <p className="font-medium text-gray-900">{transfer.routePriority || '-'}</p>
              </div>
              <div>
                <p className="text-gray-500">Costo de Ruta</p>
                <p className="font-medium text-gray-900">{transfer.routeCost != null ? transfer.routeCost : '-'}</p>
              </div>
              <div>
                <p className="text-gray-500">Fecha Creación</p>
                <p className="font-medium text-gray-900">{transfer.createdAt ? dayjs(transfer.createdAt).format('YYYY-MM-DD HH:mm') : '-'}</p>
              </div>
              <div>
                <p className="text-gray-500">Fecha Despacho</p>
                <p className="font-medium text-gray-900">{transfer.dispatchedAt ? dayjs(transfer.dispatchedAt).format('YYYY-MM-DD HH:mm') : '-'}</p>
              </div>
              <div>
                <p className="text-gray-500">Fecha Estimada de Arribo</p>
                <p className="font-medium text-gray-900">{transfer.estimatedArrival ? dayjs(transfer.estimatedArrival).format('YYYY-MM-DD HH:mm') : '-'}</p>
              </div>
            </div>

            <div>
              <p className="text-sm text-gray-500 mb-2">Productos</p>
              {Array.isArray(transfer.items) && transfer.items.length > 0 ? (
                <div className="overflow-x-auto border rounded-lg">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="text-left px-3 py-2">Producto</th>
                        <th className="text-left px-3 py-2">Solicitados</th>
                        <th className="text-left px-3 py-2">Confirmados</th>
                        <th className="text-left px-3 py-2">Recibidos</th>
                      </tr>
                    </thead>
                    <tbody>
                      {transfer.items.map((item) => (
                        <tr key={item.productId} className="border-t">
                          <td className="px-3 py-2">{item.productName}</td>
                          <td className="px-3 py-2">{item.quantityRequested}</td>
                          <td className="px-3 py-2">{item.quantityConfirmed}</td>
                          <td className="px-3 py-2">{item.receivedQuantity ?? '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="text-sm text-gray-500">No hay productos en esta transferencia.</div>
              )}
            </div>
          </div>

          {/* Formulario de recepción */}
          {showReceiveForm && (
            <div className="w-full lg:w-2/5 min-w-[340px] flex-shrink-0 border rounded-lg p-4 bg-gray-50 overflow-auto" style={{maxHeight: '75vh'}}>
              <h3 className="text-base font-semibold text-gray-900 mb-4">Registrar recepción</h3>
              <form className="space-y-4" onSubmit={handleSubmit}>
                <div>
                  <p className="text-sm font-medium text-gray-700 mb-2">Cantidades recibidas por producto</p>
                  {Array.isArray(transfer.items) && transfer.items.length > 0 ? (
                    <div className="space-y-3 max-h-64 overflow-y-auto pr-1">
                      {transfer.items.map((item) => (
                        <div key={item.productId} className="border rounded p-3 bg-white">
                          <p className="text-sm font-medium text-gray-900">Producto: {item.productName}</p>
                          <p className="text-xs text-gray-500 mb-2">Cantidad confirmada: {item.quantityConfirmed}</p>
                          <label className="block text-xs text-gray-600 mb-1">Cantidad recibida</label>
                          <input
                            type="number"
                            min={0}
                            max={Number(item.quantityConfirmed || 0)}
                            step="1"
                            value={receivedItems[item.productId] ?? 0}
                            onChange={(event) => handleReceivedQuantityChange(item.productId, event.target.value, item.quantityConfirmed)}
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
                {success && <p className="text-sm text-green-600">Recepción registrada correctamente.</p>}
                <div className="flex justify-end">
                  <button
                    type="submit"
                    disabled={receiveLoading}
                    className="rounded px-4 py-2 bg-green-600 text-white text-sm font-medium disabled:opacity-60"
                  >
                    {receiveLoading ? 'Registrando...' : 'Confirmar recepción'}
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}
    </Modal>
  );
}
