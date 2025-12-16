import React, { useCallback, useState } from "react"
import { MedicinalProductFilterValues } from "../../types/MedicinalProductFilterValues"
import { MedicinalProductSearchMode } from "../../types/MedicinalProductSearchMode"
import { DrugFilters } from "./DrugFilters"
import { DrugSearchModeSwitch } from "./DrugSearchModeSwitch"
import { DrugTableBySuklCode } from "./DrugTableBySuklCode"
import { DrugTableByRegNumber } from "./DrugTableByRegNumber"
import { useDrugCart } from "./DrugCartContext"
import { Box, useTheme } from "@mui/material"

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
        searchMode: MedicinalProductSearchMode.REGISTRATION_NUMBER,
        atcGroupId: null,
        substanceId: null,
        medicinalProductQuery: "",
        period: ""
    })

    const [shouldSearch, setShouldSearch] = useState(false)
    const [filtersVersion, setFiltersVersion] = useState(0)
    const [selectedIds, setSelectedIds] = useState<number[]>([])
    const { addSuklId, addRegistrationNumber } = useDrugCart()
    const theme = useTheme()

    React.useEffect(() => {
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

    const handleAddSelected = (ids: number[] | string[]) => {
        ids.forEach(id => {
            if (typeof id === "number") addSuklId(id)
            if (typeof id === "string") addRegistrationNumber(id)
        })
        onAddSelected?.()
        onCloseModal()
    }

    const stableSelectionUpdate = useCallback(
        (count: number, ids: number[] | string[]) => {
            setSelectedIds(ids as number[])
            onSelectionUpdate?.(count, () => handleAddSelected(ids))
        },
        [onSelectionUpdate]
    )

    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                gap: 1,
                width: '100%',
                boxSizing: 'border-box',
                p: { xs: 1, sm: 2 }
            }}
        >
            <DrugSearchModeSwitch
                searchMode={filters.searchMode}
                onChange={handleSearchModeChange}
            />

            <DrugFilters
                filters={filters}
                onChange={handleFilterChange}
                onSearchClick={handleSearchClick}
            />

            {filters.searchMode === MedicinalProductSearchMode.SUKL_CODE ? (
                <DrugTableBySuklCode
                    filters={filters}
                    triggerSearch={shouldSearch}
                    onSearchComplete={handleSearchComplete}
                    filtersVersion={filtersVersion}
                    setTriggerSearch={setShouldSearch}
                    onAddOne={handleAddOne}
                    onSelectionUpdate={(count, ids) =>
                        stableSelectionUpdate(count, ids)
                    }
                />
            ) : (
                <DrugTableByRegNumber
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
                    onSelectionUpdate={(count, ids) =>
                        stableSelectionUpdate(count, ids)
                    }
                />
            )}
        </Box>
    )
}
