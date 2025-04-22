import React from "react"
import { Box, Typography } from "@mui/material"

type Props = {
    summary?: {
        prescribed: number
        dispensed: number
        difference: number
        percentageDifference: number
    }
}

export const SummaryTiles: React.FC<Props> = ({ summary }) => {
    const formatNum = (val: number) => val.toLocaleString("cs-CZ")
    const formatPct = (val: number) => `${val.toFixed(1).replace(".", ",")}%`

    const prescribed = summary?.prescribed ?? 0
    const dispensed = summary?.dispensed ?? 0
    const difference = summary?.difference ?? 0
    const pct = summary ? summary.percentageDifference : 0

    const tiles = [
        { label: "Předepsané", value: formatNum(prescribed) },
        { label: "Vydané", value: formatNum(dispensed) },
        { label: "Rozdíl", value: formatNum(difference) },
        { label: "% Rozdíl", value: formatPct(pct) },
    ]

    return (
        <Box display="flex" flexDirection="column" gap={1}>
            {tiles.map((item, idx) => (
                <Box
                    key={idx}
                    sx={{
                        background: "#f8f9fb",
                        borderRadius: 1.5,
                        boxShadow: "0 1px 6px rgba(0,0,0,0.05)",
                        px: 2,
                        py: 1.5,
                        width: 160,
                        border: "1px solid #dce3ec"
                    }}
                >
                    <Typography
                        variant="body2"
                        sx={{ color: "#54657e", fontWeight: 500, mb: 0.5 }}
                    >
                        {item.label}
                    </Typography>

                    <Box display="flex" justifyContent="center" alignItems="center" height={32}>
                        <Typography variant="h6" fontWeight={600}>
                            {item.value}
                        </Typography>
                    </Box>
                </Box>
            ))}
        </Box>
    )
}

