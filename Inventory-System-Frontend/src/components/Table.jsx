
/**
 * Componente Table reutilizable
 * Recibe un array de objetos (data) y genera columnas dinámicamente.
 * 
 */
export default function Table({ data = [], onAction, branchId }) {
    if (!Array.isArray(data) || data.length === 0) {
        return (
            <div className="w-full text-center py-8 text-gray-500">No hay datos para mostrar.</div>
        );
    }

    // Obtener las columnas dinámicamente del primer objeto
    const columns = Object.keys(data[0]);

    return (
        <div className="overflow-x-auto w-full">
            <table className="min-w-full bg-white border border-gray-200 rounded-lg">
                <thead>
                    <tr>
                        {columns.map((col) => (
                            <th
                                key={col}
                                className="px-4 py-2 bg-primary text-left text-xs font-bold text-text uppercase border-b"
                            >
                                {col}
                            </th>
                        ))}
                        <th className="px-4 py-2 bg-primary text-left text-xs font-bold text-text uppercase border-b">Acción</th>
                    </tr>
                </thead>
                <tbody>
                    {data.map((row, idx) => (
                        <tr key={idx} className="hover:bg-gray-50">
                            {columns.map((col) => (
                                <td key={col} className="px-4 py-2 border-b text-sm text-gray-700">
                                    {row[col]}
                                </td>
                            ))}
                            <td className="px-4 py-2 border-b text-sm text-gray-700">
                                {/* Icono de ver detalles (lupa) usando SVG inline */}
                                <button
                                    title="Ver detalles"
                                    className="hover:text-primary focus:outline-none"
                                    onClick={() => onAction && onAction({ branchId, productId: row.productId })}
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35m0 0A7.5 7.5 0 104.5 4.5a7.5 7.5 0 0012.15 12.15z" />
                                    </svg>
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}