import { useState, useMemo } from "react";
import PurchaseForm from "./PurchaseForm";
import PurchaseTable from "./purchaseManager/PurchaseTable";

export default function PurchaseTabs() {
  const [activeTab, setActiveTab] = useState("create");

  const tabs = useMemo(() => [
    { id: "create", label: "Crear Compra" },
    { id: "list", label: "Compras" },
  ], []);

  return (
    <div>
      {/* Tabs Navigation */}
      <div className="mb-6 border-b border-gray-200">
        <div className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-t-md text-sm font-semibold transition-colors ${
                activeTab === tab.id
                  ? "bg-primary text-white"
                  : "bg-surface text-white hover:bg-gray-200"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content */}
      <div className="bg-white rounded-b-md">
        {activeTab === "create" && (
          <div className="p-6">
            <PurchaseForm />
          </div>
        )}

        {activeTab === "list" && (
          <div className="p-6">
            <PurchaseTable />
          </div>
        )}
      </div>
    </div>
  );
}
