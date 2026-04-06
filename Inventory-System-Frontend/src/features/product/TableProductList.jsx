import React, { useEffect, useState } from "react";
import Table from "../../components/Table";
import TablePaginator from "../../components/TablePaginator";
import LoadingSpinner from "../../components/LoadingSpinner";
import { useProductList } from "./useProductList";
import { useProductById } from "./useProductById";
import Modal from "../../components/Modal";
import { getProviders } from "../providers/providersApi";
import { createProduct } from "./productApi";
import { deleteProduct, updateProduct } from "./productApi";

export default function TableProductList() {
  // Estados para proveedores y filtro
  const [providers, setProviders] = useState([]);
  const [providerId, setProviderId] = useState("");
  const [loadingProviders, setLoadingProviders] = useState(false);
  const [errorProviders, setErrorProviders] = useState(null);

  // Estados para productos y paginación
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [sort] = useState([]);
  const { products, pageInfo, loading, error, fetchProducts } = useProductList();

  // Modal de detalle
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const { product, fetchProduct, loading: loadingDetail, error: errorDetail } = useProductById();

  // Modal para crear producto
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [form, setForm] = useState({ name: "", sku: "", unit: "", providerIds: [] });

  // Cargar proveedores al montar
  useEffect(() => {
    setLoadingProviders(true);
    getProviders({ size: 100 }).then((data) => {
      setProviders(data.content || []);
      setLoadingProviders(false);
    }).catch((err) => {
      setErrorProviders("Error al cargar proveedores");
      setLoadingProviders(false);
    });
  }, []);

  // Cargar productos al cambiar paginación
  useEffect(() => {
    fetchProducts({ page, size, sort });
    // eslint-disable-next-line
  }, [page, size, sort]);

  // Buscar detalle de producto al abrir modal
  useEffect(() => {
    if (modalOpen && selectedId) fetchProduct(selectedId);
    // eslint-disable-next-line
  }, [modalOpen, selectedId]);

  // Handlers
  const handleAction = (row) => {
    setSelectedId(row.productId || row.id);
    setModalOpen(true);
  };

  // Editar producto
  const handleEdit = (row) => {
    setForm({
      name: row.name || "",
      sku: row.sku || "",
      unit: row.unit || "",
      providerIds: row.providerIds || [],
    });
    setCreateError(null);
    setEditingId(row.productId || row.id);
    setCreateModalOpen(true);
  };

  // Eliminar producto
  const [deletingId, setDeletingId] = useState(null);
  const [deleteError, setDeleteError] = useState(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const handleDelete = (row) => {
    setDeletingId(row.productId || row.id);
    setDeleteError(null);
    setDeleteModalOpen(true);
  };
  const confirmDelete = async () => {
    try {
      await deleteProduct(deletingId);
      setDeleteModalOpen(false);
      setDeletingId(null);
      fetchProducts({ page, size, sort });
    } catch (err) {
      setDeleteError(err?.message || "Error al eliminar producto");
    }
  };

  // Estado para saber si es edición
  const [editingId, setEditingId] = useState(null);
  const handleOpenCreate = () => {
    setForm({ name: "", sku: "", unit: "", providerIds: [] });
    setCreateError(null);
    setCreateModalOpen(true);
  };
  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };
  const handleProviderChange = (e) => {
    const options = Array.from(e.target.selectedOptions).map((opt) => opt.value);
    setForm((prev) => ({ ...prev, providerIds: options }));
  };
  const handleCreateProduct = async (e) => {
    e.preventDefault();
    setCreating(true);
    setCreateError(null);
    try {
      if (editingId) {
        await updateProduct(editingId, {
          name: form.name,
          sku: form.sku,
          unit: form.unit,
          providerIds: form.providerIds,
        });
      } else {
        await createProduct({
          name: form.name,
          sku: form.sku,
          unit: form.unit,
          providerIds: form.providerIds,
        });
      }
      setCreateModalOpen(false);
      setForm({ name: "", sku: "", unit: "", providerIds: [] });
      setEditingId(null);
      fetchProducts({ page, size, sort });
    } catch (err) {
      setCreateError(err?.message || (editingId ? "Error al editar producto" : "Error al crear producto"));
    } finally {
      setCreating(false);
    }
  };

  // Filtrado en frontend si providerId está seleccionado
  const filteredProducts = providerId
    ? products.filter((p) => (Array.isArray(p.providerIds) ? p.providerIds.includes(providerId) : false))
    : products;

  if (loading) return <LoadingSpinner label="Cargando productos..." />;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div>
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center gap-2">
            <label className="font-medium">Proveedor:</label>
            {loadingProviders ? (
              <span className="text-gray-500 text-sm">Cargando proveedores...</span>
            ) : errorProviders ? (
              <span className="text-red-600 text-sm">{errorProviders}</span>
            ) : (
              <select
                className="border rounded px-2 py-1 text-sm"
                value={providerId}
                onChange={e => setProviderId(e.target.value)}
              >
                <option value="">Todos</option>
                {providers.map((prov) => (
                  <option key={prov.id} value={prov.id}>{prov.name}</option>
                ))}
              </select>
            )}
          </div>
          <button
            className="bg-primary text-white px-4 py-2 rounded shadow hover:bg-primary-dark transition"
            onClick={handleOpenCreate}
          >
            Crear producto
          </button>
        </div>
        <Table
          data={filteredProducts}
          searchable={true}
          searchFields={["name", "sku"]}
          onAction={handleAction}
          actionPayload={(row) => row}
          actionKey="productId"
          // Renderizar acciones personalizadas
          renderActions={(row) => (
            <div className="flex flex-wrap justify-end gap-2">
              <button
                type="button"
                title="Ver"
                className="rounded border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
                onClick={() => handleAction(row)}
              >
                Ver
              </button>
              <button
                type="button"
                title="Editar"
                className="rounded border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
                onClick={() => handleEdit(row)}
              >
                Editar
              </button>
              <button
                type="button"
                title="Borrar"
                className="rounded bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700"
                onClick={() => handleDelete(row)}
              >
                Borrar
              </button>
            </div>
          )}
        />
                <Modal open={deleteModalOpen} onClose={() => setDeleteModalOpen(false)}>
                  <div className="space-y-4">
                    <h2 className="text-lg font-bold">¿Eliminar producto?</h2>
                    {deleteError && <div className="text-red-600 text-sm">{deleteError}</div>}
                    <div className="flex justify-end gap-2">
                      <button
                        className="px-3 py-1 rounded border"
                        onClick={() => setDeleteModalOpen(false)}
                      >Cancelar</button>
                      <button
                        className="px-4 py-1 rounded bg-red-600 text-white"
                        onClick={confirmDelete}
                      >Eliminar</button>
                    </div>
                  </div>
                </Modal>
        <TablePaginator
          page={page}
          totalPages={pageInfo.totalPages || 1}
          onPageChange={setPage}
          isFirst={page === 0}
          isLast={page >= (pageInfo.totalPages || 1) - 1}
        />
        <Modal open={createModalOpen} onClose={() => setCreateModalOpen(false)}>
          <form className="space-y-4" onSubmit={handleCreateProduct}>
            <h2 className="text-lg font-bold">Crear producto</h2>
            <div>
              <label className="block text-sm font-medium mb-1">Nombre</label>
              <input
                type="text"
                name="name"
                value={form.name}
                onChange={handleFormChange}
                className="w-full border rounded px-2 py-1"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">SKU</label>
              <input
                type="text"
                name="sku"
                value={form.sku}
                onChange={handleFormChange}
                className="w-full border rounded px-2 py-1"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Unidad</label>
              <input
                type="text"
                name="unit"
                value={form.unit}
                onChange={handleFormChange}
                className="w-full border rounded px-2 py-1"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Proveedores</label>
              <select
                name="providerIds"
                multiple
                value={form.providerIds}
                onChange={handleProviderChange}
                className="w-full border rounded px-2 py-1 h-24"
                required
              >
                {providers.map((prov) => (
                  <option key={prov.id} value={prov.id}>{prov.name}</option>
                ))}
              </select>
              <span className="text-xs text-gray-500">(Puedes seleccionar varios con Ctrl/Cmd)</span>
            </div>
            {createError && <div className="text-red-600 text-sm">{createError}</div>}
            <div className="flex justify-end gap-2">
              <button
                type="button"
                className="px-3 py-1 rounded border"
                onClick={() => setCreateModalOpen(false)}
                disabled={creating}
              >Cancelar</button>
              <button
                type="submit"
                className="px-4 py-1 rounded bg-primary text-white"
                disabled={creating}
              >{creating ? "Creando..." : "Crear"}</button>
            </div>
          </form>
        </Modal>
        <Modal open={modalOpen} onClose={() => setModalOpen(false)}>
          {loadingDetail ? (
            <LoadingSpinner label="Cargando detalle..." />
          ) : errorDetail ? (
            <div className="text-red-600">{errorDetail}</div>
          ) : product ? (
            <div className="space-y-2">
              <h2 className="text-xl font-bold mb-2">Detalle del producto</h2>
              <div><b>ID:</b> {product.id}</div>
              <div><b>Nombre:</b> {product.name}</div>
              <div><b>SKU:</b> {product.sku}</div>
              <div><b>Unidad:</b> {product.unit}</div>
              <div><b>Proveedores:</b> {(product.providerIds || []).join(', ')}</div>
            </div>
          ) : (
            <div className="text-gray-500">No hay datos del producto.</div>
          )}
        </Modal>
      </div>
    </div>
  );
}