import React from 'react';
import { useTransferForm } from './useTransferForm';
import ProductTransferSelector from './ProductTransferSelector';

/**
 * COMPONENTE: TransferRequestForm
 *
 * Formulario para solicitar una transferencia de productos entre sucursales.
 *
 * PRINCIPIO DE AUTONOMÍA OPERATIVA:
 * Cada sucursal puede operar de manera autónoma pero colaborar con otras sucursales
 * a través de transferencias de inventario. Esto permite:
 * - Control local del inventario de cada sucursal
 * - Colaboración descentralizada sin dependencia de autoridad central
 * - Visibilidad compartida de stock entre sucursales
 * - Transferencias ágiles según necesidades operativas locales
 *
 * FLUJO DE VALIDACIÓN:
 * 1. Origen ≠ Destino (no se puede transferir a la misma sucursal)
 * 2. Al menos un producto debe ser seleccionado
 * 3. Cantidad disponible debe estar dentro del límite
 *
 * RESPONSABILIDADES:
 * - useTransferForm: lógica de estado, validaciones y API
 * - ProductTransferSelector: selección intuitiva de productos
 * - Este componente: composición y flujo visual
 */
export default function TransferRequestForm() {
  const {
    originBranchId,
    setOriginBranchId,
    destinationBranchId,
    setDestinationBranchId,
    isAdmin,
    currentBranchId,
    branches,
    originInventory,
    selectedItems,
    updateItemQuantity,
    loadingBranches,
    loadingInventory,
    submitting,
    error,
    success,
    submitTransferRequest,
  } = useTransferForm();

  const getDestinationBranchName = (id) => {
    const branch = branches.find((b) => b.id === id);
    return branch?.name || id;
  };

  const getOriginBranchName = (id) => {
    const branch = branches.find((b) => b.id === id);
    return branch?.name || id;
  };

  const availableDestinations = branches.filter(
    (b) => b.id !== originBranchId
  );

  const userDestinationBranch = branches.find((branch) => branch.id === currentBranchId);
  const destinationOptions = isAdmin
    ? availableDestinations
    : currentBranchId
      ? [{ id: currentBranchId, name: userDestinationBranch?.name || currentBranchId }]
      : [];

  if (loadingBranches) {
    return (
      <div className="w-full max-w-4xl mx-auto p-6 bg-white rounded-lg shadow">
        <div className="text-center py-12 text-gray-500">
          Cargando datos de sucursales...
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-4xl mx-auto p-6 bg-white rounded-lg shadow space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Solicitar Transferencia</h1>
        <p className="text-sm text-gray-600 mt-2">
          Transfiere productos entre sucursales de forma autónoma. Cada sucursal
          colabora según sus necesidades operativas.
        </p>
      </div>

      {/* Error y Success Messages */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 rounded text-sm">
          {error}
        </div>
      )}

      {success && (
        <div className="p-4 bg-green-50 border border-green-200 text-green-700 rounded text-sm">
          {success}
        </div>
      )}

      {/* Selección de Sucursales */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pb-6 border-b">
        {/* Sucursal Origen */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Sucursal Origen
          </label>
          <select
            value={originBranchId}
            onChange={(e) => setOriginBranchId(e.target.value)}
            disabled={loadingBranches}
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 disabled:bg-gray-100"
          >
            <option value="">-- Selecciona sucursal --</option>
            {branches.map((branch) => (
              <option key={branch.id} value={branch.id}>
                {branch.name || branch.id}
              </option>
            ))}
          </select>
          <p className="text-xs text-gray-500 mt-1">
            La sucursal de donde sales los productos
          </p>
        </div>

        {/* Sucursal Destino */}
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Sucursal Destino
          </label>
          <select
            value={destinationBranchId}
            onChange={(e) => setDestinationBranchId(e.target.value)}
            disabled={!isAdmin || !originBranchId || loadingBranches}
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 disabled:bg-gray-100"
          >
            <option value="">-- Selecciona sucursal --</option>
            {destinationOptions.map((branch) => (
              <option key={branch.id} value={branch.id}>
                {branch.name || branch.id}
              </option>
            ))}
          </select>
          <p className="text-xs text-gray-500 mt-1">
            La sucursal que recibe los productos (no puede ser igual al origen)
          </p>
        </div>
      </div>

      {/* Resumen de Sucursales */}
      {originBranchId && destinationBranchId && (
        <div className="p-4 bg-blue-50 border border-blue-200 rounded">
          <p className="text-sm text-gray-700">
            <span className="font-semibold">{getOriginBranchName(originBranchId)}</span>
            {' → '}
            <span className="font-semibold">{getDestinationBranchName(destinationBranchId)}</span>
          </p>
        </div>
      )}

      {/* Selección de Productos */}
      {originBranchId && (
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Productos a Transferir
          </label>
          <ProductTransferSelector
            products={originInventory}
            selectedItems={selectedItems}
            onUpdateQuantity={updateItemQuantity}
            isLoading={loadingInventory}
          />
        </div>
      )}

      {/* Botón Submit */}
      <div className="flex justify-end pt-6 border-t">
        <button
          onClick={submitTransferRequest}
          disabled={
            !originBranchId ||
            !destinationBranchId ||
            Object.keys(selectedItems).length === 0 ||
            submitting ||
            loadingBranches ||
            loadingInventory
          }
          className="rounded px-6 py-2 bg-primary text-white font-medium text-sm disabled:opacity-60 disabled:cursor-not-allowed hover:bg-opacity-90 transition"
        >
          {submitting ? 'Creando solicitud...' : 'Crear Solicitud de Transferencia'}
        </button>
      </div>

      {/* Información de Autonomía Operativa */}
      <div className="p-4 bg-gray-50 border border-gray-200 rounded text-xs text-gray-600">
        <p className="font-semibold text-gray-700 mb-2">💡 Autonomía Operativa</p>
        <ul className="space-y-1 list-disc list-inside">
          <li>Cada sucursal solicita transferencias según sus necesidades</li>
          <li>Las sucursales colaboran de forma descentralizada</li>
          <li>No hay dependencia de autoridad central para transferencias</li>
          <li>Visibilidad compartida de inventarios entre todas las sucursales</li>
        </ul>
      </div>
    </div>
  );
}
