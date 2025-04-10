import { useEffect, useState } from "react"
import { Filters, FilterValues, SearchMode } from "./Filters"
import { DrugTable } from "./DrugTable"
import { Cart } from "./Cart"
import { GroupedDrugTable } from "./GroupedDrugTable"

export function DrugSelectionPanel() {
    const [filters, setFilters] = useState<FilterValues>({
        searchMode: SearchMode.SUKL_CODE,
        atcGroupId: null,
        substanceId: null,
        medicinalProductQuery: "",
        period: ""
    })

    const [shouldSearch, setShouldSearch] = useState(false)

    const [filtersVersion, setFiltersVersion] = useState(0)

    const [groupedDrugs, setGroupedDrugs] = useState<any[]>([])
    const [loadingGrouped, setLoadingGrouped] = useState(false)

    const handleFilterChange = (updated: FilterValues) => {
        setFilters(updated)
        setFiltersVersion(prev => prev + 1) // bump version when filters change
    }

    const handleSearchClick = () => setShouldSearch(true)
    const handleSearchComplete = () => setShouldSearch(false)

    useEffect(() => {
        if (!shouldSearch || filters.searchMode !== SearchMode.REGISTRATION_NUMBER) return

        const fetchGrouped = async () => {
            setLoadingGrouped(true)

            try {
                const params = new URLSearchParams()
                if (filters.atcGroupId) params.append("atcGroupId", filters.atcGroupId.toString())
                if (filters.substanceId) params.append("substanceId", filters.substanceId.toString())
                if (filters.medicinalProductQuery) params.append("query", filters.medicinalProductQuery)
                if (filters.period) params.append("period", filters.period)

                const res = await fetch(`/api/medicinal-products/grouped-by-reg-number?${params.toString()}`)
                const data = await res.json()
                setGroupedDrugs(data)
            } catch (err) {
                console.error("Chyba při načítání seskupených léčiv:", err)
            } finally {
                setLoadingGrouped(false)
                handleSearchComplete()
            }
        }

        fetchGrouped()
    }, [shouldSearch, filters])

    return (
        <section className="drug-selection-panel">
            <Filters
                filters={filters}
                onChange={handleFilterChange}
                onSearchClick={handleSearchClick}
            />

            {filters.searchMode === SearchMode.SUKL_CODE ? (
                <DrugTable
                    filters={filters}
                    triggerSearch={shouldSearch}
                    onSearchComplete={handleSearchComplete}
                    filtersVersion={filtersVersion}
                    setTriggerSearch={setShouldSearch}
                />
            ) : (
                <GroupedDrugTable
                    filters={filters}
                    triggerSearch={shouldSearch}
                    onSearchComplete={handleSearchComplete}
                    filtersVersion={filtersVersion}
                    setTriggerSearch={setShouldSearch}
                />
            )}

            <Cart />
        </section>
    )
}
