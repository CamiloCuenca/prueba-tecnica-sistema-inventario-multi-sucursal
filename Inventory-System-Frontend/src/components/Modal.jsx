import React from "react";
import Card from "./Card";

export default function Modal({ open, onClose, children, contentClassName = "", maxWidth = "max-w-lg" }) {
  if (!open) return null;

  const baseClassName = "bg-white rounded-lg shadow-lg p-6 relative w-full";
  const defaultSizeClassName = `min-w-[320px] ${maxWidth}`;
  const modalContentClassName = contentClassName
    ? `${baseClassName} ${contentClassName}`
    : `${baseClassName} ${defaultSizeClassName}`;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-10">
      <div className={modalContentClassName}>
        <button
          onClick={onClose}
          className="absolute top-2 right-2 text-gray-400 hover:text-primary text-xl font-bold focus:outline-none"
          aria-label="Cerrar"
        >
          &times;
        </button>
        {children}
      </div>
    </div>
  );
}
