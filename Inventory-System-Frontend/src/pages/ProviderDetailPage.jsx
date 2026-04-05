import { useParams } from 'react-router-dom';
import Breadcrumbs from '../components/Breadcrumbs';
import ProviderDetail from '../features/providers/ProviderDetail';

export default function ProviderDetailPage() {
  const { id } = useParams();

  return (
    <div className="p-6">
      <Breadcrumbs
        backTo="/providers"
        backLabel="Volver a proveedores"
        items={[
          { label: 'Proveedores', to: '/providers' },
          { label: 'Detalle del proveedor' },
        ]}
      />
      <ProviderDetail providerId={id} />
    </div>
  );
}