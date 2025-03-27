import {Link, useLocation} from "react-router-dom"

export function Navbar() {
    const location = useLocation()

    const isActive = (path: string) => location.pathname === path

    return (
        <nav className="bg-white shadow p-4 flex gap-6 text-lg font-medium">
            <Link
                to="/"
                className={isActive("/") ? "text-blue-600 underline" : "text-gray-700 hover:text-blue-600"}
            >
                Datové přehledy
            </Link>
            <Link
                to="/map"
                className={isActive("/map") ? "text-blue-600 underline" : "text-gray-700 hover:text-blue-600"}
            >
                Mapa
            </Link>
            <Link
                to="/about"
                className={isActive("/about") ? "text-blue-600 underline" : "text-gray-700 hover:text-blue-600"}
            >
                O projektu
            </Link>
        </nav>
    )
}
