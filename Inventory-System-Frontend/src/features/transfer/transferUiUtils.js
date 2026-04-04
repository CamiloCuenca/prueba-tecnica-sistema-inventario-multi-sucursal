export const getCurrentLocalDateTime = () => {
  const now = new Date();
  const timezoneOffsetMs = now.getTimezoneOffset() * 60000;
  return new Date(now.getTime() - timezoneOffsetMs).toISOString().slice(0, 16);
};

export const carrierOptions = [
  'Carlos Mendez',
  'Andres Torres',
  'Miguel Rojas',
  'Diana Ramirez',
  'Santiago Lopez',
];

export const routePriorityOptions = ['LOW', 'MEDIUM', 'HIGH'];

export const statusLabel = (status) => {
  if (!status) return 'Desconocido';
  if (typeof status === 'string') return status;
  return status.name || status.code || 'Desconocido';
};

export const branchLabel = (value) => {
  if (!value) return '-';
  if (typeof value === 'string') return value;
  return value.name || value.id || '-';
};

export const formatDate = (value) => {
  if (!value) return '-';
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return String(value);
  return parsed.toLocaleString('es-PE');
};

export const normalizeTransfers = (rows = []) => {
  return rows.map((row) => ({
    creado: formatDate(row.createdAt),
    estado: statusLabel(row.status),
    origen: row.originBranchName || branchLabel(row.originBranch || row.originBranchId),
    destino: row.destinationBranchName || branchLabel(row.destinationBranch || row.destinationBranchId),
    despachado: formatDate(row.dispatchedAt),
    llegadaEstimada: formatDate(row.estimatedArrival),
    recibido: formatDate(row.receivedAt),
    items: Number(row.totalItems || row.itemCount || row.items?.length || row.details?.length || 0),
    id: row.id || '-',
    _raw: row,
  }));
};

export const getTransferItems = (transfer) => transfer?.items || transfer?.details || [];

export const getRequestedQuantity = (item) =>
  Number(item?.quantityRequested ?? item?.requestedQuantity ?? item?.quantity ?? item?.orderedQuantity ?? 0);

export const getProductId = (item) => item?.productId || item?.id || '-';

export const getErrorMessage = (err, fallbackMessage) => {
  if (err?.response?.status === 401) {
    return 'No autorizado (401). Inicia sesión nuevamente o verifica permisos de Gerente en la sucursal origen.';
  }
  if (err?.response?.data?.message) return err.response.data.message;
  if (typeof err === 'string') return err;
  if (err?.message) return err.message;
  if (err?.error) return err.error;
  return fallbackMessage;
};

export const getStatusCode = (status) => {
  if (!status) return '';
  if (typeof status === 'string') return status.toUpperCase();
  return String(status.code || status.name || '').toUpperCase();
};
