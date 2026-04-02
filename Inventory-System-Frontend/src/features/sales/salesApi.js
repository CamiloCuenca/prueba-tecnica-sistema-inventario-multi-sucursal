import { api } from "../../api/client";

// items: [{ productId, quantity, price, discount }], discountTotal: number
export const postSale = async ({ branchId, items, discountTotal = 0 }) => {
  if (!branchId || !Array.isArray(items) || items.length === 0) {
    throw new Error("branchId e items son requeridos");
  }
  const response = await api.post(`/api/sales`, {
    branchId,
    items,
    discountTotal,
  });
  return response.data;
};


/**
 * { "branchId": "9d6fe2bb-8ac7-49a5-907e-015961c850d5", 
 "items": [ { "productId": "607978de-94c9-458c-bc56-28b3f778769b", 
						 				"quantity": 2, 									
						 				"price": 14.50,
										"discount": 1.00 } ], 
 
 "discountTotal": 1.00 }
 */

