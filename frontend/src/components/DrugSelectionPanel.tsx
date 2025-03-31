import {useState} from "react"
import {Filters, FilterValues} from "./Filters"
import {DrugTable} from "./DrugTable"
import {Cart} from "./Cart"

export function DrugSelectionPanel() {
    const [filters, setFilters] = useState<FilterValues>({
        atcGroupId: null,
        substanceId: null,
        medicinalProductQuery: "",
        period: ""
    })

    const [shouldSearch, setShouldSearch] = useState(false)

    const handleFilterChange = (updated: FilterValues) => setFilters(updated)
    const handleSearchClick = () => setShouldSearch(true)
    const handleSearchComplete = () => setShouldSearch(false)

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
                triggerSearch={shouldSearch}
                onSearchComplete={handleSearchComplete}
            />
            <Cart />
        </section>
    )
}
