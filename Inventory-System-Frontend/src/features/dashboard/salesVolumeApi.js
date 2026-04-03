import { api } from "../../api/client";

function buildQueryString(params) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, String(value));
    }
  });

  return query.toString();
}

async function getSalesVolumeMetricsPage(params) {
  const queryString = buildQueryString(params);
  const response = await api.get(`/api/metrics/sales-volume${queryString ? `?${queryString}` : ""}`);
  return response.data;
}

export const getSalesVolumeMetrics = async ({
  branchId,
  productId,
  from,
  to,
  size = 100,
} = {}) => {
  const firstPage = await getSalesVolumeMetricsPage({ branchId, productId, from, to, page: 0, size });
  const content = firstPage?.content ?? [];
  const totalPages = firstPage?.totalPages ?? 1;

  if (totalPages <= 1) {
    return content;
  }

  const requests = [];
  for (let page = 1; page < totalPages; page += 1) {
    requests.push(getSalesVolumeMetricsPage({ branchId, productId, from, to, page, size }));
  }

  const remainingPages = await Promise.all(requests);
  return [
    ...content,
    ...remainingPages.flatMap((page) => page?.content ?? []),
  ];
};
