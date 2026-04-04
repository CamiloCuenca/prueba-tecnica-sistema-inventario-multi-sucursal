import { useMemo, useState } from 'react';
import TransferRequestForm from './TransferRequestForm';
import TransferActiveTables from './TransferActiveTables';

export default function TransferTabs() {
  const [activeTab, setActiveTab] = useState('request');

  const tabs = useMemo(
    () => [
      { id: 'request', label: 'Solicitar Transferencia' },
      { id: 'active', label: 'Transferencias Activas' },
    ],
    []
  );

  return (
    <div>
      <div className="mb-6 border-b border-gray-200">
        <div className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-t-md text-sm font-semibold transition-colors ${
                activeTab === tab.id
                  ? 'bg-primary text-white'
                  : 'bg-surface text-white hover:bg-gray-200'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-white rounded-b-md">
        {activeTab === 'request' && (
          <div className="p-6">
            <TransferRequestForm />
          </div>
        )}

        {activeTab === 'active' && (
          <div className="p-6">
            <TransferActiveTables />
          </div>
        )}
      </div>
    </div>
  );
}
