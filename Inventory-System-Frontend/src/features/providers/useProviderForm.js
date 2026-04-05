import { useEffect, useMemo, useState } from 'react';
import { createProvider, getProviderById, mapProviderApiError, normalizeProvider, updateProvider } from './providersApi';

const initialFormState = {
  name: '',
  contactInfo: '',
};

export function useProviderForm(providerId) {
  const isEdit = Boolean(providerId);
  const [form, setForm] = useState(initialFormState);
  const [provider, setProvider] = useState(null);
  const [loadingProvider, setLoadingProvider] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    if (!providerId) {
      setProvider(null);
      setForm(initialFormState);
      setLoadingProvider(false);
      return;
    }

    const loadProvider = async () => {
      setLoadingProvider(true);
      setError(null);

      try {
        const data = await getProviderById(providerId);
        const normalized = normalizeProvider(data);
        setProvider(normalized);
        setForm({
          name: normalized.name || '',
          contactInfo: normalized.contactInfo || '',
        });
      } catch (err) {
        setError(mapProviderApiError(err, 'No se pudo cargar el proveedor'));
      } finally {
        setLoadingProvider(false);
      }
    };

    loadProvider();
  }, [providerId]);

  const validation = useMemo(() => {
    return (values) => {
      const nextErrors = {};

      if (!values.name.trim()) {
        nextErrors.name = 'Campo requerido';
      }

      return nextErrors;
    };
  }, []);

  const setField = (field, value) => {
    setForm((previous) => ({ ...previous, [field]: value }));
  };

  const submit = async () => {
    const nextErrors = validation(form);
    setFieldErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      return { ok: false, error: { message: 'Hay errores de validacion.', fieldErrors: nextErrors } };
    }

    setSubmitting(true);
    setError(null);
    setFieldErrors({});

    const payload = {
      name: form.name.trim(),
      contactInfo: form.contactInfo.trim() || null,
    };

    try {
      const data = isEdit ? await updateProvider(providerId, payload) : await createProvider(payload);
      const normalized = normalizeProvider(data);
      setProvider(normalized);
      return { ok: true, provider: normalized };
    } catch (err) {
      const mapped = mapProviderApiError(err, isEdit ? 'No se pudo actualizar el proveedor' : 'No se pudo crear el proveedor');
      setError(mapped);
      setFieldErrors(mapped.fieldErrors || {});
      return { ok: false, error: mapped };
    } finally {
      setSubmitting(false);
    }
  };

  return {
    mode: isEdit ? 'edit' : 'create',
    form,
    setField,
    loadingProvider,
    submitting,
    error,
    fieldErrors,
    provider,
    submit,
  };
}