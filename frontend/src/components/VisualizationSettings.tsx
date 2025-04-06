import { Box, Typography } from "@mui/material"
import { YearMonthPicker } from "./YearMonthPicker"
import { FC } from "react"

type Props = {
    dateFrom: Date | null
    dateTo: Date | null
    onChangeDateFrom: (date: Date | null) => void
    onChangeDateTo: (date: Date | null) => void
}

export const VisualizationSettings: FC<Props> = ({
    dateFrom,
    dateTo,
    onChangeDateFrom,
    onChangeDateTo
}) => {
    return (
        <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
                Nastavení vizualizace
            </Typography>

            <Box display="flex" gap={2}>
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
        </Box>
    )
}
