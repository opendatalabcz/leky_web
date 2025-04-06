import { Box, Typography } from "@mui/material"
import { YearMonthPicker } from "./YearMonthPicker"
import { useState } from "react"

export function VisualizationSettings() {
    const [dateFrom, setDateFrom] = useState<Date | null>(null)
    const [dateTo, setDateTo] = useState<Date | null>(null)

    return (
        <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
                Nastavení vizualizace
            </Typography>

            <Box display="flex" gap={2}>
                <YearMonthPicker
                    label="Období od"
                    value={dateFrom}
                    onChange={setDateFrom}
                    maxDate={dateTo ?? undefined}
                />
                <YearMonthPicker
                    label="Období do"
                    value={dateTo}
                    onChange={setDateTo}
                    minDate={dateFrom ?? undefined}
                />
            </Box>
        </Box>
    )
}
