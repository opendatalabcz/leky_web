import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import { Navbar } from "./components/Navbar"
import { AboutPage } from "./pages/AboutPage"
import { DrugCartProvider } from "./components/drug-select-modal/DrugCartContext"
import { DistributionPage } from "./pages/DistributionPage"
import { EReceptPage } from "./pages/EReceptPage"
import { FilterProvider } from "./components/FilterContext"
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {SiteFooter} from "./components/SiteFooter";

const queryClient = new QueryClient()

export default function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <DrugCartProvider>
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
                        <SiteFooter />
                    </BrowserRouter>
                </FilterProvider>
            </DrugCartProvider>
        </QueryClientProvider>
    )
}
