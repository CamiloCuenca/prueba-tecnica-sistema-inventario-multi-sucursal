import Card from '../../components/Card';

export default function BranchCard({ data, isCurrent, onSelect }) {
    return (
        <Card
            className={
                'cursor-pointer w-full text-left focus:outline-none ' +
                (isCurrent
                    ? 'border-2 border-primary bg-blue-50'
                    : 'border border-gray-200')
            }
            tabIndex={0}
            role="button"
            onClick={() => onSelect?.(data.id)}
            onKeyDown={e => {
                if (e.key === 'Enter' || e.key === ' ') onSelect?.(data.id);
            }}
        >
            <h2 className="text-xl font-bold mb-4">{data.name}</h2>
            <p className="text-gray-600">{data.id}</p>
        </Card>
    );
}