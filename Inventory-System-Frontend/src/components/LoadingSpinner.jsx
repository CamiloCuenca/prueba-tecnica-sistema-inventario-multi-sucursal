export default function LoadingSpinner({ label = "Cargando...", className = "" }) {
  return (
    <div className={`flex items-center justify-center gap-3 py-8 text-sm text-gray-600 ${className}`}>
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-primary" />
      <span>{label}</span>
    </div>
  );
}