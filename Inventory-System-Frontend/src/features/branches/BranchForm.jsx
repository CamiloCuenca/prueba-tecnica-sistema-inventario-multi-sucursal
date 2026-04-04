import React, { useEffect, useState } from 'react';

const BranchForm = ({ branch = null, onSubmit, loading = false, fieldErrors = {} }) => {
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    latitude: '',
    longitude: '',
  });
  const [localErrors, setLocalErrors] = useState({});

  useEffect(() => {
    if (branch) {
      setFormData({
        name: branch.name || '',
        address: branch.address || '',
        latitude: branch.latitude !== undefined ? branch.latitude : '',
        longitude: branch.longitude !== undefined ? branch.longitude : '',
      });
    } else {
      setFormData({
        name: '',
        address: '',
        latitude: '',
        longitude: '',
      });
    }
    setLocalErrors({});
  }, [branch]);

  const validateForm = () => {
    const errors = {};

    if (!formData.name || formData.name.trim() === '') {
      errors.name = 'El nombre es requerido';
    }

    if (formData.latitude !== '') {
      const lat = parseFloat(formData.latitude);
      if (isNaN(lat) || lat < -90 || lat > 90) {
        errors.latitude = 'Latitud debe estar entre -90 y 90';
      }
    }

    if (formData.longitude !== '') {
      const lon = parseFloat(formData.longitude);
      if (isNaN(lon) || lon < -180 || lon > 180) {
        errors.longitude = 'Longitud debe estar entre -180 y 180';
      }
    }

    setLocalErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    const payload = {
      name: formData.name.trim(),
      address: formData.address.trim() || undefined,
      latitude: formData.latitude !== '' ? parseFloat(formData.latitude) : undefined,
      longitude: formData.longitude !== '' ? parseFloat(formData.longitude) : undefined,
    };

    if (branch) {
      payload.id = branch.id;
    }

    onSubmit(payload);
  };

  const mergedErrors = { ...localErrors, ...fieldErrors };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Nombre */}
      <div>
        <label className="block text-sm font-medium text-gray-700">Nombre *</label>
        <input
          type="text"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="Ej: Sucursal Centro"
          className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {mergedErrors.name && <p className="mt-1 text-sm text-red-600">{mergedErrors.name}</p>}
      </div>

      {/* Dirección */}
      <div>
        <label className="block text-sm font-medium text-gray-700">Dirección</label>
        <input
          type="text"
          name="address"
          value={formData.address}
          onChange={handleChange}
          placeholder="Ej: Calle 123 #45-67"
          className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {mergedErrors.address && <p className="mt-1 text-sm text-red-600">{mergedErrors.address}</p>}
      </div>

      {/* Latitud */}
      <div>
        <label className="block text-sm font-medium text-gray-700">Latitud (-90 a 90)</label>
        <input
          type="number"
          name="latitude"
          value={formData.latitude}
          onChange={handleChange}
          placeholder="Ej: 4.710989"
          step="0.000001"
          className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {mergedErrors.latitude && <p className="mt-1 text-sm text-red-600">{mergedErrors.latitude}</p>}
      </div>

      {/* Longitud */}
      <div>
        <label className="block text-sm font-medium text-gray-700">Longitud (-180 a 180)</label>
        <input
          type="number"
          name="longitude"
          value={formData.longitude}
          onChange={handleChange}
          placeholder="Ej: -74.072092"
          step="0.000001"
          className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {mergedErrors.longitude && <p className="mt-1 text-sm text-red-600">{mergedErrors.longitude}</p>}
      </div>

      {/* Botón Submit */}
      <button
        type="submit"
        disabled={loading}
        className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
      >
        {loading ? 'Guardando...' : (branch ? 'Actualizar' : 'Crear')}
      </button>
    </form>
  );
};

export default BranchForm;
