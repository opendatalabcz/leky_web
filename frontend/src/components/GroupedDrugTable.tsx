import React, { useEffect, useState } from "react"
import "./DrugTable.css"
import { FilterValues } from "./Filters"
import { Pagination } from "./Pagination"

type GroupedDrug = {
    registrationNumber: string
    suklCodes: string[]
    names: string[]
    strengths: string[]
    dosageForms: { id: number; code: string; name?: string }[]
    administrationRoutes: { id: number; code: string; name?: string }[]
    atcGroups: { id: number; code: string; name: string }[]
}

type PagedResponse<T> = {
    content: T[]
    totalPages: number
    totalElements: number
    page: number
    size: number
}

type Props = {
    filters: FilterValues
    triggerSearch: boolean
    onSearchComplete: () => void
    filtersVersion: number
    setTriggerSearch: (value: boolean) => void
}

export function GroupedDrugTable({
                                     filters,
                                     triggerSearch,
                                     onSearchComplete,
                                     filtersVersion,
                                     setTriggerSearch
                                 }: Props) {
    const [data, setData] = useState<GroupedDrug[]>([])
    const [loading, setLoading] = useState(false)
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)

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
                params.append("size", "10")

                const res = await fetch(`/api/medicinal-products/grouped-by-reg-number?${params.toString()}`)
                const json: PagedResponse<GroupedDrug> = await res.json()

                setData(json.content)
                setTotalPages(json.totalPages)
            } catch (err) {
                console.error("Chyba při načítání seskupených léčiv:", err)
            } finally {
                setLoading(false)
                onSearchComplete()
            }
        }

        fetchData()
    }, [triggerSearch, currentPage, filters, onSearchComplete])

    const handlePageChange = (page: number) => {
        setCurrentPage(page)
        setTriggerSearch(true)
    }

    return (
        <div className="drug-table-container">
            <h3>Výsledky vyhledávání (podle registračního čísla)</h3>
            {loading && <p>Načítání dat...</p>}
            {!loading && data.length === 0 && <p>Žádné výsledky</p>}

            {!loading && data.length > 0 && (
                <>
                    <table className="drug-table">
                        <thead>
                        <tr>
                            <th>Registrační číslo</th>
                            <th>Název</th>
                            <th>Síla</th>
                            <th>Léková forma</th>
                            <th>Cesta podání</th>
                            <th>ATC skupina</th>
                            <th>Počet SÚKL kódů</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.map((drug) => (
                            <tr key={drug.registrationNumber}>
                                <td>{drug.registrationNumber}</td>
                                <td>{renderList(drug.names)}</td>
                                <td>{renderList(drug.strengths)}</td>
                                <td>{renderObjectList(drug.dosageForms)}</td>
                                <td>{renderObjectList(drug.administrationRoutes)}</td>
                                <td>{renderObjectList(drug.atcGroups)}</td>
                                <td>{drug.suklCodes.length}</td>
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

function renderList(items: string[]) {
    if (items.length === 1) return items[0]
    return (
        <ul style={{ margin: 0, paddingLeft: "1.2rem" }}>
            {items.map((item, idx) => (
                <li key={idx}>{item}</li>
            ))}
        </ul>
    )
}

function renderObjectList(items: { code: string; name?: string }[]) {
    if (items.length === 1) {
        const { code, name } = items[0]
        return name ? `${name} (${code})` : code
    }

    return (
        <ul style={{ margin: 0, paddingLeft: "1.2rem" }}>
            {items.map((item, idx) => (
                <li key={idx}>
                    {item.name ? `${item.name} (${item.code})` : item.code}
                </li>
            ))}
        </ul>
    )
}
