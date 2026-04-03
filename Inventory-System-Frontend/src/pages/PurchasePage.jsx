import PurchaseTabs from '../features/purchase/PurchaseTabs';

export default function PurchasePage() {
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Compras (Ingresos por Adquisición)</h1>
      <PurchaseTabs />
    </div>
  );
}
