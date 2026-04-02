import React from "react";

/**
 * Componente TablePaginator
 * Renderiza controles de paginación reutilizables
 * Props:
 *  - page: número de página actual (0-indexed)
 *  - totalPages: total de páginas
 *  - onPageChange: función(page) => void
 *  - isFirst: boolean
 *  - isLast: boolean
 */
export default function TablePaginator({ page, totalPages, onPageChange, isFirst, isLast }) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex justify-center items-center gap-2 py-4">
      <button
        className="px-3 py-1 rounded bg-gray-200 text-gray-700 disabled:opacity-50"
        onClick={() => onPageChange(page - 1)}
        disabled={isFirst}
      >
        Anterior
      </button>
      <span className="mx-2 text-sm text-gray-700">
        Página {page + 1} de {totalPages}
      </span>
      <button
        className="px-3 py-1 rounded bg-gray-200 text-gray-700 disabled:opacity-50"
        onClick={() => onPageChange(page + 1)}
        disabled={isLast}
      >
        Siguiente
      </button>
    </div>
  );
}
