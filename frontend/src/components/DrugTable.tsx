import React, { useEffect, useState } from "react"
import { FilterValues } from "./Filters"
import "./DrugTable.css"
import {useCart} from "./CartContext";

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
}

export function DrugTable({
                              filters,
                              triggerSearch,
                              onSearchComplete,
                          }: DrugTableProps) {
    const { addToCart } = useCart()
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
                params.append("searchMode", filters.searchMode)

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
        <div className="drug-table-container">
            <h3>Výsledky vyhledávání</h3>
            {loading && <p>Načítání dat...</p>}
            {!loading && drugs.length === 0 && <p>Žádné výsledky</p>}
            {!loading && drugs.length > 0 && (
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
                            <td>{drug.atcGroup ? `${drug.atcGroup.name} (${drug.atcGroup.code})` : "-"}</td>
                            <td>
                                <button onClick={() => addToCart(Number(drug.id))}>
                                    Přidat
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}
