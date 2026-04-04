import React, { useState } from 'react';
import { createBranch, deleteBranch, getBranchById, updateBranch } from './branchesApi';
import { useBranches } from './useBranches';
import BranchForm from './BranchForm';
import BranchesListView from './BranchesListView';
import DeleteBranchModal from './DeleteBranchModal';
import Modal from '../../components/Modal';
import { decodeJWT } from '../../utils/jwt';

const BranchesTabs = () => {
  // Auth
  const token = sessionStorage.getItem('token') || sessionStorage.getItem('authToken');
  const role = decodeJWT(token)?.role;
  const isAdmin = role === 'ADMIN';

  // Tabs
  const [activeTab, setActiveTab] = useState('list');
  const [formMode, setFormMode] = useState('create');

  // Search & Pagination
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const { branches, pageInfo, loading, error, refetch } = useBranches({ page, size: 10, search });

  // Form State
  const [formData, setFormData] = useState(null);
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  // Delete Modal
  const [selectedBranchForDelete, setSelectedBranchForDelete] = useState(null);
  const [deleteSubmitting, setDeleteSubmitting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  // Feedback (Toast)
  const [feedback, setFeedback] = useState({ type: null, message: '' });

  // Helpers
  const showFeedback = (type, message) => {
    setFeedback({ type, message });
    setTimeout(() => setFeedback({ type: null, message: '' }), 3000);
  };

  const mapApiError = (err) => {
    const status = err?.status || err?.response?.status;
    if (status === 401) return 'Tu sesión ha expirado. Por favor inicia sesión.';
    if (status === 403) return 'No tienes permisos para esta acción.';
    if (status === 404) return 'Sucursal no encontrada.';
    if (status === 409) return 'El nombre ya existe.';
    if (status === 400) return 'Error de validación.';
    if (status === 500) return 'Error del servidor. Intenta más tarde.';
    if (err?.message) return err.message;
    return 'Error desconocido.';
  };

  // Form Workflows
  const openCreate = () => {
    setFormMode('create');
    setFormData(null);
    setFormError(null);
    setFieldErrors({});
    setActiveTab('form');
  };

  const openEdit = async (branch) => {
    try {
      setFormSubmitting(true);
      setFormError(null);
      const fullBranch = await getBranchById(branch.id);
      setFormMode('edit');
      setFormData(fullBranch);
      setFieldErrors({});
      setActiveTab('form');
    } catch (err) {
      setFormError(mapApiError(err));
      showFeedback('error', 'No pudimos cargar los datos de la sucursal.');
    } finally {
      setFormSubmitting(false);
    }
  };

  const handleSubmitForm = async (payload) => {
    try {
      setFormSubmitting(true);
      setFormError(null);
      setFieldErrors({});

      if (formMode === 'create') {
        await createBranch(payload);
        showFeedback('success', 'Sucursal creada correctamente.');
      } else {
        await updateBranch(payload.id, payload);
        showFeedback('success', 'Sucursal actualizada correctamente.');
      }

      setPage(0);
      refetch();
      setActiveTab('list');
    } catch (err) {
      const errorMsg = mapApiError(err);
      setFormError(errorMsg);
      showFeedback('error', errorMsg);

      // Map field errors from backend if provided
      if (err?.fieldErrors && typeof err.fieldErrors === 'object') {
        setFieldErrors(err.fieldErrors);
      }
    } finally {
      setFormSubmitting(false);
    }
  };

  const openDelete = (branch) => {
    setSelectedBranchForDelete(branch);
    setDeleteError(null);
  };

  const handleDelete = async () => {
    if (!selectedBranchForDelete) return;

    try {
      setDeleteSubmitting(true);
      setDeleteError(null);
      await deleteBranch(selectedBranchForDelete.id);
      showFeedback('success', 'Sucursal eliminada correctamente.');
      refetch();
      setSelectedBranchForDelete(null);
    } catch (err) {
      const errorMsg = mapApiError(err);
      setDeleteError(errorMsg);
      showFeedback('error', errorMsg);
    } finally {
      setDeleteSubmitting(false);
    }
  };

  const handleSearchChange = (value) => {
    setSearch(value);
    setPage(0);
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  return (
    <div className="space-y-4">
      {/* Feedback Toast */}
      {feedback.message && (
        <div
          className={`p-3 rounded-md ${
            feedback.type === 'success'
              ? 'bg-green-100 text-green-800 border border-green-300'
              : 'bg-red-100 text-red-800 border border-red-300'
          }`}
        >
          {feedback.message}
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-4 border-b">
        <button
          onClick={() => setActiveTab('list')}
          className={`px-4 py-2 font-medium transition ${
            activeTab === 'list'
              ? 'border-b-2 border-blue-600 text-blue-600'
              : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          Listado
        </button>
        {activeTab === 'form' && (
          <button
            onClick={() => setActiveTab('form')}
            className={`px-4 py-2 font-medium transition border-b-2 border-blue-600 text-blue-600`}
          >
            {formMode === 'create' ? 'Crear sucursal' : 'Editar sucursal'}
          </button>
        )}
      </div>

      {/* Content */}
      <div className="mt-6">
        {activeTab === 'list' && (
          <div>
            {error && (
              <div className="mb-4 p-3 bg-red-100 text-red-800 border border-red-300 rounded-md">
                {error}
              </div>
            )}
            <BranchesListView
              branches={branches}
              pageInfo={pageInfo}
              loading={loading}
              search={search}
              onSearchChange={handleSearchChange}
              onCreate={openCreate}
              onEdit={openEdit}
              onDelete={openDelete}
              onPageChange={handlePageChange}
              isAdmin={isAdmin}
            />
          </div>
        )}

        {activeTab === 'form' && (
          <Modal
            open={activeTab === 'form'}
            onClose={() => setActiveTab('list')}
          >
            <div className="space-y-4">
              <h2 className="text-lg font-semibold text-gray-900">
                {formMode === 'create' ? 'Crear sucursal' : 'Editar sucursal'}
              </h2>
              {formError && (
                <div className="p-3 bg-red-100 text-red-800 border border-red-300 rounded-md text-sm">
                  {formError}
                </div>
              )}
              <BranchForm
                branch={formData}
                onSubmit={handleSubmitForm}
                loading={formSubmitting}
                fieldErrors={fieldErrors}
              />
            </div>
          </Modal>
        )}
      </div>

      {/* Delete Modal */}
      <DeleteBranchModal
        open={Boolean(selectedBranchForDelete)}
        branch={selectedBranchForDelete}
        loading={deleteSubmitting}
        onConfirm={handleDelete}
        onCancel={() => setSelectedBranchForDelete(null)}
      />
    </div>
  );
};

export default BranchesTabs;
