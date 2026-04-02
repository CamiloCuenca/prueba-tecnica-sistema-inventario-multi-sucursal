import Card from "../components/Card";

export default function LoginForm() {
    
    return (
        <Card>
            <h1 className="text-2xl font-bold mb-4">Login</h1>
            <form className="space-y-4">
                <div>
                    <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Email</label>
                    <input type="email" id="email" className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:ring-primary focus:border-primary sm:text-sm" placeholder="Enter your email" />
                </div>
                <div>
                    <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Password</label>
                    <input type="password" id="password" className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:ring-primary focus:border-primary sm:text-sm" placeholder="Enter your password" />
                </div>
                <button type="submit" className="bg-primary hover:bg-primary-hover text-white font-bold py-2 px-4 rounded">
                    Login
                </button>
            </form>

        </Card>
    );

}
