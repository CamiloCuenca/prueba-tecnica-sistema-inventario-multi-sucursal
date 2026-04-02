import Card from '../../components/Card';

export default function ProductCard({ product }) {
  return (
    <Card>
      <div className="flex flex-col gap-4">

        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h3 className="text-xl font-semibold text-gray-800">
              {product.productName}
            </h3>
            <p className="text-sm text-gray-500">
              SKU: {product.sku}
            </p>
          </div>

          {/* Cantidad como badge */}
          <span className="bg-blue-100 text-blue-700 text-sm font-medium px-3 py-1 rounded-full">
            {product.quantity} unidades
          </span>
        </div>

        {/* Divider */}
        <div className="border-t"></div>

        {/* Info */}
        <div className="grid grid-cols-2 gap-3 text-sm text-gray-600">
          <div>
            <span className="font-medium text-gray-800">Sucursal:</span>
            <p>{product.branchName}</p>
          </div>

          <div>
            <span className="font-medium text-gray-800">ID Producto:</span>
            <p>{product.productId}</p>
          </div>

          <div>
            <span className="font-medium text-gray-800">ID Sucursal:</span>
            <p>{product.branchId}</p>
          </div>

          <div>
            <span className="font-medium text-gray-800">Última actualización:</span>
            <p>
              {new Date(product.updatedAt).toLocaleDateString()}
            </p>
          </div>
        </div>

      </div>
    </Card>
  );
}