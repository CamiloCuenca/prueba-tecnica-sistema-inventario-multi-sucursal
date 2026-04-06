import React, { useState } from 'react';

/**
 * Componente para seleccionar productos de una sucursal y especificar cantidades
 * Sub-componente del formulario TransferRequestForm
 */
export default function ProductTransferSelector({
  products = [],
  selectedItems = {},
  onUpdateQuantity,
  isLoading = false,
}) {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredProducts = products.filter(
    (product) =>
      (product.productId?.includes(searchTerm) || searchTerm === '') ||
      (product.productName?.toLowerCase().includes(searchTerm.toLowerCase()) || false)
  );

  if (isLoading) {
    return (
      <div className="p-4 text-center text-gray-500">Cargando inventario...</div>
    );
  }

  if (products.length === 0) {
    return (
      <div className="p-4 text-center text-gray-500">
        No hay productos disponibles en esta sucursal
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Buscador */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Buscar producto
        </label>
        <input
          type="text"
          placeholder="ID o nombre del producto..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2"
        />
      </div>

      {/* Lista de productos */}
      <div className="border rounded-lg overflow-y-auto max-h-96">
        <table className="w-full text-sm">
          <thead className="bg-gray-100 sticky top-0">
            <tr>
              <th className="text-left px-3 py-2 font-semibold">Producto ID</th>
              <th className="text-left px-3 py-2 font-semibold">Disponible</th>
              <th className="text-left px-3 py-2 font-semibold">Cantidad a transferir</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.map((product) => {
              const availableQty = Number(product.quantity || product.availableQuantity || 0);
              const selectedQty = selectedItems[product.productId] || 0;

              return (
                <tr key={product.productId} className="border-t hover:bg-gray-50">
                  <td className="px-3 py-2">
                    <div className="font-medium text-text">{product.productName || '-'}</div>
                    <div className="text-xs text-gray-500">{product.productId}</div>
                  </td>
                  <td className="px-3 py-2">
                    <span className="inline-block px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs font-semibold">
                      {availableQty}
                    </span>
                  </td>
                  <td className="px-3 py-2">
                    <input
                      type="number"
                      min="0"
                      max={availableQty}
                      step="1"
                      value={selectedQty === 0 ? '' : selectedQty}
                      onChange={e => {
                        const val = e.target.value;
                        onUpdateQuantity(product.productId, val === '' ? 0 : Number(val));
                      }}
                      className="w-20 border rounded px-2 py-1 text-center focus:outline-none focus:ring-2"
                      placeholder="0"
                    />
                    {selectedQty > 0 && (
                      <span className="ml-2 text-xs text-gray-600">
                        ({selectedQty}/{availableQty})
                      </span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {filteredProducts.length === 0 && (
        <div className="text-center py-4 text-gray-500">
          No se encontraron productos que coincidan con la búsqueda
        </div>
      )}

      {/* Resumen */}
      {Object.keys(selectedItems).length > 0 && (
        <div className="p-3 bg-blue-50 border border-blue-200 rounded">
          <p className="text-sm font-medium text-gray-900">
            {Object.keys(selectedItems).length} producto(s) seleccionado(s)
          </p>
          <p className="text-xs text-gray-600 mt-1">
            Total a transferir:{' '}
            <span className="font-semibold">
              {Object.values(selectedItems).reduce((sum, qty) => sum + qty, 0)} unidades
            </span>
          </p>
        </div>
      )}
    </div>
  );
}
