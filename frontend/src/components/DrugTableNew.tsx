import React, { useEffect, useState } from "react"
import { MedicinalProductFilterValues } from "../types/MedicinalProductFilterValues"
import { Pagination } from "./Pagination"
import { useUnifiedCart } from "./UnifiedCartContext"
import "./DrugTableNew.css"

export type Drug = {
    id: number
    name: string
    suklCode: string
    registrationNumber: string | null
    supplementaryInformation: string | null
    atcGroup?: { code: string, name: string } | null
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

export const DrugTableNew: React.FC<Props> = ({
    filters,
    triggerSearch,
    onSearchComplete,
    filtersVersion,
    setTriggerSearch
}) => {
    const [data, setData] = useState<Drug[]>([])
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [loading, setLoading] = useState(false)
    const { addSuklId } = useUnifiedCart()

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
                params.append("searchMode", filters.searchMode)
                params.append("page", currentPage.toString())
                params.append("size", "5")

                const res = await fetch(`/api/medicinal-products?${params.toString()}`)
                const json: PagedResponse<Drug> = await res.json()

                setData(json.content)
                setTotalPages(json.totalPages)
            } catch (e) {
                console.error("Chyba při načítání dat:", e)
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
                            <th>SÚKL kód</th>
                            <th>Název</th>
                            <th>Doplněk</th>
                            <th>Registrační číslo</th>
                            <th>ATC skupina</th>
                            <th>Akce</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.map(drug => (
                            <tr key={drug.id}>
                                <td>{drug.suklCode}</td>
                                <td>{drug.name}</td>
                                <td>{drug.supplementaryInformation || "-"}</td>
                                <td>{drug.registrationNumber || "-"}</td>
                                <td>{drug.atcGroup ? `${drug.atcGroup.name} (${drug.atcGroup.code})` : "-"}</td>
                                <td>
                                    <button onClick={() => addSuklId(drug.id)}>Přidat</button>
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
