import { useEffect, useMemo, useState } from 'react';
import DeleteUserModal from './DeleteUserModal';
import UserForm from './UserForm';
import UsersListView from './UsersListView';
import { createUser, deleteUser, getBranchesForUsers, getUserById, updateUser } from './usersApi';
import { useUsers } from './useUsers';
import { getBranchIdFromToken } from '../../utils/tokenUtils';

const initialFormState = {
  id: '',
  name: '',
  email: '',
  role: 'OPERATOR',
  branchId: '',
};

const normalizeBranches = (rows = []) => {
  return rows
    .map((row) => ({
      id: row?.id || row?.branchId || row?.branch_id || row?.uuid || '',
      name: row?.name || row?.branchName || row?.branch_name || 'Sucursal sin nombre',
    }))
    .filter((branch) => branch.id);
};

const mapApiError = (error, fallbackMessage) => {
  const status = error?.status || error?.response?.status;

  if (status === 401) return { message: 'Por favor inicia sesion.' };
  if (status === 403) return { message: 'No tienes permisos para esta accion.' };
  if (status === 409) return { message: 'Email ya registrado' };
  if (status === 404) return { message: 'Sucursal no encontrada' };

  if (Array.isArray(error?.errors)) {
    const fieldErrors = {};
    error.errors.forEach((item) => {
      if (item?.field) fieldErrors[item.field] = item?.message || 'Valor invalido';
    });

    if (Object.keys(fieldErrors).length > 0) {
      return { message: 'Hay errores de validacion.', fieldErrors };
    }
  }

  if (typeof error === 'string') return { message: error };
  if (error?.message) return { message: error.message };
  if (error?.error) return { message: error.error };

  return { message: fallbackMessage };
};

export default function UsersTabs() {
  const [activeTab, setActiveTab] = useState('list');
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');

  const [formMode, setFormMode] = useState('create');
  const [formData, setFormData] = useState(initialFormState);
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const [branches, setBranches] = useState([]);
  const [branchesError, setBranchesError] = useState(null);

  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedUserForDelete, setSelectedUserForDelete] = useState(null);
  const [deleteSubmitting, setDeleteSubmitting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  const [feedback, setFeedback] = useState(null);

  const currentBranchId = getBranchIdFromToken();

  const { users, loading, error, pageInfo, refetch } = useUsers({
    page,
    size,
    roleFilter,
    search,
  });

  useEffect(() => {
    const loadBranches = async () => {
      setBranchesError(null);
      try {
        const data = await getBranchesForUsers();
        setBranches(normalizeBranches(Array.isArray(data) ? data : []));
      } catch (err) {
        setBranches([]);
        setBranchesError(mapApiError(err, 'No se pudieron cargar las sucursales').message);
      }
    };

    loadBranches();
  }, []);

  const tabs = useMemo(() => {
    const next = [{ id: 'list', label: 'Lista de usuarios' }];
    if (activeTab === 'form') {
      next.push({ id: 'form', label: formMode === 'create' ? 'Crear usuario' : 'Editar usuario' });
    }
    return next;
  }, [activeTab, formMode]);

  const openCreate = () => {
    setFeedback(null);
    setFormMode('create');
    setFormData({ ...initialFormState, role: 'OPERATOR', branchId: currentBranchId || '' });
    setFormError(null);
    setFieldErrors({});
    setActiveTab('form');
  };

  const openEdit = async (user) => {
    setFeedback(null);
    setFormMode('edit');
    setFormError(null);
    setFieldErrors({});

    try {
      const detail = await getUserById(user.id);
      setFormData({
        id: detail.id,
        name: detail.name || '',
        email: detail.email || '',
        role: detail.role || 'OPERATOR',
        branchId: detail.branchId || '',
      });
      setActiveTab('form');
    } catch (err) {
      setFeedback({ type: 'error', text: mapApiError(err, 'No se pudo obtener el usuario').message });
    }
  };

  const handleSubmitForm = async (payload) => {
    setFormSubmitting(true);
    setFormError(null);
    setFieldErrors({});

    try {
      if (formMode === 'create') {
        await createUser({
          name: payload.name,
          email: payload.email,
          password: payload.password,
          role: payload.role,
          ...(payload.branchId ? { branchId: payload.branchId } : {}),
        });
        setFeedback({ type: 'success', text: 'Usuario creado' });
      } else {
        await updateUser(payload.id, {
          id: payload.id,
          name: payload.name,
          email: payload.email,
          role: payload.role,
          ...(payload.branchId ? { branchId: payload.branchId } : {}),
        });
        setFeedback({ type: 'success', text: 'Usuario actualizado' });
      }

      setActiveTab('list');
      await refetch();
    } catch (err) {
      const mapped = mapApiError(err, formMode === 'create' ? 'No se pudo crear el usuario' : 'No se pudo actualizar el usuario');
      setFormError(mapped.message);
      setFieldErrors(mapped.fieldErrors || {});
    } finally {
      setFormSubmitting(false);
    }
  };

  const openDelete = (user) => {
    setDeleteError(null);
    setSelectedUserForDelete(user);
    setDeleteModalOpen(true);
  };

  const closeDelete = () => {
    setDeleteModalOpen(false);
    setSelectedUserForDelete(null);
    setDeleteError(null);
  };

  const handleDelete = async () => {
    if (!selectedUserForDelete?.id) return;

    setDeleteSubmitting(true);
    setDeleteError(null);

    try {
      await deleteUser(selectedUserForDelete.id);
      setFeedback({ type: 'success', text: 'Usuario eliminado' });
      closeDelete();
      await refetch();
    } catch (err) {
      setDeleteError(mapApiError(err, 'No se pudo eliminar el usuario').message);
    } finally {
      setDeleteSubmitting(false);
    }
  };

  const handleRoleFilterChange = (value) => {
    setRoleFilter(value);
    setPage(0);
  };

  const handleSearchChange = (value) => {
    setSearch(value);
    setPage(0);
  };

  return (
    <div>
      <div className="mb-6 border-b border-gray-200">
        <div className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-t-md text-sm font-semibold transition-colors ${
                activeTab === tab.id ? 'bg-primary text-white' : 'bg-surface text-white hover:bg-gray-200'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {feedback && (
        <div
          className={`mb-4 rounded border px-4 py-3 text-sm ${
            feedback.type === 'success'
              ? 'border-green-200 bg-green-50 text-green-700'
              : 'border-red-200 bg-red-50 text-red-700'
          }`}
        >
          {feedback.text}
        </div>
      )}

      {branchesError && (
        <div className="mb-4 rounded border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {branchesError}
        </div>
      )}

      <div className="bg-white rounded-b-md p-6">
        {activeTab === 'list' && (
          <UsersListView
            users={users}
            loading={loading}
            error={error}
            pageInfo={pageInfo}
            search={search}
            roleFilter={roleFilter}
            onSearchChange={handleSearchChange}
            onRoleFilterChange={handleRoleFilterChange}
            onCreate={openCreate}
            onEdit={openEdit}
            onDelete={openDelete}
            onPageChange={setPage}
          />
        )}

        {activeTab === 'form' && (
          <UserForm
            mode={formMode}
            initialData={formData}
            branches={branches}
            submitting={formSubmitting}
            serverError={formError}
            fieldErrors={fieldErrors}
            onSubmit={handleSubmitForm}
            onCancel={() => setActiveTab('list')}
          />
        )}
      </div>

      <DeleteUserModal
        open={deleteModalOpen}
        user={selectedUserForDelete}
        submitting={deleteSubmitting}
        onCancel={closeDelete}
        onConfirm={handleDelete}
        error={deleteError}
      />
    </div>
  );
}
