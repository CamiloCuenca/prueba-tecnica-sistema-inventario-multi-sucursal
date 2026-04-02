import { useEffect, useState } from "react";
import Table from '../../components/Table';
import TablePaginator from '../../components/TablePaginator';
import { useInventory } from './useInventory';

export default function TableInventory() {
  const { handleInventory, loading, error } = useInventory();
  const [inventory, setInventory] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(20); 
  const [totalPages, setTotalPages] = useState(1);
  const [isFirst, setIsFirst] = useState(true);
  const [isLast, setIsLast] = useState(true);

  const fetchData = (pageToFetch) => {
    handleInventory({ page: pageToFetch, size }).then((res) => {
      if (res && Array.isArray(res.content)) {
        setInventory(res.content);
        setTotalPages(res.totalPages || 1);
        setIsFirst(res.first);
        setIsLast(res.last);
      }
    });
  };

  useEffect(() => {
    fetchData(page);
    // eslint-disable-next-line
  }, [page]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
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
      <Table data={inventory} />
      <TablePaginator
        page={page}
        totalPages={totalPages}
        onPageChange={handlePageChange}
        isFirst={isFirst}
        isLast={isLast}
      />
    </div>
  );
}