import React from "react"
import { Link, useLocation } from "react-router-dom"
import { Box } from "@mui/material"

export function Navbar() {
    const location = useLocation()

    const isActive = (path: string) => location.pathname === path

    return (
        <Box
            component="nav"
            sx={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                flexWrap: "wrap",
                backgroundColor: "#192b47",
                padding: "0.75rem 1.5rem",
                color: "white"
            }}
        >
            <Box
                sx={{
                    display: "flex",
                    alignItems: "center",
                    flexWrap: "wrap",
                    gap: { xs: "1rem", sm: "2rem" },
                    width: "100%"
                }}
            >
                <Box
                    sx={{
                        fontSize: "1.25rem",
                        fontWeight: "bold",
                        whiteSpace: "nowrap",
                        mb: { xs: 1, sm: 0 }
                    }}
                >
                    Léčiva v datech
                </Box>

                <Box
                    sx={{
                        display: "flex",
                        gap: { xs: "0.75rem", sm: "1.25rem" },
                        flexWrap: "wrap"
                    }}
                >
                    <Link
                        to="/predepisovani-a-vydej"
                        style={{
                            color: isActive("/predepisovani-a-vydej") ? "white" : "#ddd",
                            textDecoration: "none",
                            padding: "0.5rem 0.25rem",
                            fontWeight: isActive("/predepisovani-a-vydej") ? 500 : "normal",
                            borderBottom: isActive("/predepisovani-a-vydej") ? "2px solid white" : "none"
                        }}
                    >
                        Předepisování a výdej
                    </Link>

                    <Link
                        to="/distribucni-tok"
                        style={{
                            color: isActive("/distribucni-tok") ? "white" : "#ddd",
                            textDecoration: "none",
                            padding: "0.5rem 0.25rem",
                            fontWeight: isActive("/distribucni-tok") ? 500 : "normal",
                            borderBottom: isActive("/distribucni-tok") ? "2px solid white" : "none"
                        }}
                    >
                        Distribuční tok
                    </Link>

                    <Link
                        to="/o-projektu"
                        style={{
                            color: isActive("/o-projektu") ? "white" : "#ddd",
                            textDecoration: "none",
                            padding: "0.5rem 0.25rem",
                            fontWeight: isActive("/o-projektu") ? 500 : "normal",
                            borderBottom: isActive("/o-projektu") ? "2px solid white" : "none"
                        }}
                    >
                        O projektu
                    </Link>
                </Box>
            </Box>
        </Box>
    )
}
