import React from 'react';
import Modal from '../../components/Modal';

const DeleteBranchModal = ({
  open = false,
  branch = null,
  loading = false,
  onConfirm = () => {},
  onCancel = () => {},
}) => {
  if (!branch) return null;

  return (
    <Modal open={open} onClose={onCancel}>
      <div className="space-y-4">
        <h2 className="text-lg font-semibold text-gray-900">Confirmar eliminación</h2>
        <p className="text-gray-700">
          ¿Confirmas eliminar la sucursal <span className="font-semibold">{branch.name}</span>? Esta acción no se puede deshacer.
        </p>
        <div className="flex gap-2 justify-end">
          <button
            onClick={onCancel}
            disabled={loading}
            className="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 disabled:bg-gray-200 disabled:cursor-not-allowed transition"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
          >
            {loading ? 'Eliminando...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default DeleteBranchModal;
