import React, { useState, useEffect } from "react"
import { MedicinalProductFilterValues } from "../types/MedicinalProductFilterValues"
import { MedicinalProductSearchMode } from "../types/MedicinalProductSearchMode"
import { MedicinalProductFilters } from "./MedicinalProductFilters"
import { DrugSearchModeSwitch } from "./DrugSearchModeSwitch"
import { DrugTableNew } from "./DrugTableNew"
import { GroupedDrugTableNew } from "./GroupedDrugTableNew"
import { useUnifiedCart } from "./UnifiedCartContext"
import { Box } from "@mui/material"

type Props = {
    onCloseModal: () => void
    onAddSelected?: () => void
    refreshToken?: any
    onSelectionUpdate?: (count: number, handler: () => void) => void
}

export const DrugSearchSection: React.FC<Props> = ({
                                                       onCloseModal,
                                                       onAddSelected,
                                                       refreshToken,
                                                       onSelectionUpdate
                                                   }) => {
    const [filters, setFilters] = useState<MedicinalProductFilterValues>({
        searchMode: MedicinalProductSearchMode.SUKL_CODE,
        atcGroupId: null,
        substanceId: null,
        medicinalProductQuery: "",
        period: ""
    })

    const [shouldSearch, setShouldSearch] = useState(false)
    const [filtersVersion, setFiltersVersion] = useState(0)

    const [selectedIds, setSelectedIds] = useState<number[]>([])

    const { addSuklId, addRegistrationNumber } = useUnifiedCart()

    useEffect(() => {
        if (refreshToken) {
            setShouldSearch(true)
        }
    }, [refreshToken])

    const handleSearchModeChange = (mode: MedicinalProductSearchMode) => {
        setFilters(prev => ({ ...prev, searchMode: mode }))
    }

    const handleFilterChange = (updated: MedicinalProductFilterValues) => {
        setFilters(updated)
        setFiltersVersion(v => v + 1)
    }

    const handleSearchClick = () => setShouldSearch(true)
    const handleSearchComplete = () => setShouldSearch(false)

    const handleAddOne = (id: number) => {
        addSuklId(id)
        onAddSelected?.()
        onCloseModal()
    }

    const handleAddSelected = (selectedIds: number[]) => {
        selectedIds.forEach(addSuklId)
        onAddSelected?.()
        onCloseModal()
    }

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

            <Box mt={2}>
                {filters.searchMode === MedicinalProductSearchMode.SUKL_CODE ? (
                    <DrugTableNew
                        filters={filters}
                        triggerSearch={shouldSearch}
                        onSearchComplete={handleSearchComplete}
                        filtersVersion={filtersVersion}
                        setTriggerSearch={setShouldSearch}
                        onAddOne={handleAddOne}
                        onSelectionUpdate={(count, ids) => {
                            setSelectedIds(ids)
                            onSelectionUpdate?.(count, () => handleAddSelected(ids))
                        }}
                    />
                ) : (
                    <GroupedDrugTableNew
                        filters={filters}
                        triggerSearch={shouldSearch}
                        onSearchComplete={handleSearchComplete}
                        filtersVersion={filtersVersion}
                        setTriggerSearch={setShouldSearch}
                        onAddOne={(registrationNumber: string) => {
                            addRegistrationNumber(registrationNumber)
                            onAddSelected?.()
                            onCloseModal()
                        }}
                        onSelectionUpdate={(count, ids) => {
                            onSelectionUpdate?.(count, () => {
                                ids.forEach(regNum => addRegistrationNumber(regNum))
                                onAddSelected?.()
                                onCloseModal()
                            })
                        }}
                    />
                )}
            </Box>
        </>
    )
}
