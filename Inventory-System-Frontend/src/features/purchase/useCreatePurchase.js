import { useState } from 'react';
import { createPurchase } from './purchaseApi';

export const useCreatePurchase = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const submit = async (purchaseData) => {
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const result = await createPurchase(purchaseData);
      setSuccess(true);
      return result;
    } catch (err) {
      setError(err.message || 'Error al crear la compra');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setError(null);
    setSuccess(false);
    setLoading(false);
  };

  return {
    submit,
    loading,
    error,
    success,
    reset,
  };
};
