import Card from '../../components/Card';

export default function BranchCard({ data, isCurrent }) {
    return (
        <Card
            className={
                isCurrent
                    ? 'border-2 border-primary bg-blue-50'
                    : 'border border-gray-200'
            }
        >
            <h2 className="text-xl font-bold mb-4">{data.name}</h2>
            <p className="text-gray-600">{data.id}</p>
        </Card>
    );
}