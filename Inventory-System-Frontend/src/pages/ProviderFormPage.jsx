import { useParams } from 'react-router-dom';
import Breadcrumbs from '../components/Breadcrumbs';
import ProviderForm from '../features/providers/ProviderForm';

export default function ProviderFormPage({ mode }) {
  const { id } = useParams();
  const isEdit = mode === 'edit';

  return (
    <div className="p-6">
      <Breadcrumbs
        backTo="/providers"
        backLabel="Volver a proveedores"
        items={[
          { label: 'Proveedores', to: '/providers' },
          { label: isEdit ? 'Editar proveedor' : 'Nuevo proveedor' },
        ]}
      />
      <ProviderForm providerId={isEdit ? id : null} />
    </div>
  );
}