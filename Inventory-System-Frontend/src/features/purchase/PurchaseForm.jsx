import { useState, useEffect } from "react";
import Card from '../../components/Card';
import { useCreatePurchase } from './useCreatePurchase';
import { getBranchIdFromToken } from '../../utils/tokenUtils';
import { getProviders, getProviderProducts } from './purchaseApi';

const normalizeListResponse = (response) => {
  if (Array.isArray(response)) {
    return response;
  }

  if (Array.isArray(response?.content)) {
    return response.content;
  }

  if (Array.isArray(response?.data)) {
    return response.data;
  }

  if (Array.isArray(response?.providers)) {
    return response.providers;
  }

  if (Array.isArray(response?.products)) {
    return response.products;
  }

  return [];
};

const formatExpectedDeliveryDate = (value) => {
  if (!value) {
    return '';
  }

  const datePart = String(value).slice(0, 10);
  return `${datePart}T00:00:00Z`;
};

export default function PurchaseForm() {
  const { submit: submitPurchase, loading: isSubmitting, error: submitError } = useCreatePurchase();

  // Obtener branchId del token
  const branchId = getBranchIdFromToken();

  const paymentTermsOptions = [
    { value: '7D', label: 'Crédito 7 días' },
    { value: '30D', label: 'Crédito 30 días' },
    { value: '60D', label: 'Crédito 60 días' },
  ];

  // Estado para proveedores
  const [providers, setProviders] = useState([]);
  const [loadingProviders, setLoadingProviders] = useState(false);
  const [errorProviders, setErrorProviders] = useState(null);

  // Estado para productos del proveedor
  const [availableProducts, setAvailableProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [errorProducts, setErrorProducts] = useState(null);

  // Estado general del formulario
  const [form, setForm] = useState({
    provider_id: '',
    paymentTerms: '30D',
    expectedDeliveryDate: '',
    notes: '',
  });

  // Estado para los items (líneas de compra)
  const [items, setItems] = useState([
    { productId: '', quantity: 1, unitPrice: 0, discount: 0 }
  ]);

  const [alert, setAlert] = useState("");
  const [success, setSuccess] = useState(false);

  // Cargar proveedores al montar el componente
  useEffect(() => {
    const fetchProviders = async () => {
      setLoadingProviders(true);
      setErrorProviders(null);
      try {
        const data = await getProviders();
        setProviders(normalizeListResponse(data));
      } catch (error) {
        setErrorProviders(error?.message || 'Error al cargar proveedores');
        console.error('Error fetching providers:', error);
      } finally {
        setLoadingProviders(false);
      }
    };

    fetchProviders();
  }, []);

  // Cuando cambia el proveedor, cargar sus productos
  useEffect(() => {
    const fetchProducts = async () => {
      setAlert("");
      setErrorProducts(null);
      
      if (form.provider_id) {
        setLoadingProducts(true);
        try {
          const data = await getProviderProducts(form.provider_id);
          setAvailableProducts(normalizeListResponse(data));
        } catch (error) {
          setErrorProducts(error?.message || 'Error al cargar productos del proveedor');
          console.error('Error fetching products:', error);
          setAvailableProducts([]);
        } finally {
          setLoadingProducts(false);
        }
      } else {
        setAvailableProducts([]);
      }
    };

    fetchProducts();
  }, [form.provider_id]);

  // Maneja cambios en campos principales
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    setAlert("");
    setSuccess(false);
  };

  // Maneja cambios en items
  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index] = { ...newItems[index], [field]: value };

    // Actualizar precio automáticamente si se selecciona producto
    if (field === 'productId') {
      const selectedProduct = availableProducts.find(p => p.id === value);
      if (selectedProduct) {
        // El backend puede devolver 'price' o 'unitPrice'
        newItems[index].unitPrice = selectedProduct.price || selectedProduct.unitPrice || 0;
      }
    }

    setItems(newItems);
    setAlert("");
    setSuccess(false);
  };

  // Agrega un nuevo item
  const addItem = () => {
    setItems([...items, { productId: '', quantity: 1, unitPrice: 0, discount: 0 }]);
  };

  // Elimina un item
  const removeItem = (index) => {
    if (items.length > 1) {
      setItems(items.filter((_, i) => i !== index));
    }
  };

  // Valida el formulario
  const validateForm = () => {
    if (!branchId) {
      setAlert("No se pudo obtener la sucursal del token. Por favor, inicie sesión nuevamente.");
      return false;
    }
    if (!form.provider_id) {
      setAlert("Debe seleccionar un proveedor");
      return false;
    }
    if (!form.expectedDeliveryDate) {
      setAlert("Debe ingresar la fecha esperada de entrega");
      return false;
    }
    if (items.length === 0) {
      setAlert("Debe agregar al menos un producto");
      return false;
    }
    for (let item of items) {
      if (!item.productId || !item.quantity || item.quantity <= 0 || !item.unitPrice || item.unitPrice <= 0) {
        setAlert("Todos los items deben tener producto, cantidad y precio válidos");
        return false;
      }
    }
    return true;
  };

  // Maneja el envío del formulario
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    // Estructura del DTO según especificación
    const purchaseData = {
      branchId: branchId,
      provider_id: form.provider_id,
      items: items.map(item => ({
        productId: item.productId,
        quantity: parseFloat(item.quantity),
        unitPrice: parseFloat(item.unitPrice),
        discount: parseFloat(item.discount) || 0,
      })),
      paymentTerms: form.paymentTerms,
      expectedDeliveryDate: formatExpectedDeliveryDate(form.expectedDeliveryDate),
      notes: form.notes,
    };

    console.log('Purchase create request:', purchaseData);

    try {
      await submitPurchase(purchaseData);
      setSuccess(true);
      // Limpiar formulario
      setForm({
        provider_id: '',
        paymentTerms: '30D',
        expectedDeliveryDate: '',
        notes: '',
      });
      setItems([{ productId: '', quantity: 1, unitPrice: 0, discount: 0 }]);
      setAvailableProducts([]);
    } catch (err) {
      setAlert(submitError || "Error al crear la compra");
    }
  };

  // Calcula el subtotal de un item (cantidad * precioUnitario - descuento)
  const calculateItemTotal = (item) => {
    const subtotal = (parseFloat(item.quantity) || 0) * (parseFloat(item.unitPrice) || 0);
    const discount = parseFloat(item.discount) || 0;
    return Math.max(subtotal - discount, 0);
  };

  // Calcula el total general
  const calculateTotal = () => {
    return items.reduce((sum, item) => sum + calculateItemTotal(item), 0);
  };

  return (
    <div className="w-full max-w-4xl mx-auto">
      <Card>
        <h1 className="text-2xl font-bold mb-2">Crear Compra</h1>
        {branchId && <p className="text-sm text-gray-600 mb-6">Sucursal ID: {branchId}</p>}
        
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Mensajes de Error */}
          {errorProviders && (
            <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded">
              {errorProviders}
            </div>
          )}

          {/* Proveedor */}
          <div>
            <label className="block mb-2 font-semibold">Proveedor *</label>
            {loadingProviders ? (
              <div className="p-2 text-gray-600 italic">Cargando proveedores...</div>
            ) : (
              <select
                name="provider_id"
                value={form.provider_id}
                onChange={handleChange}
                className="w-full border rounded p-2 focus:outline-none focus:ring-2"
              >
                <option value="">Seleccione un proveedor</option>
                {providers.map((provider, index) => (
                  <option key={provider.id || provider.providerId || provider.name || index} value={provider.id || provider.providerId}>
                    {provider.name || provider.businessName || 'Proveedor sin nombre'}
                  </option>
                ))}
              </select>
            )}
          </div>

          {/* Productos del Proveedor */}
          {form.provider_id && (
            <div className="bg-blue-50 border border-blue-200 rounded p-4">
              <p className="font-semibold text-blue-900 mb-2">Productos disponibles del proveedor:</p>
              {loadingProducts ? (
                <div className="text-blue-800 italic">Cargando productos...</div>
              ) : errorProducts ? (
                <div className="text-red-700">{errorProducts}</div>
              ) : availableProducts.length > 0 ? (
                <ul className="list-disc list-inside text-blue-800">
                  {availableProducts.map((product, index) => (
                    <li key={product.id || product.productId || product.name || index}>
                      {product.name || product.productName || 'Producto sin nombre'} - S/. {product.price?.toFixed(2) || product.unitPrice?.toFixed(2) || '0.00'}
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="text-blue-800 italic">Sin productos disponibles</div>
              )}
            </div>
          )}

          {/* Fila: Términos de Pago y Fecha de Entrega */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block mb-2 font-semibold">Términos de Pago</label>
              <select
                name="paymentTerms"
                value={form.paymentTerms}
                onChange={handleChange}
                className="w-full border rounded p-2 focus:outline-none focus:ring-2"
              >
                {paymentTermsOptions.map(opt => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block mb-2 font-semibold">Fecha Esperada de Entrega *</label>
              <input
                type="datetime-local"
                name="expectedDeliveryDate"
                value={form.expectedDeliveryDate}
                onChange={handleChange}
                className="w-full border rounded p-2 focus:outline-none focus:ring-2"
              />
            </div>
          </div>

          {/* Notas */}
          <div>
            <label className="block mb-2 font-semibold">Notas</label>
            <textarea
              name="notes"
              value={form.notes}
              onChange={handleChange}
              placeholder="Notas adicionales sobre la compra..."
              rows="3"
              className="w-full border rounded p-2 focus:outline-none focus:ring-2 resize-none"
            />
          </div>

          {/* Items de Compra */}
          <div className="border-t pt-6">
            <h2 className="text-lg font-bold mb-4">Productos *</h2>
            
            <div className="space-y-4">
              {items.map((item, index) => (
                <div key={index} className="border rounded p-4 bg-gray-50">
                  <div className="grid grid-cols-1 md:grid-cols-5 gap-4 items-end">
                    <div>
                      <label className="block mb-2 text-sm font-semibold">Producto</label>
                      <select
                        value={item.productId}
                        onChange={(e) => handleItemChange(index, 'productId', e.target.value)}
                        disabled={loadingProducts || availableProducts.length === 0}
                        className="w-full border rounded p-2 focus:outline-none focus:ring-2 disabled:bg-gray-200"
                      >
                        <option value="">
                          {loadingProducts ? 'Cargando...' : availableProducts.length === 0 ? 'Sin productos' : 'Seleccione'}
                        </option>
                        {availableProducts.map((product, index) => (
                          <option key={product.id || product.productId || product.name || index} value={product.id || product.productId}>
                            {product.name || product.productName || 'Producto sin nombre'}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block mb-2 text-sm font-semibold">Cantidad</label>
                      <input
                        type="number"
                        step="0.01"
                        min="0.01"
                        value={item.quantity}
                        onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                        className="w-full border rounded p-2 focus:outline-none focus:ring-2"
                      />
                    </div>

                    <div>
                      <label className="block mb-2 text-sm font-semibold">Precio Unit.</label>
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        value={item.unitPrice}
                        onChange={(e) => handleItemChange(index, 'unitPrice', e.target.value)}
                        className="w-full border rounded p-2 focus:outline-none focus:ring-2"
                      />
                    </div>

                    <div>
                      <label className="block mb-2 text-sm font-semibold">Descuento</label>
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        value={item.discount}
                        onChange={(e) => handleItemChange(index, 'discount', e.target.value)}
                        className="w-full border rounded p-2 focus:outline-none focus:ring-2"
                      />
                    </div>

                    <div>
                      <label className="block mb-2 text-sm font-semibold">Subtotal</label>
                      <div className="font-semibold">
                        S/. {calculateItemTotal(item).toFixed(2)}
                      </div>
                    </div>

                    {items.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removeItem(index)}
                        className="bg-red-500 text-white px-3 py-2 rounded hover:bg-red-600 transition col-span-1"
                      >
                        Eliminar
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>

            <button
              type="button"
              onClick={addItem}
              className="mt-4 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition"
            >
              + Agregar Producto
            </button>
          </div>

          {/* Total */}
          <div className="border-t pt-4">
            <div className="text-right">
              <p className="text-lg font-bold">Total: S/. {calculateTotal().toFixed(2)}</p>
            </div>
          </div>

          {/* Mensajes */}
          {alert && (
            <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded">
              {alert}
            </div>
          )}
          {success && (
            <div className="p-3 bg-green-100 border border-green-400 text-green-700 rounded">
              ✓ Compra creada exitosamente. Se ha enviado al backend.
            </div>
          )}

          {/* Botón Submit */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-green-500 text-white font-semibold px-4 py-3 rounded hover:bg-green-600 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {isSubmitting ? 'Enviando...' : 'Confirmar Compra'}
          </button>
        </form>
      </Card>
    </div>
  );
}
