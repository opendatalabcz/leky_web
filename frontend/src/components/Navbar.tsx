import {Link, useLocation} from "react-router-dom"
import "./Navbar.css"

export function Navbar() {
    const location = useLocation()

    const isActive = (path: string) => location.pathname === path

    return (
        <nav className="navbar">
            <Link to="/" className={isActive("/") ? "nav-link active" : "nav-link"}>
                Datové přehledy
            </Link>
            <Link to="/map" className={isActive("/map") ? "nav-link active" : "nav-link"}>
                Mapa
            </Link>
            <Link to="/about" className={isActive("/about") ? "nav-link active" : "nav-link"}>
                O projektu
            </Link>
        </nav>
    )
}
