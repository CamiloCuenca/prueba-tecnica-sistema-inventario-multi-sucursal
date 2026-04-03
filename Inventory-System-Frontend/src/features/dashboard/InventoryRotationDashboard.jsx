import { useEffect, useState } from "react";
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, ScatterChart, Scatter, CartesianGrid, Legend,
} from "recharts";
import Card from "../../components/Card";

function numberFormat(n) {
  return n?.toLocaleString("en-US", { maximumFractionDigits: 2 }) ?? "-";
}

export default function InventoryRotationDashboard({ data, loading }) {
  if (loading) {
    return <div className="w-full text-center py-8 text-gray-500">Cargando métricas...</div>;
  }
  if (!data) {
    return <div className="w-full text-center py-8 text-gray-500">No hay datos para mostrar.</div>;
  }

  const { rotations = [], topHighDemand = [], topLowDemand = [] } = data;
  const totalProducts = rotations.length;
  const zeroSales = rotations.filter(p => p.totalSold === 0);
  const topProduct = topHighDemand[0];
  const avgRotation = rotations.length
    ? rotations.reduce((acc, p) => acc + (p.rotationIndex || 0), 0) / rotations.length
    : 0;
  const percentNoSales = totalProducts ? Math.round((zeroSales.length / totalProducts) * 100) : 0;

  return (
    <div className="space-y-8">
      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="bg-white border shadow p-4 flex flex-col items-center">
          <div className="text-2xl font-bold text-primary">{numberFormat(totalProducts)}</div>
          <div className="text-gray-600">Total productos</div>
        </Card>
        <Card className="bg-white border shadow p-4 flex flex-col items-center">
          <div className="text-2xl font-bold text-red-500">{numberFormat(zeroSales.length)}</div>
          <div className="text-gray-600">Sin ventas</div>
        </Card>
        <Card className="bg-white border shadow p-4 flex flex-col items-center">
          <div className="text-lg font-bold text-green-600">{topProduct ? topProduct.productName : "-"}</div>
          <div className="text-gray-600">Top ventas</div>
        </Card>
        <Card className="bg-white border shadow p-4 flex flex-col items-center">
          <div className="text-2xl font-bold text-blue-600">{numberFormat(avgRotation)}</div>
          <div className="text-gray-600">Rotación promedio</div>
        </Card>
      </div>

      {/* Bar Chart - Top High Demand */}
      <div>
        <h2 className="text-lg font-bold mb-2">Top productos alta demanda</h2>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={topHighDemand.slice(0, 5)} margin={{ top: 16, right: 16, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="productName" tick={{ fontSize: 12 }} />
            <YAxis />
            <Tooltip formatter={numberFormat} />
            <Bar dataKey="totalSold" fill="#22c55e" name="Ventas" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Bar Chart - Top Low Demand */}
      <div>
        <h2 className="text-lg font-bold mb-2">Productos baja demanda</h2>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={topLowDemand.slice(0, 5)} margin={{ top: 16, right: 16, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="productName" tick={{ fontSize: 12 }} />
            <YAxis />
            <Tooltip formatter={numberFormat} />
            <Bar dataKey="totalSold" fill="#f59e42" name="Ventas bajas" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Scatter/Bar Chart - Product Rotation Analysis */}
      <div>
        <h2 className="text-lg font-bold mb-2">Análisis de rotación de productos</h2>
        <ResponsiveContainer width="100%" height={300}>
          <ScatterChart>
            <CartesianGrid />
            <XAxis dataKey="productName" type="category" tick={{ fontSize: 12 }} />
            <YAxis dataKey="rotationIndex" name="Índice de rotación" />
            <Tooltip formatter={numberFormat} />
            <Scatter data={rotations} fill="#3b82f6" name="Rotación" />
          </ScatterChart>
        </ResponsiveContainer>
      </div>

      {/* Table - Products Without Sales */}
      <div>
        <h2 className="text-lg font-bold mb-2">Productos sin ventas</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white border rounded-lg">
            <thead>
              <tr>
                <th className="px-4 py-2 border-b">Producto</th>
                <th className="px-4 py-2 border-b">SKU</th>
                <th className="px-4 py-2 border-b">Stock actual</th>
              </tr>
            </thead>
            <tbody>
              {zeroSales.map((p) => (
                <tr key={p.productId} className="bg-red-50 text-red-700">
                  <td className="px-4 py-2 border-b font-semibold">{p.productName}</td>
                  <td className="px-4 py-2 border-b">{p.sku}</td>
                  <td className="px-4 py-2 border-b">{numberFormat(p.currentStock)}</td>
                </tr>
              ))}
              {zeroSales.length === 0 && (
                <tr><td colSpan={3} className="text-center py-4 text-gray-500">Todos los productos tienen ventas</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Insights Section */}
      <div className="mt-6 p-4 bg-blue-50 border-l-4 border-blue-400 rounded">
        <span className="font-semibold">{percentNoSales}%</span> de los productos no tuvieron ventas en el periodo seleccionado.
      </div>
    </div>
  );
}
