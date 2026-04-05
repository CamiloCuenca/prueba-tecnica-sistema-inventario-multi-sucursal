import Modal from '../../components/Modal';

export default function DeleteUserModal({ open, user, submitting, onCancel, onConfirm, error }) {
  return (
    <Modal open={open} onClose={onCancel}>
      <div className="space-y-4">
        <h3 className="text-lg font-semibold text-gray-900">Confirmar eliminacion</h3>
        <p className="text-sm text-gray-700">
          {`¿Confirmas eliminar al usuario ${user?.name || ''}? Esta accion no se puede deshacer.`}
        </p>

        {error && <div className="text-sm text-red-600">{error}</div>}

        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="rounded border border-gray-300 px-4 py-2 text-sm"
            disabled={submitting}
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="rounded bg-red-600 text-white px-4 py-2 text-sm"
            disabled={submitting}
          >
            {submitting ? 'Eliminando...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </Modal>
  );
}
