import React from "react"
import { Link, useLocation } from "react-router-dom"
import "./Navbar.css"

export function Navbar() {
    const location = useLocation()

    const isActive = (path: string) => location.pathname === path

    return (
        <nav className="navbar">
            <div className="navbar-left">
                <div className="navbar-title">Léčiva v datech</div>

                <div className="nav-links">
                    <Link to="/predepisovani-a-vydej" className={isActive("/predepisovani-a-vydej") ? "nav-link active" : "nav-link"}>
                        Předepisování a výdej
                    </Link>

                    <Link to="/distribucni-tok" className={isActive("/distribucni-tok") ? "nav-link active" : "nav-link"}>
                        Distribuční tok
                    </Link>

                    <Link to="/o-projektu" className={isActive("/o-projektu") ? "nav-link active" : "nav-link"}>
                        O projektu
                    </Link>
                </div>
            </div>
        </nav>
    )
}
