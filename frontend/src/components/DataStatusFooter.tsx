import React from "react"
import { Box, Typography } from "@mui/material"
import { format } from "date-fns"
import { cs } from "date-fns/locale"

export const DataStatusFooter: React.FC = () => {
    const updatedAt = new Date(2025, 3, 9)
    const dataMonth = new Date(2025, 2)

    return (
        <Box mt={4} textAlign="center">
            <Typography variant="body2" color="text.secondary">
                Poslední aktualizace dat proběhla {format(updatedAt, "d. MMMM yyyy", { locale: cs })} stažením dat za {format(dataMonth, "LLLL yyyy", { locale: cs })}.
            </Typography>
        </Box>
    )
}
