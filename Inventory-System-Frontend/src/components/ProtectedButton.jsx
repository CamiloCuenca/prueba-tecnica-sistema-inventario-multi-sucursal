import { useAuth } from '../context/AuthContext';

export default function ProtectedButton({
  requiredRoles = ['ADMIN'],
  hideWhenDenied = true,
  disabled = false,
  children,
  className = '',
  type = 'button',
  onClick,
  ...rest
}) {
  const { role } = useAuth();
  const allowedRoles = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
  const allowed = !requiredRoles || allowedRoles.includes(role);

  if (!allowed && hideWhenDenied) {
    return null;
  }

  return (
    <button
      type={type}
      onClick={allowed ? onClick : undefined}
      disabled={disabled || !allowed}
      className={`${className} ${!allowed ? 'cursor-not-allowed opacity-50' : ''}`.trim()}
      {...rest}
    >
      {children}
    </button>
  );
}