import { useState } from "react";
import Card from '../../components/Card';

export default function PurchaseForm() {
  const [form, setForm] = useState({
    product: '',
    quantity: 1,
    price: '',
    discount: '',
    supplier: '',
  });
  const [alert, setAlert] = useState("");
  const [success, setSuccess] = useState(false);

  // Simulación de productos y proveedores (reemplazar por fetch real)
  const products = [
    { id: '1', name: 'Leche Gloria 1L' },
    { id: '2', name: 'Arroz 5kg' },
  ];
  const suppliers = [
    { id: 'a', name: 'Proveedor A' },
    { id: 'b', name: 'Proveedor B' },
  ];

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setAlert("");
    setSuccess(false);
  };

  const handleSubmit = e => {
    e.preventDefault();
    if (!form.product || !form.quantity || !form.price || !form.supplier) {
      setAlert("Todos los campos obligatorios deben estar completos.");
      return;
    }
    setSuccess(true);
  };

  return (
    <Card className="max-w-xl mx-auto">
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">Producto</label>
          <select name="product" className="w-full border rounded p-2" value={form.product} onChange={handleChange}>
            <option value="">Seleccione un producto</option>
            {products.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">Cantidad</label>
          <input
            name="quantity"
            type="number"
            min="1"
            value={form.quantity}
            onChange={handleChange}
            className="w-full border rounded p-2"
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">Precio Unitario</label>
          <input
            name="price"
            type="number"
            min="0"
            step="0.01"
            value={form.price}
            onChange={handleChange}
            className="w-full border rounded p-2"
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">Descuento (%)</label>
          <input
            name="discount"
            type="number"
            min="0"
            max="100"
            value={form.discount}
            onChange={handleChange}
            className="w-full border rounded p-2"
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">Proveedor</label>
          <select name="supplier" className="w-full border rounded p-2" value={form.supplier} onChange={handleChange}>
            <option value="">Seleccione un proveedor</option>
            {suppliers.map(s => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
        {alert && <div className="mb-2 text-red-600 font-semibold">{alert}</div>}
        {success && <div className="mb-2 text-green-600 font-semibold">Recepción confirmada. El inventario se actualizará automáticamente.</div>}
        <button
          type="submit"
          className="bg-primary text-white px-4 py-2 rounded mt-2"
        >
          Confirmar compra
        </button>
      </form>
    </Card>
  );
}
