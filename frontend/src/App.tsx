import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom"
import {Navbar} from "./components/Navbar"
import {AboutPage} from "./pages/AboutPage"
import {UnifiedCartProvider} from "./components/UnifiedCartContext";
import {DistributionFlowPage} from "./pages/DistributionFlowPage";
import {PrescriptionDispensePage} from "./pages/PrescriptionDispensePage";

export default function App() {
    return (
        <UnifiedCartProvider>
            <BrowserRouter>
                <Navbar />
                <main style={{ padding: "2rem" }}>
                    <Routes>
                        <Route path="/predepisovani-a-vydej" element={<PrescriptionDispensePage />} />
                        <Route path="/distribucni-tok" element={<DistributionFlowPage />} />
                        <Route path="/o-projektu" element={<AboutPage />} />
                        <Route path="*" element={<Navigate to="/predepisovani-a-vydej" replace />} />
                    </Routes>
                </main>
            </BrowserRouter>
        </UnifiedCartProvider>
    )
}
