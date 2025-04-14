import React from "react"
import {
    Box,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent
} from "@mui/material"
import { YearMonthPicker } from "./YearMonthPicker"
import { MedicinalUnitMode, MedicinalUnitModeLabels } from "../types/MedicinalUnitMode"
import {
    PopulationNormalisationMode,
    PopulationNormalisationModeLabels
} from "../types/PopulationNormalisationMode"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"

type Props = {
    dateFrom: Date | null
    dateTo: Date | null
    onChangeDateFrom: (date: Date | null) => void
    onChangeDateTo: (date: Date | null) => void

    calculationMode: MedicinalUnitMode
    onChangeCalculationMode: (mode: MedicinalUnitMode) => void

    normalisationMode: PopulationNormalisationMode
    onChangeNormalisationMode: (mode: PopulationNormalisationMode) => void

    aggregationType: EReceptDataTypeAggregation
    onChangeAggregationType: (val: EReceptDataTypeAggregation) => void
}

export const EReceptFiltersPanel: React.FC<Props> = ({
                                                         dateFrom,
                                                         dateTo,
                                                         onChangeDateFrom,
                                                         onChangeDateTo,
                                                         calculationMode,
                                                         onChangeCalculationMode,
                                                         normalisationMode,
                                                         onChangeNormalisationMode,
                                                         aggregationType,
                                                         onChangeAggregationType
                                                     }) => {
    return (
        <Box
            display="flex"
            gap={4}
            flexWrap="wrap"
            alignItems="center"
            mt={2}
            mb={3}
        >
            <FormControl>
                <InputLabel id="aggregation-type-select-label">Typ eReceptů</InputLabel>
                <Select
                    id="aggregation-type-select"
                    labelId="aggregation-type-select-label"
                    value={aggregationType}
                    label="Typ eReceptů"
                    onChange={(e: SelectChangeEvent) =>
                        onChangeAggregationType(e.target.value as EReceptDataTypeAggregation)
                    }
                    size="small"
                    sx={{ minWidth: 200 }}
                >
                    <MenuItem value={EReceptDataTypeAggregation.PRESCRIBED}>Předepsané</MenuItem>
                    <MenuItem value={EReceptDataTypeAggregation.DISPENSED}>Vydané</MenuItem>
                    <MenuItem value={EReceptDataTypeAggregation.DIFFERENCE}>Rozdíl</MenuItem>
                </Select>
            </FormControl>

            <YearMonthPicker
                label="Období od"
                value={dateFrom}
                onChange={onChangeDateFrom}
                maxDate={dateTo ?? undefined}
            />

            <YearMonthPicker
                label="Období do"
                value={dateTo}
                onChange={onChangeDateTo}
                minDate={dateFrom ?? undefined}
            />

            <FormControl>
                <InputLabel id="medicinal-unit-mode-select-label">Jednotka množství léčiv</InputLabel>
                <Select
                    id="medicinal-unit-mode-select"
                    labelId="medicinal-unit-mode-select-label"
                    value={calculationMode}
                    label="Jednotka množství léčiv"
                    onChange={(e: SelectChangeEvent) =>
                        onChangeCalculationMode(e.target.value as MedicinalUnitMode)
                    }
                    size="small"
                    sx={{ minWidth: 200 }}
                >
                    {Object.values(MedicinalUnitMode).map((mode) => (
                        <MenuItem key={mode} value={mode}>
                            {MedicinalUnitModeLabels[mode]}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>

            <FormControl>
                <InputLabel id="population-normalisation-mode-select-label">Způsob normalizace</InputLabel>
                <Select
                    id="population-normalisation-mode-select"
                    labelId="population-normalisation-mode-select-label"
                    value={normalisationMode}
                    label="Způsob normalizace"
                    onChange={(e: SelectChangeEvent) =>
                        onChangeNormalisationMode(e.target.value as PopulationNormalisationMode)
                    }
                    size="small"
                    sx={{ minWidth: 200 }}
                >
                    {Object.values(PopulationNormalisationMode).map((mode) => (
                        <MenuItem key={mode} value={mode}>
                            {PopulationNormalisationModeLabels[mode]}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>
        </Box>
    )
}
