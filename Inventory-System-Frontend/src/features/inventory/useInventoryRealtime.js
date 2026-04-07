// src/hooks/useInventoryRealtime.js
import { useEffect, useRef } from 'react';
import { createStompClient, disconnectStomp, subscribeTopic } from '../../api/inventorySocket';
import { getInventory, getBranchInventory } from './InventoryApi'; // tus funciones existentes

export function useInventoryRealtime({ branchId, token, refreshPage }) {
  // refreshPage: función del componente que recarga la página actual del inventario (por ejemplo llama handleBranchInventory)
  const subsRef = useRef([]);

  useEffect(() => {
    if (!token) return;

    const client = createStompClient({
      token,
      onConnect: () => console.log('STOMP conectado'),
      onError: (err) => console.error('STOMP error', err),
      debug: (m) => console.debug('[STOMP]', m),
    });

    // Suscribirse a productos (para cambios globales en product)
    try {
      const s1 = subscribeTopic('/topic/products', async (msg) => {
        // msg: { productId, event, timestamp }
        console.debug('product event', msg);
        // Estrategia simple: refrescar la vista si es relevante
        // Si estás viendo inventario de una sucursal, refrescar esa sucursal
        if (branchId) {
          await refreshPage(); // invoca el refresco (p. ej. handleBranchInventory)
        } else {
          // si estás en catálogo global: refrescar catálogo
          const data = await getInventory({ page: 0, size: 20 });
          // actualizar UI con response
        }
      });
      subsRef.current.push(s1);
    } catch (e) { /* ignore */ }

    // Suscribirse a inventario por branch
    if (branchId) {
      try {
        const s2 = subscribeTopic(`/topic/inventory-updates.${branchId}`, async (msg) => {
          // msg: { branchId, productId, currentStock, timestamp }
          console.debug('inventory update', msg);
          // Si el mensaje contiene currentStock, actualizamos solo la fila específica en la UI:
          if (msg.productId && typeof msg.currentStock === 'number') {
            // implementa función en tu componente para actualizar una fila (ej. updateInventoryRowStock)
            // si no la tienes, simplemente refresca la página actual:
            await refreshPage();
          } else {
            // fallback: refrescar la página
            await refreshPage();
          }
        });
        subsRef.current.push(s2);
      } catch (e) {}
    }

    // Suscríbete a global import/topic
    try {
      const s3 = subscribeTopic('/topic/inventory-updates.global', async (msg) => {
        console.debug('global inventory event', msg);
        await refreshPage();
      });
      subsRef.current.push(s3);
    } catch (e) {}

    return () => {
      // Unsubscribe y disconnect si quieres
      subsRef.current.forEach(sub => { try { sub.unsubscribe(); } catch (e) {} });
      subsRef.current = [];
      // No siempre desconectar globalmente (si otros componentes usan ws), pero si quieres:
      disconnectStomp();
    };
  }, [token, branchId, refreshPage]);
}
