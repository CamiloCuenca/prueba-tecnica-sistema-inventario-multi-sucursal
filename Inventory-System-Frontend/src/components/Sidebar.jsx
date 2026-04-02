
import React from "react";
import DashboardIcon from "@mui/icons-material/Dashboard";
import Inventory2Icon from "@mui/icons-material/Inventory2";
import PeopleIcon from "@mui/icons-material/People";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import { Link, useLocation } from "react-router-dom";
import { decodeJWT } from "../utils/jwt";
import { LocalGroceryStore, PointOfSale } from "@mui/icons-material";

function getRole() {
  const token = sessionStorage.getItem("token");
  const payload = decodeJWT(token);
  return payload?.role || null;
}

const baseMenu = [
  {
    label: "Dashboard",
    icon: <DashboardIcon fontSize="medium" />,
    to: "/dashboard",
    roles: ["ADMIN", "MANAGER", "OPERATOR"],
  },
  {
    label: "Inventario",
    icon: <Inventory2Icon fontSize="medium" />,
    to: "/inventario",
    roles: ["ADMIN", "MANAGER", "OPERATOR"],
  },
  {
    label: "Ventas",
    icon: <PointOfSale fontSize="medium" />,
    to: "/ventas",
    roles: ["ADMIN", "MANAGER", "OPERATOR"],
  },
  {
    label: "Compras",
    icon: <LocalGroceryStore fontSize="medium" />,
    to: "/compras",
    roles: ["ADMIN", "MANAGER", "OPERATOR"],
  },
  {
    label: "Transacciones",
    icon: <SwapHorizIcon fontSize="medium" />,
    to: "/transacciones",
    roles: ["ADMIN", "MANAGER"],
  },
  {
    label: "Usuarios",
    icon: <PeopleIcon fontSize="medium" />,
    to: "/usuarios",
    roles: ["ADMIN"],
  },
];

export default function Sidebar() {
  const location = useLocation();
  const role = getRole();
  const menuItems = baseMenu.filter((item) => item.roles.includes(role));
  return (
    <aside className="h-screen w-56 bg-white dark:bg-neutral-900 border-r border-gray-200 dark:border-neutral-800 flex flex-col py-6 px-4">
      <nav className="flex flex-col gap-2">
        {menuItems.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className={`flex items-center gap-3 px-3 py-2 rounded-md font-medium transition-colors duration-150
              ${location.pathname === item.to
                ? "bg-primary text-white"
                : "text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-neutral-800"}
            `}
          >
            {item.icon}
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>
    </aside>
  );
}
