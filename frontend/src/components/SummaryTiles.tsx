import React from "react"
import { Box, Typography } from "@mui/material"

type SummaryItem = {
    label: string
    value: string
}

const summaryData: SummaryItem[] = [
    { label: "Předepsané", value: "1 200 000" },
    { label: "Vydané", value: "1 100 000" },
    { label: "Rozdíl", value: "100 000" },
    { label: "% Rozdíl", value: "8.3%" }
]

export const SummaryTiles: React.FC = () => {
    return (
        <Box display="flex" flexDirection="column" gap={1}>
            {summaryData.map((item, index) => (
                <Box
                    key={index}
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
