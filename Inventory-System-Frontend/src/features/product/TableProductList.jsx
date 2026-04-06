import React, { useEffect, useState } from "react";
import Table from "../../components/Table";
import TablePaginator from "../../components/TablePaginator";
import LoadingSpinner from "../../components/LoadingSpinner";
import { useProductList } from "./useProductList";

export default function TableProductList() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [sort] = useState([]); // Puedes agregar lógica de ordenamiento si lo necesitas
  const { products, pageInfo, loading, error, fetchProducts } = useProductList();

  useEffect(() => {
    fetchProducts({ page, size, sort });
    // eslint-disable-next-line
  }, [page, size, sort]);

  if (loading) return <LoadingSpinner label="Cargando productos..." />;
  if (error) return <div className="text-red-600">{error}</div>;

  return (
    <div className="space-y-4">
      <Table
        data={products}
        searchable={true}
        searchFields={["name", "sku"]}
      />
      <TablePaginator
        page={page}
        totalPages={pageInfo.totalPages || 1}
        onPageChange={setPage}
        isFirst={page === 0}
        isLast={page >= (pageInfo.totalPages || 1) - 1}
      />
    </div>
  );
}