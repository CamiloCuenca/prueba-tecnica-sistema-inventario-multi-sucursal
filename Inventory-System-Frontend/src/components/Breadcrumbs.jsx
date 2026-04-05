import { Link } from 'react-router-dom';
import ArrowBackIosNewRoundedIcon from '@mui/icons-material/ArrowBackIosNewRounded';

export default function Breadcrumbs({ items = [], backTo, backLabel = 'Volver' }) {
  const safeItems = Array.isArray(items) ? items.filter(Boolean) : [];

  return (
    <div className="mb-6 flex flex-col gap-3">
      {backTo && (
        <Link
          to={backTo}
          className="inline-flex w-fit items-center gap-2 rounded-full border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 shadow-sm transition-colors hover:bg-gray-100"
        >
          <ArrowBackIosNewRoundedIcon sx={{ fontSize: 16 }} />
          <span>{backLabel}</span>
        </Link>
      )}

      {safeItems.length > 0 && (
        <nav aria-label="Breadcrumb" className="text-sm text-gray-500">
          <ol className="flex flex-wrap items-center gap-2">
            {safeItems.map((item, index) => {
              const isLast = index === safeItems.length - 1;

              return (
                <li key={`${item.label}-${index}`} className="flex items-center gap-2">
                  {item.to && !isLast ? (
                    <Link to={item.to} className="font-medium text-gray-600 hover:text-primary">
                      {item.label}
                    </Link>
                  ) : (
                    <span className={isLast ? 'font-semibold text-gray-900' : 'font-medium text-gray-600'}>
                      {item.label}
                    </span>
                  )}

                  {!isLast && <span className="text-gray-400">/</span>}
                </li>
              );
            })}
          </ol>
        </nav>
      )}
    </div>
  );
}