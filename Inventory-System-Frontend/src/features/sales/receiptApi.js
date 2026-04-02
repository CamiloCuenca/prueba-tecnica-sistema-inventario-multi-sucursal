import { api } from "../../api/client";

export const getSaleReceiptPdf = async (saleId) => {
  if (!saleId) throw new Error("saleId es requerido");
  const response = await api.get(`/api/sales/${saleId}/receipt/view`, {
    responseType: 'blob',
  });
  return response.data; // Blob PDF
};
