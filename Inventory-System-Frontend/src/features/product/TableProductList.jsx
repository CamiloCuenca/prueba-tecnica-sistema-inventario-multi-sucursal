import React, { useEffect, useState } from "react";
import Table from "../../components/Table";
import TablePaginator from "../../components/TablePaginator";
import LoadingSpinner from "../../components/LoadingSpinner";
import { useProductList } from "./useProductList";
import { useProductById } from "./useProductById";
import Modal from "../../components/Modal";
import { useEffect as useEffectBase, useState as useStateBase } from "react";
import { getProviders } from "../providers/providersApi";

export default function TableProductList() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [sort] = useState([]); // Puedes agregar lógica de ordenamiento si lo necesitas
  const { products, pageInfo, loading, error, fetchProducts } = useProductList();

  // Proveedores para el filtro
  const [providers, setProviders] = useStateBase([]);
  const [providerId, setProviderId] = useStateBase("");
  const [loadingProviders, setLoadingProviders] = useStateBase(false);
  const [errorProviders, setErrorProviders] = useStateBase(null);

  useEffectBase(() => {
    setLoadingProviders(true);
    getProviders({ size: 100 }).then((data) => {
      setProviders(data.content || []);
      setLoadingProviders(false);
    }).catch((err) => {
      setErrorProviders("Error al cargar proveedores");
      setLoadingProviders(false);
    });
  }, []);

  // Modal de detalle
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const { product, fetchProduct, loading: loadingDetail, error: errorDetail } = useProductById();

  useEffect(() => {
    // Si hay providerId, filtrar productos por ese proveedor
    // Suponiendo que el backend soporta filtro por providerId, si no, filtra en frontend
    fetchProducts({ page, size, sort });
    // eslint-disable-next-line
  }, [page, size, sort]);

  // Cuando cambia el id seleccionado y se abre el modal, busca el detalle
  useEffect(() => {
    if (modalOpen && selectedId) {
      console.log('Buscando detalle para id:', selectedId);
      fetchProduct(selectedId).then((data) => {
        console.log('Respuesta del detalle:', data);
      });
    }
    // eslint-disable-next-line
  }, [modalOpen, selectedId]);

  const handleAction = (row) => {
    console.log('Fila seleccionada:', row);
    // Usar productId si existe, si no id
    setSelectedId(row.productId || row.id);
    setModalOpen(true);
  };

  if (loading) return <LoadingSpinner label="Cargando productos..." />;
  if (error) return <div className="text-red-600">{error}</div>;

  // Filtrado en frontend si providerId está seleccionado
  const filteredProducts = providerId
    ? products.filter((p) => (Array.isArray(p.providerIds) ? p.providerIds.includes(providerId) : false))
    : products;

  return (
    <div className="space-y-4">
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
      <Table
        data={filteredProducts}
        searchable={true}
        searchFields={["name", "sku"]}
        onAction={handleAction}
      />
      <TablePaginator
        page={page}
        totalPages={pageInfo.totalPages || 1}
        onPageChange={setPage}
        isFirst={page === 0}
        isLast={page >= (pageInfo.totalPages || 1) - 1}
      />
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
  );
}