
import React from "react";
import DashboardIcon from "@mui/icons-material/Dashboard";
import Inventory2Icon from "@mui/icons-material/Inventory2";
import PeopleIcon from "@mui/icons-material/People";
import SwapHorizIcon from "@mui/icons-material/SwapHoriz";
import { Link, useLocation } from "react-router-dom";
import { LocalGroceryStore, PointOfSale, Business, Storefront } from "@mui/icons-material";
import { useAuth } from "../context/AuthContext";

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
    label: "transacciones",
    icon: <SwapHorizIcon fontSize="medium" />,
    to: "/transacciones",
    roles: ["ADMIN", "MANAGER", "OPERATOR"],
  },
  {
    label: "Proveedores",
    icon: <Storefront fontSize="medium" />,
    to: "/providers",
    roles: ["ADMIN"],
  },
  {
    label: "Usuarios",
    icon: <PeopleIcon fontSize="medium" />,
    to: "/usuarios",
    roles: ["ADMIN"],
  },
  {
    label: "Sucursales",
    icon: <Business fontSize="medium" />,
    to: "/sucursales",
    roles: ["ADMIN", "MANAGER"],
  },
];

export default function Sidebar({ collapsed = false }) {
  const location = useLocation();
  const { role } = useAuth();
  const menuItems = baseMenu.filter((item) => item.roles.includes(role));

  return (
    <aside
      className={`min-h-screen border-r border-gray-200 bg-white py-6 transition-all duration-300 dark:border-neutral-800 dark:bg-neutral-900 ${
        collapsed ? "w-20 px-2" : "w-56 px-4"
      }`}
    >
      <nav className="flex flex-col gap-2">
        {menuItems.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            title={collapsed ? item.label : undefined}
            className={`flex items-center rounded-md px-3 py-2 font-medium transition-colors duration-150 ${
              collapsed ? "justify-center" : "gap-3"
            }
              ${location.pathname === item.to
                ? "bg-primary text-white"
                : "text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-neutral-800"}
            `}
          >
            {item.icon}
            {!collapsed && <span>{item.label}</span>}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
