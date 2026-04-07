import { useState } from 'react';
import BranchSelector from './components/BranchSelector';
import StatusFilter from './components/StatusFilter';
import { decodeJWT } from '../../utils/jwt';
import DispatchTransferModal from './components/DispatchTransferModal';
import PrepareTransferModal from './components/PrepareTransferModal';
import TransferTables from './components/TransferTables';
import ReceiveTransferModal from './components/ReceiveTransferModal';
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
import { getRoleFromToken } from '../../utils/tokenUtils';


export default function TransferActiveTables() {
  // State for ReceiveTransferModal
  const [receiveModalOpen, setReceiveModalOpen] = useState(false);
  const [receiveLoading, setReceiveLoading] = useState(false);
  const [receiveError, setReceiveError] = useState(null);
  const [receiveTransfer, setReceiveTransfer] = useState(null);

  const handleOpenReceiveModal = async (row) => {
    setReceiveModalOpen(true);
    setReceiveLoading(true);
    setReceiveError(null);
    setReceiveTransfer(null);
    try {
      const transferDetail = await getTransferById(row.id);
      setReceiveTransfer(transferDetail);
    } catch (err) {
      setReceiveError(getErrorMessage(err, 'Error al cargar detalle de la transferencia'));
    } finally {
      setReceiveLoading(false);
    }
  };

  const handleCloseReceiveModal = () => {
    setReceiveModalOpen(false);
    setReceiveTransfer(null);
    setReceiveError(null);
    setReceiveLoading(false);
  };

  const handleConfirmReceive = () => {
    // Aquí irá la lógica real de confirmación
    // Por ahora solo cierra el modal
    setReceiveModalOpen(false);
  };


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


  const token = sessionStorage.getItem('token') || sessionStorage.getItem('authToken');
  const payload = decodeJWT(token);
  const role = getRoleFromToken() || payload?.role || null;
  const tokenBranchId = payload?.branchId || payload?.branch_id || payload?.branch || null;
  const isAdmin = role === 'ADMIN';

  // State for PrepareTransferModal
  const [prepareModalOpen, setPrepareModalOpen] = useState(false);
  const [prepareLoading, setPrepareLoading] = useState(false);
  const [prepareSubmitting, setPrepareSubmitting] = useState(false);
  const [prepareError, setPrepareError] = useState(null);
  const [prepareSuccess, setPrepareSuccess] = useState(null);
  const [selectedOutgoingTransfer, setSelectedOutgoingTransfer] = useState(null);
  const [confirmedItems, setConfirmedItems] = useState({});
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

  // Acción para transferencias entrantes: mostrar botón 'Recibir' si estado es IN_TRANSIT
  const renderIncomingAction = (row) => {
    if (row.estado === 'IN_TRANSIT') {
      return (
        <button
          type="button"
          className="rounded px-3 py-1 bg-green-600 text-white text-xs font-medium"
          onClick={() => handleOpenReceiveModal(row)}
        >
          Recibir
        </button>
      );
    }
    return null;
  };

  // Acción para transferencias salientes: mostrar botón 'Preparar envío' o 'Despachar' según el estado
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
        <BranchSelector
          isAdmin={isAdmin}
          selectedBranchId={selectedBranchId}
          setSelectedBranchId={setSelectedBranchId}
        />

        <StatusFilter
          label="Estado (Entrantes)"
          id="incoming-status-filter"
          value={incomingStatusFilter}
          onChange={(event) => setIncomingStatusFilter(event.target.value)}
          options={transferStatusOptions}
        />


        <StatusFilter
          label="Estado (Salientes)"
          id="outgoing-status-filter"
          value={outgoingStatusFilter}
          onChange={(event) => setOutgoingStatusFilter(event.target.value)}
          options={transferStatusOptions}
        />


         <TransferTables
          incomingRows={incomingRows}
          outgoingRows={outgoingRows}
          incomingLoading={incomingLoading}
          outgoingLoading={outgoingLoading}
          incomingError={incomingError}
          outgoingError={outgoingError}
          incomingPageInfo={incomingPageInfo}
          outgoingPageInfo={outgoingPageInfo}
          setIncomingPage={setIncomingPage}
          setOutgoingPage={setOutgoingPage}
          renderIncomingAction={renderIncomingAction}
          renderOutgoingAction={renderOutgoingAction}
        />
        <ReceiveTransferModal
          open={receiveModalOpen}
          onClose={handleCloseReceiveModal}
          transfer={receiveTransfer}
          loading={receiveLoading}
          error={receiveError}
          onConfirm={handleConfirmReceive}
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
