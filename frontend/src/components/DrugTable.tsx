import React, { useEffect, useState } from "react"
import { FilterValues } from "./Filters"
import "./DrugTable.css"
import { useCart } from "./CartContext"
import { Pagination } from "./Pagination"

export type Drug = {
    id: string
    name: string
    suklCode: string
    registrationNumber: string | null
    supplementaryInformation: string | null
    atcGroup?: {
        code: string
        name: string
    } | null
}

type DrugTableProps = {
    filters: FilterValues
    triggerSearch: boolean
    onSearchComplete: () => void
    filtersVersion: number
    setTriggerSearch: (value: boolean) => void
}

type PagedResponse<T> = {
    content: T[]
    totalPages: number
    totalElements: number
    page: number
    size: number
}

export function DrugTable({
                              filters,
                              triggerSearch,
                              onSearchComplete,
                              filtersVersion,
                              setTriggerSearch
                          }: DrugTableProps) {
    const { addToCart } = useCart()
    const [drugs, setDrugs] = useState<Drug[]>([])
    const [loading, setLoading] = useState(false)
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)

    useEffect(() => {
        setCurrentPage(0)
    }, [filtersVersion])

    useEffect(() => {
        if (!triggerSearch) return

        const fetchDrugs = async () => {
            setLoading(true)

            try {
                const params = new URLSearchParams()
                if (filters.atcGroupId) params.append("atcGroupId", filters.atcGroupId.toString())
                if (filters.substanceId) params.append("substanceId", filters.substanceId.toString())
                if (filters.medicinalProductQuery) params.append("query", filters.medicinalProductQuery)
                if (filters.period) params.append("period", filters.period)
                params.append("searchMode", filters.searchMode)
                params.append("page", currentPage.toString())
                params.append("size", "10")

                const response = await fetch(`/api/medicinal-products?${params.toString()}`)
                const data: PagedResponse<Drug> = await response.json()

                setDrugs(data.content)
                setTotalPages(data.totalPages)
            } catch (error) {
                console.error("Chyba při načítání léčiv:", error)
            } finally {
                setLoading(false)
                onSearchComplete()
            }
        }

        fetchDrugs()
    }, [triggerSearch, currentPage, filters, onSearchComplete])

    const handlePageChange = (page: number) => {
        setCurrentPage(page)
        setTriggerSearch(true)
    }

    return (
        <div className="drug-table-container">
            <h3>Výsledky vyhledávání</h3>
            {loading && <p>Načítání dat...</p>}
            {!loading && drugs.length === 0 && <p>Žádné výsledky</p>}
            {!loading && drugs.length > 0 && (
                <>
                    <table className="drug-table">
                        <thead>
                        <tr>
                            <th>SÚKL kód</th>
                            <th>Název</th>
                            <th>Doplněk názvu</th>
                            <th>Registrační číslo</th>
                            <th>ATC skupina</th>
                            <th>Akce</th>
                        </tr>
                        </thead>
                        <tbody>
                        {drugs.map((drug) => (
                            <tr key={drug.id}>
                                <td>{drug.suklCode}</td>
                                <td>{drug.name}</td>
                                <td>{drug.supplementaryInformation || "-"}</td>
                                <td>{drug.registrationNumber || "-"}</td>
                                <td>
                                    {drug.atcGroup
                                        ? `${drug.atcGroup.name} (${drug.atcGroup.code})`
                                        : "-"}
                                </td>
                                <td>
                                    <button onClick={() => addToCart(Number(drug.id))}>
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
