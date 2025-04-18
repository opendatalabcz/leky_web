import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import { Navbar } from "./components/Navbar"
import { AboutPage } from "./pages/AboutPage"
import { UnifiedCartProvider } from "./components/UnifiedCartContext"
import { DistributionPage } from "./pages/DistributionPage"
import { EReceptPage } from "./pages/EReceptPage"
import { FilterProvider } from "./components/FilterContext"

export default function App() {
    return (
        <UnifiedCartProvider>
            <FilterProvider>
                <BrowserRouter>
                    <Navbar />
                    <main style={{ padding: "2rem" }}>
                        <Routes>
                            <Route path="/predepisovani-a-vydej" element={<EReceptPage />} />
                            <Route path="/distribucni-tok" element={<DistributionPage />} />
                            <Route path="/o-projektu" element={<AboutPage />} />
                            <Route path="*" element={<Navigate to="/predepisovani-a-vydej" replace />} />
                        </Routes>
                    </main>
                </BrowserRouter>
            </FilterProvider>
        </UnifiedCartProvider>
    )
}
