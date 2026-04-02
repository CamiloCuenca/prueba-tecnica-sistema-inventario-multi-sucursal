
/**
 * Componente Table reutilizable
 * Recibe un array de objetos (data) y genera columnas dinámicamente.
 * 
 */
export default function Table({ data = [] }) {
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
                                className="px-4 py-2 bg-primary text-left text-xs font-semibold text-gray-700 uppercase border-b"
                            >
                                {col}
                            </th>
                        ))}
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
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}