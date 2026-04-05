import ProviderList from '../features/providers/ProviderList';
import Breadcrumbs from '../components/Breadcrumbs';

export default function ProvidersPage() {
  return (
    <div className="p-6">
      <Breadcrumbs items={[{ label: 'Proveedores' }]} />
      <ProviderList />
    </div>
  );
}