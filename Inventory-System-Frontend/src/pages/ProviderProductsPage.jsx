import { useLocation, useParams } from 'react-router-dom';
import Breadcrumbs from '../components/Breadcrumbs';
import ProviderProducts from '../features/providers/ProviderProducts';

export default function ProviderProductsPage() {
  const { id } = useParams();
  const location = useLocation();
  const providerName = location.state?.providerName || '';

  return (
    <div className="p-6">
      <Breadcrumbs
        backTo={`/providers/${id}`}
        backLabel="Volver al detalle"
        items={[
          { label: 'Proveedores', to: '/providers' },
          { label: providerName || 'Proveedor', to: `/providers/${id}` },
          { label: 'Productos' },
        ]}
      />
      <ProviderProducts providerId={id} providerName={providerName} />
    </div>
  );
}