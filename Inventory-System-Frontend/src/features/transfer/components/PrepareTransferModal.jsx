import Modal from '../../../components/Modal';
import { getProductId, getRequestedQuantity, getTransferItems } from '../transferUiUtils';

export default function PrepareTransferModal({
  open,
  onClose,
  loading,
  transfer,
  confirmedItems,
  onQuantityChange,
  onSubmit,
  error,
  success,
  submitting,
}) {
  return (
    <Modal
      open={open}
      onClose={onClose}
      contentClassName="min-w-[320px] w-[94vw] max-w-5xl max-h-[90vh] overflow-y-auto"
    >
      {loading && <div className="text-center py-8 text-gray-500">Cargando transferencia...</div>}

      {!loading && transfer && (
        <div className="space-y-4">
          <div>
            <h3 className="text-xl font-bold text-gray-900">Preparar Transferencia</h3>
            <p className="text-sm text-gray-500">ID: {transfer.id}</p>
          </div>

          <form onSubmit={onSubmit} className="space-y-4">
            <div className="overflow-x-auto border rounded-lg">
              <table className="w-full text-sm">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="text-left px-3 py-2">Producto</th>
                    <th className="text-left px-3 py-2">Cantidad Solicitada</th>
                    <th className="text-left px-3 py-2">Cantidad a Enviar</th>
                    <th className="text-left px-3 py-2">Ajuste</th>
                  </tr>
                </thead>
                <tbody>
                  {getTransferItems(transfer).map((item) => {
                    const productId = getProductId(item);
                    const requested = getRequestedQuantity(item);
                    const confirmed = Number(confirmedItems[productId] ?? requested);
                    const hasAdjustment = confirmed !== requested;

                    return (
                      <tr key={productId} className={`border-t ${hasAdjustment ? 'bg-amber-50' : ''}`}>
                        <td className="px-3 py-2">{productId}</td>
                        <td className="px-3 py-2">{requested}</td>
                        <td className="px-3 py-2">
                          <input
                            type="number"
                            min={0}
                            step="1"
                            value={confirmed}
                            onChange={(event) => onQuantityChange(productId, event.target.value)}
                            className={`w-28 border rounded p-2 ${hasAdjustment ? 'border-amber-400' : ''}`}
                            required
                          />
                        </td>
                        <td className="px-3 py-2">
                          {hasAdjustment ? (
                            <span className="text-xs font-semibold text-amber-700">Ajustado ({confirmed - requested})</span>
                          ) : (
                            <span className="text-xs text-gray-500">Sin ajuste</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {error && <p className="text-sm text-red-500">{error}</p>}
            {success && <p className="text-sm text-green-600">{success}</p>}

            <div className="flex justify-end">
              <button
                type="submit"
                disabled={submitting}
                className="rounded px-4 py-2 bg-primary text-white text-sm font-medium disabled:opacity-60"
              >
                {submitting ? 'Preparando...' : 'Confirmar preparación'}
              </button>
            </div>
          </form>
        </div>
      )}

      {!loading && !transfer && error && <div className="text-center py-8 text-red-500">{error}</div>}
    </Modal>
  );
}
