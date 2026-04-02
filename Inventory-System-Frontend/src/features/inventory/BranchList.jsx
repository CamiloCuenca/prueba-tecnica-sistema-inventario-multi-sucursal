import { useEffect, useState } from "react";
import { useInventory } from "./useInventory";
import BranchCard from "./branchCard";
import { decodeJWT } from "../../utils/jwt";

export default function BranchList({ onBranchSelect, selectedBranchId }) {
  const { handleBranches, loading, error } = useInventory();
  const [branches, setBranches] = useState([]);
  const [userBranchId, setUserBranchId] = useState(null);



  useEffect(() => {
    handleBranches().then((res) => {
      if (Array.isArray(res)) {
        setBranches(res);
      }
    });
    // eslint-disable-next-line
  }, []);

  useEffect(() => {
    const token = sessionStorage.getItem("token");
    if (token) {
      const payload = decodeJWT(token);
      setUserBranchId(payload?.branchId || null);
    }
  }, []);

  if (loading) {
    return <div className="w-full text-center py-8 text-gray-500">Cargando sucursales...</div>;
  }
  if (error) {
    return <div className="w-full text-center py-8 text-red-500">{error}</div>;
  }

  // El branch seleccionado es el que el usuario clickea, si no hay, se resalta la del token
  const currentBranchId = selectedBranchId ?? userBranchId;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
      {branches.map((branch) => (
        <BranchCard
          key={branch.id}
          data={branch}
          isCurrent={currentBranchId === branch.id}
          onSelect={onBranchSelect}
        />
      ))}
    </div>
  );
}
