import { Box, Typography } from "@mui/material"
import { YearMonthPicker } from "./YearMonthPicker"
import { FC } from "react"
import { CalculationMode, CalculationModeLabels } from "../types/CalculationMode"
import { NormalisationMode } from "../types/NormalisationMode"

type Props = {
    dateFrom: Date | null
    dateTo: Date | null
    onChangeDateFrom: (date: Date | null) => void
    onChangeDateTo: (date: Date | null) => void
    calculationMode: CalculationMode
    onChangeCalculationMode: (mode: CalculationMode) => void
    normalisationMode: NormalisationMode
    onChangeNormalisationMode: (mode: NormalisationMode) => void
}

export const VisualizationSettings: FC<Props> = ({
    dateFrom,
    dateTo,
    onChangeDateFrom,
    onChangeDateTo,
    calculationMode,
    onChangeCalculationMode,
    normalisationMode,
    onChangeNormalisationMode
}) => {
    return (
        <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
                Nastavení vizualizace
            </Typography>

            <Box display="flex" gap={2} mb={2}>
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
            </Box>

            <Box display="flex" gap={2}>
                <div>
                    <label>Mód výpočtu:</label>
                    <select
                        value={calculationMode}
                        onChange={(e) => onChangeCalculationMode(e.target.value as CalculationMode)}
                        style={{ padding: "0.4rem", fontSize: "1rem", marginLeft: "0.5rem" }}
                    >
                        {Object.values(CalculationMode).map((mode) => (
                            <option key={mode} value={mode}>
                                {CalculationModeLabels[mode]}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label>Normalizace:</label>
                    <select
                        value={normalisationMode}
                        onChange={(e) => onChangeNormalisationMode(e.target.value as NormalisationMode)}
                        style={{ padding: "0.4rem", fontSize: "1rem", marginLeft: "0.5rem" }}
                    >
                        <option value={NormalisationMode.ABSOLUTE}>Absolutní čísla</option>
                        <option value={NormalisationMode.PER_1000}>Na 1000 obyvatel</option>
                    </select>
                </div>
            </Box>
        </Box>
    )
}
