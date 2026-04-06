import React, { useEffect, useState } from "react";
import Table from "../../components/Table";
import TablePaginator from "../../components/TablePaginator";
import LoadingSpinner from "../../components/LoadingSpinner";
import { useProductList } from "./useProductList";
import { useProductById } from "./useProductById";
import Modal from "../../components/Modal";

export default function TableProductList() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [sort] = useState([]); // Puedes agregar lógica de ordenamiento si lo necesitas
  const { products, pageInfo, loading, error, fetchProducts } = useProductList();

  // Modal de detalle
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const { product, fetchProduct, loading: loadingDetail, error: errorDetail } = useProductById();

  useEffect(() => {
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

  return (
    <div className="space-y-4">
      <Table
        data={products}
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