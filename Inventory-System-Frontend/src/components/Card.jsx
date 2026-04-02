import React from "react";

/**
 * Card reutilizable para usar como contenedor visual.
 * No contiene lógica de negocio ni estilos hardcodeados.
 * Usa clases de TailwindCSS y permite personalización vía props.
 *
 * @param {object} props
 * @param {React.ReactNode} props.children - Contenido del card
 * @param {string} [props.className] - Clases adicionales para personalización
 * @param {object} [props.rest] - Otros props (ej: onClick, style)
 */


const Card = ({ children, className = "", ...rest }) => {
	return (
		<div
			className={`bg-white  rounded-lg shadow p-6 transition-colors duration-200 ${className}`}
			{...rest}
		>
			{children}
		</div>
	);
};

export default Card;
