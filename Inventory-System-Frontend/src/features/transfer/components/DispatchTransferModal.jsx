import Modal from '../../../components/Modal';
import { carrierOptions, routePriorityOptions } from '../transferUiUtils';

export default function DispatchTransferModal({
  open,
  onClose,
  transfer,
  dispatchForm,
  onFieldChange,
  onSubmit,
  submitting,
  error,
}) {
  return (
    <Modal
      open={open}
      onClose={onClose}
      contentClassName="min-w-[320px] w-[92vw] max-w-2xl max-h-[90vh] overflow-y-auto"
    >
      <div className="space-y-4">
        <div>
          <h3 className="text-xl font-bold text-gray-900">Confirmar despacho</h3>
          <p className="text-sm text-gray-500">ID: {transfer?.id || '-'}</p>
        </div>

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Transportista</label>
            <select
              value={dispatchForm.carrier}
              onChange={(event) => onFieldChange('carrier', event.target.value)}
              className="w-full border rounded p-2"
              required
            >
              {carrierOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Llegada estimada</label>
            <input
              type="datetime-local"
              value={dispatchForm.estimatedArrival}
              onChange={(event) => onFieldChange('estimatedArrival', event.target.value)}
              className="w-full border rounded p-2"
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">ID de ruta</label>
              <input
                type="text"
                value={dispatchForm.routeId}
                onChange={(event) => onFieldChange('routeId', event.target.value)}
                className="w-full border rounded p-2"
                placeholder="ARM-PER-01"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Prioridad de ruta</label>
              <select
                value={dispatchForm.routePriority}
                onChange={(event) => onFieldChange('routePriority', event.target.value)}
                className="w-full border rounded p-2"
                required
              >
                {routePriorityOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Costo de ruta</label>
              <input
                type="number"
                min={0.01}
                step="0.01"
                value={dispatchForm.routeCost}
                onChange={(event) => onFieldChange('routeCost', event.target.value)}
                className="w-full border rounded p-2"
                required
              />
            </div>
          </div>

          {error && <p className="text-sm text-red-500">{error}</p>}

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="rounded px-4 py-2 bg-primary text-white text-sm font-medium disabled:opacity-60"
            >
              {submitting ? 'Despachando...' : 'Confirmar despacho'}
            </button>
          </div>
        </form>
      </div>
    </Modal>
  );
}
