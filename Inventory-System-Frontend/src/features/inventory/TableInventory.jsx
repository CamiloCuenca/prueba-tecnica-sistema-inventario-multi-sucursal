import { useEffect, useState } from "react";
import Table from '../../components/Table';
import TablePaginator from '../../components/TablePaginator';
import Modal from '../../components/Modal';
import ProductCard from '../product/productCard';
import { useProduct } from '../product/useProduct';
import { useInventory } from './useInventory';

function normalizeInventoryResponse(response) {
  if (Array.isArray(response)) {
    return {
      content: response,
      totalPages: 1,
      first: true,
      last: true,
    };
  }

  return {
    content: Array.isArray(response?.content) ? response.content : [],
    totalPages: response?.totalPages || 1,
    first: response?.first ?? true,
    last: response?.last ?? true,
  };
}

export default function TableInventory({ branchId }) {
  const { handleInventory, handleBranchInventory, loading, error } = useInventory();
  const { handleProducts } = useProduct();
  const [inventory, setInventory] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [totalPages, setTotalPages] = useState(1);
  const [isFirst, setIsFirst] = useState(true);
  const [isLast, setIsLast] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [productLoading, setProductLoading] = useState(false);
  const [productError, setProductError] = useState(null);

  const fetchData = (pageToFetch) => {
    const fetch = branchId
      ? handleBranchInventory({ branchId, page: pageToFetch, size })
      : handleInventory({ page: pageToFetch, size });
    fetch.then((res) => {
      if (!res) return;

      const normalized = normalizeInventoryResponse(res);
      setInventory(normalized.content);
      setTotalPages(normalized.totalPages);
      setIsFirst(normalized.first);
      setIsLast(normalized.last);
    });
  };

  useEffect(() => {
    setPage(0); // Resetear a la primera página al cambiar branch
  }, [branchId]);

  useEffect(() => {
    fetchData(page);
    // eslint-disable-next-line
  }, [page, branchId]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  const handleShowDetails = async ({ branchId, productId }) => {
    setProductLoading(true);
    setProductError(null);
    setModalOpen(true);
    try {
      console.log('Solicitando detalles', { branchId, productId });
      const product = await handleProducts({ branchId, productId });
      console.log('Respuesta producto', product);
      if (!product) {
        setProductError("No se encontró el producto o la respuesta está vacía.");
        setSelectedProduct(null);
      } else {
        setSelectedProduct(product);
      }
    } catch (err) {
      setProductError("Error al obtener los detalles del producto");
      setSelectedProduct(null);
    } finally {
      setProductLoading(false);
    }
  };

  if (loading) {
    return <div className="w-full text-center py-8 text-gray-500">Cargando inventario...</div>;
  }
  if (error) {
    return <div className="w-full text-center py-8 text-red-500">{error}</div>;
  }

  return (
    <div>
      <Table data={inventory} onAction={handleShowDetails} branchId={branchId} />
      <TablePaginator
        page={page}
        totalPages={totalPages}
        onPageChange={handlePageChange}
        isFirst={isFirst}
        isLast={isLast}
      />
      <Modal open={modalOpen} onClose={() => { setModalOpen(false); setSelectedProduct(null); }}>
        {productLoading && <div className="text-center py-4 text-gray-500">Cargando detalles...</div>}
        {productError && <div className="text-center py-4 text-red-500">{productError}</div>}
        {selectedProduct && !productLoading && !productError && (
          <ProductCard product={selectedProduct} />
        )}
        {!selectedProduct && !productLoading && !productError && (
          <div className="text-center py-4 text-gray-500">No se encontraron detalles para este producto.</div>
        )}
      </Modal>
    </div>
  );
}