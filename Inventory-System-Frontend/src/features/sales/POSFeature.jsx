import { useState, useEffect } from "react";
import Card from '../../components/Card';
import { useSales } from './useSales';
import { getSaleReceiptPdf } from './receiptApi';
import { getProductPrices } from './pricesApi';

export default function POSFeature() {
  const { products, loading, error, registerSale } = useSales();
  const [cart, setCart] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [price, setPrice] = useState(0);
  const [discount, setDiscount] = useState(0);
  const [prices, setPrices] = useState([]);
  const [alert, setAlert] = useState("");
  const [success, setSuccess] = useState("");
  const [processing, setProcessing] = useState(false);
  const [saleId, setSaleId] = useState(null);
  const [downloading, setDownloading] = useState(false);
  const [editIdx, setEditIdx] = useState(null);
  const [discountTotal, setDiscountTotal] = useState(0);

  const selectedProduct = products.find(p => p.productId === selectedProductId);
  const stock = selectedProduct ? selectedProduct.quantity : 0;

  // Cargar precios de referencia al seleccionar producto
  useEffect(() => {
    if (selectedProductId) {
      getProductPrices(selectedProductId)
        .then((data) => setPrices(data))
        .catch(() => setPrices([]));
    } else {
      setPrices([]);
    }
  }, [selectedProductId]);

  const handleSelectProduct = (e) => {
    setSelectedProductId(e.target.value);
    setQuantity(1);
    setDiscount(0);
    setAlert("");
    setSuccess("");
    setEditIdx(null);
    // Si hay precios, selecciona el primero por defecto
    setPrice(0);
  };

  const handleAddOrEditCart = () => {
    if (!selectedProduct) return;
    if (quantity > stock) {
      setAlert("No hay stock suficiente para este producto.");
      return;
    }
    let newCart = [...cart];
    if (editIdx !== null) {
      // Editar producto existente
      newCart[editIdx] = {
        productId: selectedProduct.productId,
        name: selectedProduct.productName,
        quantity,
        price,
        discount,
        stock,
      };
      setEditIdx(null);
    } else {
      // Agregar nuevo producto
      const idx = cart.findIndex(item => item.productId === selectedProduct.productId);
      if (idx >= 0) {
        setAlert("Este producto ya está en el carrito. Edítalo si deseas cambiar cantidad o precio.");
        return;
      }
      newCart.push({
        productId: selectedProduct.productId,
        name: selectedProduct.productName,
        quantity,
        price,
        discount,
        stock,
      });
    }
    setCart(newCart);
    setAlert("");
    setSuccess("");
    setSelectedProductId("");
    setQuantity(1);
    setPrice(0);
    setDiscount(0);
  };

  const handleEditCart = (idx) => {
    const item = cart[idx];
    setSelectedProductId(item.productId);
    setQuantity(item.quantity);
    setPrice(item.price);
    setDiscount(item.discount);
    setEditIdx(idx);
    setAlert("");
    setSuccess("");
  };

  const handleRemoveCart = (idx) => {
    let newCart = [...cart];
    newCart.splice(idx, 1);
    setCart(newCart);
    setAlert("");
    setSuccess("");
  };

  const handleRegisterSale = async () => {
    setProcessing(true);
    setAlert("");
    setSuccess("");
    setSaleId(null);
    // Validar stock antes de enviar
    for (const item of cart) {
      const prod = products.find(p => p.productId === item.productId);
      if (!prod || item.quantity > prod.quantity) {
        setAlert(`Stock insuficiente para ${item.name}`);
        setProcessing(false);
        return;
      }
    }
    const items = cart.map(({ productId, quantity, price, discount }) => ({ productId, quantity, price, discount }));
    const res = await registerSale(items, discountTotal);
    if (res && res.id) {
      setSuccess("Venta registrada correctamente.");
      setSaleId(res.id);
      setCart([]);
      setSelectedProductId("");
      setQuantity(1);
      setPrice(0);
      setDiscount(0);
      setDiscountTotal(0);
    } else {
      setAlert("Error al registrar la venta.");
    }
    setProcessing(false);
  };

  const handleDownloadReceipt = async () => {
    if (!saleId) return;
    setDownloading(true);
    try {
      const blob = await getSaleReceiptPdf(saleId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `comprobante-venta-${saleId}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setAlert('No se pudo descargar el comprobante.');
    }
    setDownloading(false);
  };

  return (
    <Card className="max-w-xl mx-auto">
      <div className="mb-4">
        <label className="block mb-1 font-semibold">Producto</label>
        <select className="w-full border rounded p-2" value={selectedProductId} onChange={handleSelectProduct} disabled={loading}>
          <option value="">Seleccione un producto</option>
          {products.map(p => (
            <option key={p.productId} value={p.productId}>{p.productName} (Stock: {p.quantity})</option>
          ))}
        </select>
      </div>
      {selectedProduct && (
        <>
          <div className="mb-4">
            <label className="block mb-1 font-semibold">Cantidad</label>
            <input
              type="number"
              min="1"
              max={stock}
              value={quantity === 0 ? '' : quantity}
              onChange={e => {
                const val = e.target.value;
                setQuantity(val === '' ? 0 : Number(val));
              }}
              className="w-full border rounded p-2"
            />
          </div>
          <div className="mb-4">
            <label className="block mb-1 font-semibold">Precio sugerido</label>
            <select
              className="w-full border rounded p-2 mb-2"
              value={price}
              onChange={e => setPrice(Number(e.target.value))}
            >
              <option value={0}>Seleccionar precio</option>
              {prices.map((pr, idx) => (
                <option key={idx} value={pr.price}>{pr.label} - S/ {pr.price}</option>
              ))}
            </select>
            <input
              type="number"
              min="0"
              step="0.01"
              value={price === 0 ? '' : price}
              onChange={e => {
                const val = e.target.value;
                setPrice(val === '' ? 0 : Number(val));
              }}
              className="w-full border rounded p-2"
              placeholder="Precio personalizado"
            />
          </div>
          <div className="mb-4">
            <label className="block mb-1 font-semibold">Descuento (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              value={discount === 0 ? '' : discount}
              onChange={e => {
                const val = e.target.value;
                setDiscount(val === '' ? 0 : Number(val));
              }}
              className="w-full border rounded p-2"
            />
          </div>
        </>
      )}
      {alert && <div className="mb-2 text-red-600 font-semibold">{alert}</div>}
      {success && <div className="mb-2 text-green-600 font-semibold">{success}</div>}
      <button
        className="bg-primary text-white px-4 py-2 rounded disabled:opacity-50"
        disabled={!selectedProduct || quantity < 1 || quantity > stock || loading || price <= 0}
        onClick={handleAddOrEditCart}
      >
        {editIdx !== null ? 'Actualizar producto' : 'Agregar al carrito'}
      </button>
      <div className="mt-6">
        <h2 className="font-bold mb-2">Carrito</h2>
        {cart.length === 0 && <div>No hay productos en el carrito.</div>}
        {cart.map((item, idx) => (
          <div key={idx} className="border-b py-2 flex flex-col md:flex-row md:justify-between items-start md:items-center gap-2">
            <div>
              <span className="font-semibold">{item.name}</span> x {item.quantity} &nbsp;
              <span className="text-gray-500">S/ {item.price} </span>
              {item.discount > 0 && <span className="text-blue-600">(-{item.discount}%)</span>}
            </div>
            <div className="flex gap-2">
              <button className="text-xs bg-yellow-500 text-white px-2 py-1 rounded" onClick={() => handleEditCart(idx)}>Editar</button>
              <button className="text-xs bg-red-600 text-white px-2 py-1 rounded" onClick={() => handleRemoveCart(idx)}>Eliminar</button>
            </div>
          </div>
        ))}
        {cart.length > 0 && (
          <>
            <div className="mb-4 mt-4">
              <label className="block mb-1 font-semibold">Descuento general (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                value={discountTotal === 0 ? '' : discountTotal}
                onChange={e => {
                  const val = e.target.value;
                  setDiscountTotal(val === '' ? 0 : Number(val));
                }}
                className="w-full border rounded p-2"
              />
            </div>
            <button
              className="mt-2 w-full bg-green-600 text-white px-4 py-2 rounded disabled:opacity-50"
              onClick={handleRegisterSale}
              disabled={processing}
            >
              Confirmar venta
            </button>
          </>
        )}
        {saleId && (
          <button
            className="mt-4 w-full bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50"
            onClick={handleDownloadReceipt}
            disabled={downloading}
          >
            {downloading ? 'Descargando comprobante...' : 'Descargar comprobante PDF'}
          </button>
        )}
      </div>
      {loading && <div className="text-center py-2 text-gray-500">Cargando productos...</div>}
      {error && <div className="text-center py-2 text-red-500">{error}</div>}
    </Card>
  );
}
