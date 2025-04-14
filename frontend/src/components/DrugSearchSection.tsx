import React, { useState } from "react"
import { MedicinalProductFilterValues } from "../types/MedicinalProductFilterValues"
import { MedicinalProductSearchMode } from "../types/MedicinalProductSearchMode"
import { MedicinalProductFilters } from "./MedicinalProductFilters"
import { DrugTableNew } from "./DrugTableNew"
import { GroupedDrugTableNew } from "./GroupedDrugTableNew"
import { DrugSearchModeSwitch } from "./DrugSearchModeSwitch"

export const DrugSearchSection: React.FC = () => {
    const [filters, setFilters] = useState<MedicinalProductFilterValues>({
        searchMode: MedicinalProductSearchMode.SUKL_CODE,
        atcGroupId: null,
        substanceId: null,
        medicinalProductQuery: "",
        period: ""
    })

    const [shouldSearch, setShouldSearch] = useState(false)
    const [filtersVersion, setFiltersVersion] = useState(0)

    const handleSearchModeChange = (mode: MedicinalProductSearchMode) => {
        setFilters(prev => ({ ...prev, searchMode: mode }))
    }

    const handleFilterChange = (updated: MedicinalProductFilterValues) => {
        setFilters(updated)
        setFiltersVersion(v => v + 1)
    }

    const handleSearchClick = () => setShouldSearch(true)
    const handleSearchComplete = () => setShouldSearch(false)

    return (
        <>
            <DrugSearchModeSwitch
                searchMode={filters.searchMode}
                onChange={handleSearchModeChange}
            />

            <MedicinalProductFilters
                filters={filters}
                onChange={handleFilterChange}
                onSearchClick={handleSearchClick}
            />

            {filters.searchMode === MedicinalProductSearchMode.SUKL_CODE ? (
                <DrugTableNew
                    filters={filters}
                    triggerSearch={shouldSearch}
                    onSearchComplete={handleSearchComplete}
                    filtersVersion={filtersVersion}
                    setTriggerSearch={setShouldSearch}
                />
            ) : (
                <GroupedDrugTableNew
                    filters={filters}
                    triggerSearch={shouldSearch}
                    onSearchComplete={handleSearchComplete}
                    filtersVersion={filtersVersion}
                    setTriggerSearch={setShouldSearch}
                />
            )}
        </>
    )
}
