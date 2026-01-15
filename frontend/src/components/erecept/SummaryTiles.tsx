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
    const formatPct = (val: number) => `${Math.abs(val).toFixed(1).replace(".", ",")}%`

    const prescribed = summary?.prescribed ?? 0
    const dispensed = summary?.dispensed ?? 0
    const difference = summary?.difference ?? 0
    const pct = summary ? summary.percentageDifference : 0

    const getDispenseStatusLabel = (val: number) => {
        if (val > 0) return "méně vydaných";
        if (val < 0) return "více vydaných";
        return "shoda s preskripcí";
    }

    const shouldShowSubLabel = prescribed > 0 || dispensed > 0;

    const tiles = [
        {
            label: "Předepsané",
            value: formatNum(prescribed),
            subLabel: null
        },
        {
            label: "Vydané",
            value: formatNum(dispensed),
            subLabel: null
        },
        {
            label: "Rozdíl",
            value: formatNum(Math.abs(difference)),
            subLabel: shouldShowSubLabel ? getDispenseStatusLabel(difference) : null
        },
        {
            label: "Rozdíl v %",
            value: formatPct(pct),
            subLabel: shouldShowSubLabel ? getDispenseStatusLabel(difference) : null
        },
    ]

    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                gap: 1,
                width: '100%',
                boxSizing: 'border-box'
            }}
        >
            {tiles.map((item, idx) => (
                <Box
                    key={`tile-${idx}`}
                    sx={{
                        width: '100%',
                        background: '#f8f9fb',
                        borderRadius: 1.5,
                        boxShadow: '0 1px 6px rgba(0,0,0,0.05)',
                        px: 2,
                        py: 1.5,
                        border: '1px solid #dce3ec',
                        textAlign: 'center',
                        boxSizing: 'border-box',
                        minHeight: item.subLabel ? 90 : 80
                    }}
                >
                    <Typography variant="body2" sx={{ color: '#54657e', fontWeight: 500, mb: 0.5 }}>
                        {item.label}
                    </Typography>

                    <Box
                        display="flex"
                        flexDirection="column"
                        justifyContent="center"
                        alignItems="center"
                    >
                        <Typography variant="h6" fontWeight={600} sx={{ lineHeight: 1.2 }}>
                            {item.value}
                        </Typography>

                        {item.subLabel && (
                            <Typography
                                variant="caption"
                                sx={{
                                    fontWeight: 500,
                                    mt: 0.2,
                                    color: item.subLabel.includes("méně") ? '#176AFF' :
                                        item.subLabel.includes("více") ? '#CC0000' : '#54657e'
                                }}
                            >
                                {item.subLabel}
                            </Typography>
                        )}
                    </Box>
                </Box>
            ))}
        </Box>
    )
}