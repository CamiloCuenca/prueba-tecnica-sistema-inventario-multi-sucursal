import { useEffect, useMemo, useState } from 'react';

const roleOptions = ['ADMIN', 'MANAGER', 'OPERATOR'];

const initialState = {
  id: '',
  name: '',
  email: '',
  password: '',
  role: 'OPERATOR',
  branchId: '',
};

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function UserForm({
  mode,
  initialData,
  branches,
  submitting,
  serverError,
  fieldErrors,
  onSubmit,
  onCancel,
}) {
  const [form, setForm] = useState(initialState);
  const [localErrors, setLocalErrors] = useState({});

  useEffect(() => {
    if (!initialData) {
      setForm(initialState);
      return;
    }

    setForm({
      id: initialData.id || '',
      name: initialData.name || '',
      email: initialData.email || '',
      password: '',
      role: initialData.role || 'OPERATOR',
      branchId: initialData.branchId || '',
    });
  }, [initialData]);

  const branchIds = useMemo(() => branches.map((branch) => branch.id), [branches]);

  const validate = () => {
    const nextErrors = {};

    if (!form.name.trim()) nextErrors.name = 'Campo requerido';
    if (!form.email.trim()) {
      nextErrors.email = 'Campo requerido';
    } else if (!emailRegex.test(form.email.trim())) {
      nextErrors.email = 'Formato de email invalido';
    }

    if (mode === 'create') {
      if (!form.password) nextErrors.password = 'Campo requerido';
      else if (form.password.length < 6) nextErrors.password = 'La contrasena debe tener al menos 6 caracteres';
    }

    if (!roleOptions.includes(form.role)) {
      nextErrors.role = 'Rol invalido';
    }

    if (form.branchId && !branchIds.includes(form.branchId)) {
      nextErrors.branchId = 'Sucursal no valida';
    }

    setLocalErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!validate()) return;

    const payload = {
      id: form.id,
      name: form.name.trim(),
      email: form.email.trim(),
      role: form.role,
      branchId: form.branchId || null,
      ...(mode === 'create' ? { password: form.password } : {}),
    };

    onSubmit(payload);
  };

  const mergedErrors = { ...fieldErrors, ...localErrors };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-1">Nombre</label>
        <input
          type="text"
          value={form.name}
          onChange={(event) => handleChange('name', event.target.value)}
          className="w-full border rounded px-3 py-2"
        />
        {mergedErrors.name && <p className="text-xs text-red-600 mt-1">{mergedErrors.name}</p>}
      </div>

      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-1">Email</label>
        <input
          type="email"
          value={form.email}
          onChange={(event) => handleChange('email', event.target.value)}
          className="w-full border rounded px-3 py-2"
        />
        {mergedErrors.email && <p className="text-xs text-red-600 mt-1">{mergedErrors.email}</p>}
      </div>

      {mode === 'create' && (
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Password</label>
          <input
            type="password"
            value={form.password}
            onChange={(event) => handleChange('password', event.target.value)}
            className="w-full border rounded px-3 py-2"
          />
          {mergedErrors.password && <p className="text-xs text-red-600 mt-1">{mergedErrors.password}</p>}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Rol</label>
          <select
            value={form.role}
            onChange={(event) => handleChange('role', event.target.value)}
            className="w-full border rounded px-3 py-2"
          >
            {roleOptions.map((role) => (
              <option key={role} value={role}>
                {role}
              </option>
            ))}
          </select>
          {mergedErrors.role && <p className="text-xs text-red-600 mt-1">{mergedErrors.role}</p>}
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Sucursal</label>
          <select
            value={form.branchId}
            onChange={(event) => handleChange('branchId', event.target.value)}
            className="w-full border rounded px-3 py-2"
          >
            <option value="">Sin sucursal</option>
            {branches.map((branch) => (
              <option key={branch.id} value={branch.id}>
                {branch.name || branch.id}
              </option>
            ))}
          </select>
          {mergedErrors.branchId && <p className="text-xs text-red-600 mt-1">{mergedErrors.branchId}</p>}
        </div>
      </div>

      {serverError && <div className="text-sm text-red-600">{serverError}</div>}

      <div className="flex justify-end gap-2 pt-2">
        <button
          type="button"
          onClick={onCancel}
          className="rounded border border-gray-300 px-4 py-2 text-sm"
          disabled={submitting}
        >
          Cancelar
        </button>
        <button
          type="submit"
          className="rounded bg-primary text-white px-4 py-2 text-sm"
          disabled={submitting}
        >
          {submitting ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
