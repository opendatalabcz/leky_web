import React, { useState } from "react"
import { Filters, FilterValues } from "./Filters"
import { DrugTable } from "./DrugTable"
import "./DrugSelectionPanel.css"

export function DrugSelectionPanel() {
    const [filters, setFilters] = useState<FilterValues>({
        atcGroupId: null,
        substanceId: null,
        medicinalProductQuery: "",
        period: ""
    })

    const [shouldSearch, setShouldSearch] = useState(false)

    const handleFilterChange = (updated: FilterValues) => {
        setFilters(updated)
    }

    const handleSearchClick = () => {
        setShouldSearch(true)
    }

    const handleSearchComplete = () => {
        setShouldSearch(false)
    }

    const handleAddToCart = (drugId: string) => {
        console.log("Přidat do košíku:", drugId)
    }

    return (
        <section className="drug-selection-panel">
            <h2>Výběr léčiv</h2>
            <Filters
                filters={filters}
                onChange={handleFilterChange}
                onSearchClick={handleSearchClick}
            />
            <DrugTable
                filters={filters}
                onSelectDrug={handleAddToCart}
                triggerSearch={shouldSearch}
                onSearchComplete={handleSearchComplete}
            />
        </section>
    )
}
