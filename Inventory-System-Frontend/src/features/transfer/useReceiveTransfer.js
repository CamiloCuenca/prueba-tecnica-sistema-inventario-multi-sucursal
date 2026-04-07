import { useState } from 'react';
import { receiveTransfer } from './transferApi';

export function useReceiveTransfer() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleReceive = async ({ transferId, items }) => {
    setLoading(true);
    setError(null);
    setSuccess(false);
    try {
      await receiveTransfer({ transferId, items });
      setSuccess(true);
    } catch (err) {
      setError(err?.message || 'Error al recibir transferencia');
    } finally {
      setLoading(false);
    }
  };

  return { handleReceive, loading, error, success };
}
