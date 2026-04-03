import DashboardTabs from "../features/dashboard/DashboardTabs";

export default function DashboardPage() {
    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4 text-text">Dashboard</h1>
            <DashboardTabs />
        </div>
    );
}