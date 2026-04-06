import React, { useEffect, useState } from "react";
import { useCsvTemplate } from "./useCsvTemplate";
import { useImportProductsFromCsv } from "./useImportProductsFromCsv";
import Table from "../../components/Table";
import TablePaginator from "../../components/TablePaginator";
import LoadingSpinner from "../../components/LoadingSpinner";
import { useProductList } from "./useProductList";
import { useProductById } from "./useProductById";
import Modal from "../../components/Modal";
import { getProviders } from "../providers/providersApi";
import { createProduct } from "./productApi";
import { deleteProduct, updateProduct } from "./productApi";
import { useDeleteProductFromProvider } from "./useDeleteProductFromProvider";

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

  // Modal para importar CSV
  const [importModalOpen, setImportModalOpen] = useState(false);
  const [csvFile, setCsvFile] = useState(null);
  const [selectedProviders, setSelectedProviders] = useState([]);
  const { importCsv, loading: loadingImport, error: errorImport, success: successImport } = useImportProductsFromCsv();

  // Manejar selección de proveedores en checklist
  const handleProviderCheck = (id) => {
    setSelectedProviders((prev) =>
      prev.includes(id) ? prev.filter((pid) => pid !== id) : [...prev, id]
    );
  };

  // Manejar archivo CSV
  const handleCsvFileChange = (e) => {
    setCsvFile(e.target.files[0] || null);
  };

  // Enviar importación
  const handleImportCsv = async (e) => {
    e.preventDefault();
    if (!csvFile) return;
    // Si hay proveedores seleccionados, crear un archivo temporal con provider_ids
    if (selectedProviders.length > 0) {
      // Leer el archivo CSV y reemplazar/añadir la columna provider_ids
      const text = await csvFile.text();
      const lines = text.split(/\r?\n/);
      if (lines.length > 1) {
        const headers = lines[0].split(',');
        let idx = headers.findIndex(h => h.trim() === 'provider_ids');
        if (idx === -1) {
          headers.push('provider_ids');
          idx = headers.length - 1;
          lines[0] = headers.join(',');
        }
        for (let i = 1; i < lines.length; i++) {
          if (!lines[i].trim()) continue;
          const cols = lines[i].split(',');
          cols[idx] = selectedProviders.join(';');
          lines[i] = cols.join(',');
        }
        const newCsv = lines.join('\n');
        const blob = new Blob([newCsv], { type: 'text/csv' });
        const fileWithProviders = new File([blob], csvFile.name, { type: 'text/csv' });
        await importCsv(fileWithProviders);
      } else {
        await importCsv(csvFile);
      }
    } else {
      await importCsv(csvFile);
    }
    setCsvFile(null);
    setSelectedProviders([]);
    setImportModalOpen(false);
    fetchProducts({ page, size, sort });
  };

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

  // Hook para descargar plantilla CSV
  const { fetchTemplate, loading: loadingTemplate, error: errorTemplate } = useCsvTemplate();

  const handleDownloadTemplate = async () => {
    const blob = await fetchTemplate();
    if (blob) {
      // Si la respuesta es un blob, descargar como archivo
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'plantilla_productos.csv';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    }
  };

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
  const [deleteSuccess, setDeleteSuccess] = useState(null);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedProvidersToDelete, setSelectedProvidersToDelete] = useState([]);
  const { deleteFromProvider, loading: loadingDeleteProvider } = useDeleteProductFromProvider();

  // Al hacer click en borrar, cargar detalle del producto si no está cargado
  const handleDelete = async (row) => {
    setDeletingId(row.productId || row.id);
    setDeleteError(null);
    setSelectedProvidersToDelete([]);
    setDeleteModalOpen(true);
    if (!product || product.id !== (row.productId || row.id)) {
      await fetchProduct(row.productId || row.id);
    }
  };

  // Manejar selección de proveedores
  const handleProviderDeleteCheck = (id) => {
    setSelectedProvidersToDelete((prev) =>
      prev.includes(id) ? prev.filter((pid) => pid !== id) : [...prev, id]
    );
  };

  // Confirmar eliminación
  const confirmDelete = async () => {
    setDeleteError(null);
    setDeleteSuccess(null);
    if (!product) return;
    const providerIds = product.providerIds || (product.providers ? product.providers.map(p => p.id) : []);
    if (!Array.isArray(providerIds) || providerIds.length === 0 || selectedProvidersToDelete.length === 0 || selectedProvidersToDelete.length === providerIds.length) {
      try {
        await deleteProduct(deletingId);
        setDeleteSuccess("Producto eliminado correctamente.");
        setTimeout(() => {
          setDeleteModalOpen(false);
          setDeletingId(null);
          setDeleteSuccess(null);
          fetchProducts({ page, size, sort });
        }, 1200);
      } catch (err) {
        setDeleteError(err?.message || "Error al eliminar producto");
      }
    } else {
      try {
        for (const providerId of selectedProvidersToDelete) {
          await deleteFromProvider(deletingId, providerId);
        }
        setDeleteSuccess("Producto eliminado correctamente del/los proveedor(es).");
        setTimeout(() => {
          setDeleteModalOpen(false);
          setDeletingId(null);
          setDeleteSuccess(null);
          fetchProducts({ page, size, sort });
        }, 1200);
      } catch (err) {
        setDeleteError(err?.message || "Error al eliminar del proveedor");
      }
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
        <div className="flex flex-wrap justify-between items-center gap-2">
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
          <div className="flex items-center gap-2">
            <button
              className="bg-primary text-white px-4 py-2 rounded shadow hover:bg-primary-dark transition"
              onClick={handleOpenCreate}
            >
              Crear producto
            </button>
            <button
              className="border-primary border-2 text-text px-4 py-2 rounded shadow hover:bg-secondary-dark transition"
              onClick={handleDownloadTemplate}
              disabled={loadingTemplate}
              type="button"
            >
              {loadingTemplate ? "Descargando..." : "Descargar plantilla CSV"}
            </button>
            <button
              className="bg-green-600 text-white px-4 py-2 rounded shadow hover:bg-green-700 transition"
              onClick={() => setImportModalOpen(true)}
              type="button"
            >
              Importar productos CSV
            </button>
                  {/* Modal de importación fuera del contenedor de botones para evitar problemas de layout */}
                  <Modal open={importModalOpen} onClose={() => {
                    setImportModalOpen(false);
                    setTimeout(() => {
                      // Limpiar feedback al cerrar
                      if (errorImport) setTimeout(() => window.location.reload(), 1000);
                    }, 300);
                  }}>
                    <form className="space-y-4" onSubmit={handleImportCsv}>
                      <h2 className="text-lg font-bold">Importar productos por CSV</h2>
                      <div>
                        <label className="block text-sm font-medium mb-1">Archivo CSV</label>
                        <div className="flex items-center gap-2">
                          <label htmlFor="csv-upload" className="cursor-pointer bg-primary text-white px-4 py-2 rounded shadow hover:bg-primary-dark transition">
                            Seleccionar archivo
                          </label>
                          <input
                            id="csv-upload"
                            type="file"
                            accept=".csv"
                            onChange={handleCsvFileChange}
                            required
                            className="hidden"
                          />
                          <span className="text-sm text-gray-700">
                            {csvFile ? csvFile.name : "Ningún archivo seleccionado"}
                          </span>
                        </div>
                        <span className="text-xs text-gray-500">Selecciona el archivo CSV a importar.</span>
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-1">Asignar proveedores a todos (opcional)</label>
                        <div className="max-h-32 overflow-y-auto border rounded p-2">
                          {providers.map((prov) => (
                            <label key={prov.id} className="flex items-center gap-2 text-sm">
                              <input
                                type="checkbox"
                                checked={selectedProviders.includes(prov.id)}
                                onChange={() => handleProviderCheck(prov.id)}
                              />
                              {prov.name}
                            </label>
                          ))}
                        </div>
                        <span className="text-xs text-gray-500">Si seleccionas proveedores aquí, el campo provider_ids del CSV se sobrescribirá para todos los productos importados, separados por punto y coma (;).</span>
                      </div>
                      {(errorImport || successImport) && (
                        <div className={`text-sm py-2 rounded ${errorImport ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}
                             style={{textAlign: 'center'}}>
                          {errorImport || successImport}
                        </div>
                      )}
                      <div className="flex justify-end gap-2">
                        <button
                          type="button"
                          className="px-3 py-1 rounded border"
                          onClick={() => {
                            setImportModalOpen(false);
                            setTimeout(() => {
                              // Limpiar feedback al cerrar
                            }, 300);
                          }}
                          disabled={loadingImport}
                        >Cerrar</button>
                        <button
                          type="submit"
                          className="px-4 py-1 rounded bg-green-600 text-white"
                          disabled={loadingImport || !csvFile}
                        >{loadingImport ? "Importando..." : "Importar"}</button>
                      </div>
                    </form>
                  </Modal>
          </div>
        </div>
        {errorTemplate && <div className="text-red-600 text-sm mt-1">{errorTemplate}</div>}
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
            {product && (Array.isArray(product.providerIds) && product.providerIds.length > 0 || (product.providers && product.providers.length > 0)) && (
              <div>
                <label className="block text-sm font-medium mb-1">Selecciona proveedores para eliminar el producto:</label>
                <div className="max-h-32 overflow-y-auto border rounded p-2">
                  <label className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={selectedProvidersToDelete.length === (product.providerIds ? product.providerIds.length : product.providers.length)}
                      onChange={() => {
                        const allIds = product.providerIds || (product.providers ? product.providers.map(p => p.id) : []);
                        if (selectedProvidersToDelete.length === allIds.length) {
                          setSelectedProvidersToDelete([]);
                        } else {
                          setSelectedProvidersToDelete(allIds);
                        }
                      }}
                    />
                    <b>Todos los proveedores</b>
                  </label>
                  {(product.providers || (product.providerIds || [])).map((prov) => {
                    const id = prov.id || prov;
                    const name = prov.name || providers.find(p => p.id === id)?.name || id;
                    return (
                      <label key={id} className="flex items-center gap-2 text-sm">
                        <input
                          type="checkbox"
                          checked={selectedProvidersToDelete.includes(id)}
                          onChange={() => handleProviderDeleteCheck(id)}
                        />
                        {name}
                      </label>
                    );
                  })}
                </div>
                <span className="text-xs text-gray-500">Si seleccionas todos, se eliminará el producto globalmente.</span>
              </div>
            )}
            {deleteError && <div className="text-red-600 text-sm">{deleteError}</div>}
            {deleteSuccess && <div className="text-green-600 text-sm">{deleteSuccess}</div>}
            <div className="flex justify-end gap-2">
              <button
                className="px-3 py-1 rounded border"
                onClick={() => setDeleteModalOpen(false)}
                disabled={loadingDeleteProvider}
              >Cancelar</button>
              <button
                className="px-4 py-1 rounded bg-red-600 text-white"
                onClick={confirmDelete}
                disabled={loadingDeleteProvider}
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