import TransfersTable from '../components/TransfersTable';

export default function TransferTables({
  incomingRows,
  outgoingRows,
  incomingLoading,
  outgoingLoading,
  incomingError,
  outgoingError,
  incomingPageInfo,
  outgoingPageInfo,
  setIncomingPage,
  setOutgoingPage,
  renderIncomingAction,
  renderOutgoingAction
}) {
  return (
    <>
      <TransfersTable
        title="Transferencias Entrantes Activas"
        rows={incomingRows}
        loading={incomingLoading}
        error={incomingError}
        pageInfo={incomingPageInfo}
        onPageChange={setIncomingPage}
        showAction
        renderAction={renderIncomingAction}
      />
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
    </>
  );
}
