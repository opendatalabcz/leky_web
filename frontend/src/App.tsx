import { BrowserRouter, Routes, Route } from "react-router-dom"
import { Navbar } from "./components/Navbar"
import { HomePage } from "./pages/HomePage"
import { AboutPage } from "./pages/AboutPage"
import MapOverviewPage from "./pages/MapOverviewPage";

export default function App() {
    return (
        <BrowserRouter>
            <Navbar />
            <main style={{ padding: "2rem" }}>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/about" element={<AboutPage />} />
                    <Route path="/map" element={<MapOverviewPage />} />
                </Routes>
            </main>
        </BrowserRouter>
    )
}
