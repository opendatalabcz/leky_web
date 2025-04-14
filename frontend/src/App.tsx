import {BrowserRouter, Route, Routes} from "react-router-dom"
import {Navbar} from "./components/Navbar"
import {HomePage} from "./pages/HomePage"
import {AboutPage} from "./pages/AboutPage"
import {UnifiedCartProvider} from "./components/UnifiedCartContext";

export default function App() {
    return (
        <UnifiedCartProvider>
            <BrowserRouter>
                <Navbar />
                <main style={{ padding: "2rem" }}>
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/about" element={<AboutPage />} />
                    </Routes>
                </main>
            </BrowserRouter>
        </UnifiedCartProvider>
    )
}
