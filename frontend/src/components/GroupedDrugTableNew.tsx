import React, { useEffect, useState } from "react"
import { MedicinalProductFilterValues } from "../types/MedicinalProductFilterValues"
import { Pagination } from "./Pagination"
import { useUnifiedCart } from "./UnifiedCartContext"

export type GroupedDrug = {
    registrationNumber: string
    names: string[]
    suklCodes: string[]
    strengths: string[]
    dosageForms: { code: string, name?: string }[]
    administrationRoutes: { code: string, name?: string }[]
    atcGroups: { code: string, name: string }[]
}

type PagedResponse<T> = {
    content: T[]
    totalPages: number
    page: number
    size: number
    totalElements: number
}

type Props = {
    filters: MedicinalProductFilterValues
    triggerSearch: boolean
    onSearchComplete: () => void
    filtersVersion: number
    setTriggerSearch: (val: boolean) => void
}

export const GroupedDrugTableNew: React.FC<Props> = ({
    filters,
    triggerSearch,
    onSearchComplete,
    filtersVersion,
    setTriggerSearch
}) => {
    const [data, setData] = useState<GroupedDrug[]>([])
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [loading, setLoading] = useState(false)
    const { addRegistrationNumber } = useUnifiedCart()

    useEffect(() => {
        setCurrentPage(0)
    }, [filtersVersion])

    useEffect(() => {
        if (!triggerSearch) return

        const fetchData = async () => {
            setLoading(true)

            try {
                const params = new URLSearchParams()
                if (filters.atcGroupId) params.append("atcGroupId", filters.atcGroupId.toString())
                if (filters.substanceId) params.append("substanceId", filters.substanceId.toString())
                if (filters.medicinalProductQuery) params.append("query", filters.medicinalProductQuery)
                if (filters.period) params.append("period", filters.period)
                params.append("page", currentPage.toString())
                params.append("size", "5")

                const res = await fetch(`/api/medicinal-products/grouped-by-reg-number?${params.toString()}`)
                const json: PagedResponse<GroupedDrug> = await res.json()

                setData(json.content)
                setTotalPages(json.totalPages)
            } catch (e) {
                console.error("Chyba při načítání:", e)
            } finally {
                setLoading(false)
                onSearchComplete()
            }
        }

        fetchData()
    }, [triggerSearch, filters, currentPage])

    const handlePageChange = (page: number) => {
        setCurrentPage(page)
        setTriggerSearch(true)
    }

    return (
        <div className="drug-table-container">
            {loading && <p>Načítání...</p>}
            {!loading && data.length === 0 && <p>Žádné výsledky.</p>}
            {!loading && data.length > 0 && (
                <>
                    <table className="drug-table">
                        <thead>
                        <tr>
                            <th>Registrační číslo</th>
                            <th>Názvy</th>
                            <th>Síly</th>
                            <th>Lékové formy</th>
                            <th>Cesty podání</th>
                            <th>ATC skupiny</th>
                            <th>SÚKL kódy</th>
                            <th>Akce</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.map(drug => (
                            <tr key={drug.registrationNumber}>
                                <td>{drug.registrationNumber}</td>
                                <td>{drug.names.join(", ")}</td>
                                <td>{drug.strengths.join(", ")}</td>
                                <td>{drug.dosageForms.map(d => d.name || d.code).join(", ")}</td>
                                <td>{drug.administrationRoutes.map(r => r.name || r.code).join(", ")}</td>
                                <td>{drug.atcGroups.map(a => `${a.name} (${a.code})`).join(", ")}</td>
                                <td>{drug.suklCodes.length}</td>
                                <td>
                                    <button onClick={() => addRegistrationNumber(drug.registrationNumber)}>
                                        Přidat
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        onPageChange={handlePageChange}
                    />
                </>
            )}
        </div>
    )
}
