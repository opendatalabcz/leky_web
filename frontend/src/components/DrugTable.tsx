import React, { useEffect, useState } from "react"
import { FilterValues } from "./Filters"

type Drug = {
    id: string
    name: string
}

type DrugTableProps = {
    filters: FilterValues
    triggerSearch: boolean
    onSearchComplete: () => void
    onSelectDrug: (drugId: string) => void
}

export function DrugTable({
                              filters,
                              triggerSearch,
                              onSearchComplete,
                              onSelectDrug
                          }: DrugTableProps) {
    const [drugs, setDrugs] = useState<Drug[]>([])
    const [loading, setLoading] = useState(false)

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

                const response = await fetch(`/api/medicinal-products?${params.toString()}`)
                const data = await response.json()
                setDrugs(data)
            } catch (error) {
                console.error("Chyba při načítání léčiv:", error)
            } finally {
                setLoading(false)
                onSearchComplete()
            }
        }

        fetchDrugs()
    }, [triggerSearch, filters, onSearchComplete])

    return (
        <div style={{ marginTop: "2rem" }}>
            <h3>Výsledky vyhledávání</h3>
            {loading && <p>Načítání dat...</p>}
            {!loading && drugs.length === 0 && <p>Žádné výsledky</p>}
            {!loading && drugs.length > 0 && (
                <table>
                    <thead>
                    <tr>
                        <th>Název léčiva</th>
                        <th>Akce</th>
                    </tr>
                    </thead>
                    <tbody>
                    {drugs.map((drug) => (
                        <tr key={drug.id}>
                            <td>{drug.name}</td>
                            <td>
                                <button onClick={() => onSelectDrug(drug.id)}>Přidat do košíku</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}
