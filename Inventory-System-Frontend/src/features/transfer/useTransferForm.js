import { useState, useEffect } from 'react';
import { requestTransfer, getBranchInventoryForTransfer, getAllBranches } from './transferApi';
import { getBranchIdFromToken } from '../../utils/tokenUtils';

/**
 * Hook para manejar la lógica del formulario de solicitud de transferencia
 * Gestiona estado de sucursales, inventario, validaciones y envío de solicitud
 */
export const useTransferForm = () => {
  const [originBranchId, setOriginBranchId] = useState('');
  const [destinationBranchId, setDestinationBranchId] = useState('');
  const [branches, setBranches] = useState([]);
  const [originInventory, setOriginInventory] = useState([]);
  const [selectedItems, setSelectedItems] = useState({});
  const [loadingBranches, setLoadingBranches] = useState(false);
  const [loadingInventory, setLoadingInventory] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const currentBranchId = getBranchIdFromToken();

  // Inicializar sucursal origen con la del usuario
  useEffect(() => {
    if (currentBranchId && !originBranchId) {
      setOriginBranchId(currentBranchId);
    }
  }, [currentBranchId, originBranchId]);

  // Cargar sucursales disponibles
  useEffect(() => {
    const loadBranches = async () => {
      setLoadingBranches(true);
      setError(null);
      try {
        const data = await getAllBranches();
        setBranches(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(getErrorMessage(err, 'Error al cargar sucursales'));
        setBranches([]);
      } finally {
        setLoadingBranches(false);
      }
    };

    loadBranches();
  }, []);

  // Cargar inventario cuando cambia la sucursal origen
  useEffect(() => {
    if (!originBranchId) {
      setOriginInventory([]);
      setSelectedItems({});
      return;
    }

    const loadInventory = async () => {
      setLoadingInventory(true);
      setError(null);
      try {
        const data = await getBranchInventoryForTransfer(originBranchId, { page: 0, size: 100 });
        setOriginInventory(data?.content || []);
        setSelectedItems({});
      } catch (err) {
        setError(getErrorMessage(err, 'Error al cargar inventario de la sucursal origen'));
        setOriginInventory([]);
      } finally {
        setLoadingInventory(false);
      }
    };

    loadInventory();
  }, [originBranchId]);

  const getErrorMessage = (err, fallbackMessage) => {
    if (typeof err === 'string') return err;
    if (err?.message) return err.message;
    if (err?.error) return err.error;
    return fallbackMessage;
  };

  const updateItemQuantity = (productId, quantity) => {
    const parsedQty = Number(quantity) || 0;
    if (parsedQty > 0) {
      setSelectedItems((prev) => ({ ...prev, [productId]: parsedQty }));
    } else {
      setSelectedItems((prev) => {
        const updated = { ...prev };
        delete updated[productId];
        return updated;
      });
    }
  };

  const validateForm = () => {
    if (!originBranchId) {
      setError('Debes seleccionar una sucursal origen');
      return false;
    }
    if (!destinationBranchId) {
      setError('Debes seleccionar una sucursal destino');
      return false;
    }
    if (originBranchId === destinationBranchId) {
      setError('La sucursal origen y destino no pueden ser la misma');
      return false;
    }
    if (Object.keys(selectedItems).length === 0) {
      setError('Debes seleccionar al menos un producto');
      return false;
    }
    return true;
  };

  const submitTransferRequest = async () => {
    setError(null);
    setSuccess(null);

    if (!validateForm()) {
      return;
    }

    const items = Object.entries(selectedItems).map(([productId, quantity]) => ({
      productId,
      quantity: Number(quantity),
    }));

    const payload = {
      originBranchId,
      destinationBranchId,
      items,
    };

    setSubmitting(true);
    try {
      const response = await requestTransfer(payload);
      setSuccess('Solicitud de transferencia creada exitosamente');
      
      // Reset del formulario
      setDestinationBranchId('');
      setSelectedItems({});
      setOriginBranchId(currentBranchId || '');

      return response;
    } catch (err) {
      setError(getErrorMessage(err, 'Error al crear la solicitud de transferencia'));
      return null;
    } finally {
      setSubmitting(false);
    }
  };

  return {
    originBranchId,
    setOriginBranchId,
    destinationBranchId,
    setDestinationBranchId,
    branches,
    originInventory,
    selectedItems,
    updateItemQuantity,
    loadingBranches,
    loadingInventory,
    submitting,
    error,
    success,
    submitTransferRequest,
  };
};
