import React, {useState} from "react"
import {Filters, FilterValues} from "./Filters"
import {DrugTable} from "./DrugTable"

export function DrugSelectionPanel() {
    const [filters, setFilters] = useState<FilterValues>({
        atcGroupId: null,
        substanceId: null,
        medicinalProductQuery: "",
        period: ""
    })

    const handleFilterChange = (updated: FilterValues) => {
        setFilters(updated)
    }

    const handleAddToCart = (drugId: string) => {
        // TODO: implementace přidání do košíku
        console.log("Přidat do košíku:", drugId)
    }

    return (
        <section className="drug-selection-panel">
            <h2>Výběr léčiv</h2>
            <Filters filters={filters} onChange={handleFilterChange} />
            <DrugTable filters={filters} onSelectDrug={handleAddToCart} />
        </section>
    )
}
