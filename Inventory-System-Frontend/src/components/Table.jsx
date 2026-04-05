
import { useMemo, useState } from 'react';

const normalizeText = (value) => String(value ?? '').toLowerCase();

const collectSearchableText = (value) => {
    if (value == null) return '';
    if (Array.isArray(value)) return value.map(collectSearchableText).join(' ');
    if (typeof value === 'object') return Object.values(value).map(collectSearchableText).join(' ');
    return String(value);
};

/**
 * Componente Table reutilizable
 * Recibe un array de objetos (data) y genera columnas dinámicamente.
 */
export default function Table({
    data = [],
    onAction,
    branchId,
    actionKey = 'productId',
    actionPayload,
    searchable = false,
    searchPlaceholder = 'Buscar en la tabla',
    searchFields = [],
}) {
    const [search, setSearch] = useState('');

    const filteredData = useMemo(() => {
        if (!searchable) return data;

        const normalizedSearch = normalizeText(search).trim();
        if (!normalizedSearch) return data;

        return data.filter((row) => {
            if (Array.isArray(searchFields) && searchFields.length > 0) {
                return searchFields.some((field) => normalizeText(row?.[field]).includes(normalizedSearch));
            }

            return normalizeText(collectSearchableText(row)).includes(normalizedSearch);
        });
    }, [data, search, searchFields, searchable]);

    const columns = useMemo(() => {
        if (!Array.isArray(filteredData) || filteredData.length === 0) {
            return Array.isArray(data) && data.length > 0 ? Object.keys(data[0]) : [];
        }

        return Object.keys(filteredData[0]);
    }, [data, filteredData]);

    return (
        <div className="space-y-3">
            {searchable && (
                <div className="w-full max-w-md">
                    <label className="mb-1 block text-sm font-medium text-gray-700">Buscar</label>
                    <input
                        type="search"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                        placeholder={searchPlaceholder}
                        className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                    />
                </div>
            )}

            {filteredData.length === 0 ? (
                <div className="w-full text-center py-8 text-gray-500">No hay datos para mostrar.</div>
            ) : (
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
                            {filteredData.map((row, idx) => (
                                <tr key={idx} className="hover:bg-gray-50">
                                    {columns.map((col) => (
                                        <td key={col} className="px-4 py-2 border-b text-sm text-gray-700">
                                            {row[col]}
                                        </td>
                                    ))}
                                    <td className="px-4 py-2 border-b text-sm text-gray-700">
                                        <button
                                            title="Ver detalles"
                                            className="hover:text-primary focus:outline-none"
                                            onClick={() => {
                                                if (!onAction) return;

                                                if (typeof actionPayload === 'function') {
                                                    onAction(actionPayload(row, branchId));
                                                    return;
                                                }

                                                onAction({ branchId, [actionKey]: row[actionKey] ?? row.productId ?? row.id });
                                            }}
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
            )}
        </div>
    );
}