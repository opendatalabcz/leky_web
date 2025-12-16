import React from "react"
import {
    Box,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent
} from "@mui/material"
import { YearMonthPicker } from "../YearMonthPicker"
import { MedicinalUnitMode, MedicinalUnitModeLabels } from "../../types/MedicinalUnitMode"

type Props = {
    dateFrom: Date | null
    dateTo: Date | null
    onChangeDateFrom: (date: Date | null) => void
    onChangeDateTo: (date: Date | null) => void

    medicinalUnitMode: MedicinalUnitMode
    onChangeMedicinalUnitMode: (mode: MedicinalUnitMode) => void
}

export const DistributionFiltersPanel: React.FC<Props> = ({
                                                              dateFrom,
                                                              dateTo,
                                                              onChangeDateFrom,
                                                              onChangeDateTo,
                                                              medicinalUnitMode,
                                                              onChangeMedicinalUnitMode
                                                          }) => {
    return (
        <Box
            display="flex"
            flexWrap="wrap"
            gap={2}
            mt={2}
            mb={3}
            maxWidth="800px" // nebo jiná hodnota, podle toho jak široké pole chceš
        >
            <Box flex={1} minWidth={{ xs: '100%', sm: 180 }}>
                <YearMonthPicker
                    label="Období od"
                    value={dateFrom}
                    onChange={onChangeDateFrom}
                    maxDate={dateTo ?? undefined}
                />
            </Box>

            <Box flex={1} minWidth={{ xs: '100%', sm: 180 }}>
                <YearMonthPicker
                    label="Období do"
                    value={dateTo}
                    onChange={onChangeDateTo}
                    minDate={dateFrom ?? undefined}
                />
            </Box>

            <Box flex={1} minWidth={{ xs: '100%', sm: 200 }}>
                <FormControl fullWidth size="small">
                    <InputLabel id="distribution-medicinal-unit-mode-select-label">
                        Jednotka množství léčiv
                    </InputLabel>
                    <Select
                        id="distribution-medicinal-unit-mode-select"
                        labelId="distribution-medicinal-unit-mode-select-label"
                        value={medicinalUnitMode}
                        label="Jednotka množství léčiv"
                        onChange={(e: SelectChangeEvent) =>
                            onChangeMedicinalUnitMode(e.target.value as MedicinalUnitMode)
                        }
                    >
                        {Object.values(MedicinalUnitMode).map((mode) => (
                            <MenuItem key={mode} value={mode}>
                                {MedicinalUnitModeLabels[mode]}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </Box>
        </Box>
    )
}
