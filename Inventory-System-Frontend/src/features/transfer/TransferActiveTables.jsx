import { useState } from 'react';
import BranchList from '../inventory/BranchList';
import { decodeJWT } from '../../utils/jwt';
import DispatchTransferModal from './components/DispatchTransferModal';
import PrepareTransferModal from './components/PrepareTransferModal';
import TransfersTable from './components/TransfersTable';
import { dispatchTransfer, getTransferById, prepareTransfer } from './transferApi';
import {
  carrierOptions,
  getCurrentLocalDateTime,
  getErrorMessage,
  getProductId,
  getRequestedQuantity,
  getStatusCode,
  getTransferItems,
  normalizeTransfers,
} from './transferUiUtils';
import { useTransferLists } from './useTransferLists';

const createInitialDispatchForm = () => ({
  carrier: carrierOptions[0],
  estimatedArrival: getCurrentLocalDateTime(),
  routeId: '',
  routePriority: 'MEDIUM',
  routeCost: '',
});

const transferStatusOptions = [
  { value: '', label: 'Todos' },
  { value: 'PENDING', label: 'PENDING' },
  { value: 'PREPARING', label: 'PREPARING' },
  { value: 'SHIPPED', label: 'SHIPPED' },
  { value: 'PARTIALLY_SHIPPED', label: 'PARTIALLY_SHIPPED' },
  { value: 'IN_TRANSIT', label: 'IN_TRANSIT' },
  { value: 'PARTIALLY_RECEIVED', label: 'PARTIALLY_RECEIVED' },
  { value: 'RECEIVED', label: 'RECEIVED' },
  { value: 'CANCELLED', label: 'CANCELLED' },
];

export default function TransferActiveTables() {
  const token = sessionStorage.getItem('token') || sessionStorage.getItem('authToken');
  const payload = decodeJWT(token);
  const role = payload?.role || null;
  const tokenBranchId = payload?.branchId || payload?.branch_id || payload?.branch || null;
  const isAdmin = role === 'ADMIN';

  const [selectedBranchId, setSelectedBranchId] = useState(tokenBranchId);
  const [incomingStatusFilter, setIncomingStatusFilter] = useState('PENDING');
  const [outgoingStatusFilter, setOutgoingStatusFilter] = useState('RECEIVED');

  const {
    incomingTransfers,
    outgoingTransfers,
    incomingPageInfo,
    outgoingPageInfo,
    incomingLoading,
    outgoingLoading,
    incomingError,
    outgoingError,
    setIncomingPage,
    setOutgoingPage,
    loadOutgoing,
  } = useTransferLists({
    branchId: isAdmin ? selectedBranchId : undefined,
    incomingStatus: incomingStatusFilter,
    outgoingStatus: outgoingStatusFilter,
    enabled: !isAdmin || Boolean(selectedBranchId),
  });

  const [prepareModalOpen, setPrepareModalOpen] = useState(false);
  const [prepareLoading, setPrepareLoading] = useState(false);
  const [prepareSubmitting, setPrepareSubmitting] = useState(false);
  const [prepareError, setPrepareError] = useState(null);
  const [prepareSuccess, setPrepareSuccess] = useState(null);
  const [selectedOutgoingTransfer, setSelectedOutgoingTransfer] = useState(null);
  const [confirmedItems, setConfirmedItems] = useState({});

  const [dispatchModalOpen, setDispatchModalOpen] = useState(false);
  const [dispatchSubmitting, setDispatchSubmitting] = useState(false);
  const [dispatchError, setDispatchError] = useState(null);
  const [selectedDispatchTransfer, setSelectedDispatchTransfer] = useState(null);
  const [dispatchForm, setDispatchForm] = useState(createInitialDispatchForm());

  const incomingRows = normalizeTransfers(incomingTransfers);
  const outgoingRows = normalizeTransfers(outgoingTransfers);

  const closePrepareModal = () => {
    setPrepareModalOpen(false);
    setPrepareLoading(false);
    setPrepareSubmitting(false);
    setPrepareError(null);
    setPrepareSuccess(null);
    setSelectedOutgoingTransfer(null);
    setConfirmedItems({});
  };

  const closeDispatchModal = () => {
    setDispatchModalOpen(false);
    setDispatchSubmitting(false);
    setDispatchError(null);
    setSelectedDispatchTransfer(null);
    setDispatchForm(createInitialDispatchForm());
  };

  const initializeConfirmedItems = (transfer) => {
    const items = getTransferItems(transfer);
    const next = {};

    items.forEach((item) => {
      const productId = getProductId(item);
      if (productId && productId !== '-') {
        next[productId] = getRequestedQuantity(item);
      }
    });

    setConfirmedItems(next);
  };

  const handleOpenPrepareModal = async (row) => {
    if (!row?.id || row.id === '-') return;

    setPrepareModalOpen(true);
    setPrepareLoading(true);
    setPrepareError(null);
    setPrepareSuccess(null);

    try {
      const transferDetail = await getTransferById(row.id);
      setSelectedOutgoingTransfer(transferDetail);
      initializeConfirmedItems(transferDetail);
    } catch (err) {
      setPrepareError(getErrorMessage(err, 'Error al cargar detalle de la transferencia'));
    } finally {
      setPrepareLoading(false);
    }
  };

  const handleOpenDispatchModal = (row) => {
    const transfer = row._raw || row;
    const fallbackRoute = `${row.origen || 'ORIGEN'}-${row.destino || 'DESTINO'}`;

    setSelectedDispatchTransfer(transfer);
    setDispatchForm({
      ...createInitialDispatchForm(),
      routeId: fallbackRoute,
    });
    setDispatchError(null);
    setDispatchModalOpen(true);
  };

  const handleDispatchFieldChange = (field, value) => {
    setDispatchForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleConfirmedQuantityChange = (productId, value) => {
    const parsed = Number(value);
    const safeValue = Number.isNaN(parsed) ? 0 : Math.max(0, parsed);
    setConfirmedItems((prev) => ({ ...prev, [productId]: safeValue }));
  };

  const handleSubmitPrepare = async (event) => {
    event.preventDefault();

    if (!selectedOutgoingTransfer?.id) {
      setPrepareError('No se pudo identificar la transferencia a preparar.');
      return;
    }

    const items = getTransferItems(selectedOutgoingTransfer).map((item) => ({
      productId: getProductId(item),
      quantityConfirmed: Number(confirmedItems[getProductId(item)] ?? 0),
    }));

    const preparePayload = { items };

    console.log('Prepare transfer payload:', {
      transferId: selectedOutgoingTransfer.id,
      body: preparePayload,
    });

    if (items.length === 0) {
      setPrepareError('La transferencia no tiene items para preparar.');
      return;
    }

    setPrepareSubmitting(true);
    setPrepareError(null);
    setPrepareSuccess(null);

    try {
      await prepareTransfer(selectedOutgoingTransfer.id, preparePayload);
      setPrepareSuccess('Transferencia preparada correctamente.');
      await loadOutgoing();
    } catch (err) {
      setPrepareError(getErrorMessage(err, 'Error al preparar la transferencia'));
    } finally {
      setPrepareSubmitting(false);
    }
  };

  const handleSubmitDispatch = async (event) => {
    event.preventDefault();

    if (!selectedDispatchTransfer?.id) {
      setDispatchError('No se pudo identificar la transferencia a despachar.');
      return;
    }

    const cost = Number(dispatchForm.routeCost);

    if (!dispatchForm.carrier) {
      setDispatchError('Debes seleccionar un transportista.');
      return;
    }
    if (!dispatchForm.estimatedArrival) {
      setDispatchError('Debes seleccionar la fecha/hora estimada de llegada.');
      return;
    }
    if (!dispatchForm.routeId.trim()) {
      setDispatchError('Debes ingresar un identificador de ruta.');
      return;
    }
    if (!dispatchForm.routePriority) {
      setDispatchError('Debes seleccionar prioridad de ruta.');
      return;
    }
    if (!Number.isFinite(cost) || cost <= 0) {
      setDispatchError('El costo de ruta debe ser un valor positivo.');
      return;
    }

    const payload = {
      carrier: dispatchForm.carrier,
      estimatedArrival: new Date(dispatchForm.estimatedArrival).toISOString(),
      routeId: dispatchForm.routeId.trim(),
      routePriority: dispatchForm.routePriority,
      routeCost: cost,
    };

    setDispatchSubmitting(true);
    setDispatchError(null);

    try {
      await dispatchTransfer(selectedDispatchTransfer.id, payload);
      closeDispatchModal();
      await loadOutgoing();
    } catch (err) {
      setDispatchError(getErrorMessage(err, 'Error al registrar el despacho de la transferencia'));
    } finally {
      setDispatchSubmitting(false);
    }
  };

  const renderOutgoingAction = (row) => {
    const statusCode = getStatusCode(row?._raw?.status);

    if (statusCode === 'RECEIVED') {
      return null;
    }

    const canDispatch = statusCode === 'PREPARADO' || statusCode === 'SHIPPED';

    if (canDispatch) {
      return (
        <button
          type="button"
          onClick={() => handleOpenDispatchModal(row)}
          className="rounded px-3 py-1 bg-primary text-white text-xs font-medium"
        >
          Despachar
        </button>
      );
    }

    return (
      <button
        type="button"
        onClick={() => handleOpenPrepareModal(row)}
        className="rounded px-3 py-1 bg-primary text-white text-xs font-medium"
      >
        Preparar envío
      </button>
    );
  };

  return (
    <>
      <div className="space-y-6">
        {isAdmin && (
          <div className="space-y-3">
            <h2 className="text-lg font-semibold text-gray-900">Selecciona sucursal</h2>
            <BranchList
              selectedBranchId={selectedBranchId}
              onBranchSelect={setSelectedBranchId}
            />
            {!selectedBranchId && (
              <p className="text-sm text-gray-600">Selecciona una sucursal para ver sus transferencias activas.</p>
            )}
          </div>
        )}

        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <label className="text-sm font-medium text-gray-700" htmlFor="incoming-status-filter">
            Estado (Entrantes)
          </label>
          <select
            id="incoming-status-filter"
            value={incomingStatusFilter}
            onChange={(event) => setIncomingStatusFilter(event.target.value)}
            className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none sm:w-72"
          >
            {transferStatusOptions.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <TransfersTable
          title="Transferencias Entrantes Activas"
          rows={incomingRows}
          loading={incomingLoading}
          error={incomingError}
          pageInfo={incomingPageInfo}
          onPageChange={setIncomingPage}
        />

        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <label className="text-sm font-medium text-gray-700" htmlFor="outgoing-status-filter">
            Estado (Salientes)
          </label>
          <select
            id="outgoing-status-filter"
            value={outgoingStatusFilter}
            onChange={(event) => setOutgoingStatusFilter(event.target.value)}
            className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none sm:w-72"
          >
            {transferStatusOptions.map((option) => (
              <option key={option.label} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <TransfersTable
          title="Transferencias Salientes Activas"
          rows={outgoingRows}
          loading={outgoingLoading}
          error={outgoingError}
          pageInfo={outgoingPageInfo}
          onPageChange={setOutgoingPage}
          showAction
          renderAction={renderOutgoingAction}
        />
      </div>

      <PrepareTransferModal
        open={prepareModalOpen}
        onClose={closePrepareModal}
        loading={prepareLoading}
        transfer={selectedOutgoingTransfer}
        confirmedItems={confirmedItems}
        onQuantityChange={handleConfirmedQuantityChange}
        onSubmit={handleSubmitPrepare}
        error={prepareError}
        success={prepareSuccess}
        submitting={prepareSubmitting}
      />

      <DispatchTransferModal
        open={dispatchModalOpen}
        onClose={closeDispatchModal}
        transfer={selectedDispatchTransfer}
        dispatchForm={dispatchForm}
        onFieldChange={handleDispatchFieldChange}
        onSubmit={handleSubmitDispatch}
        submitting={dispatchSubmitting}
        error={dispatchError}
      />
    </>
  );
}
