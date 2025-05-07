import React, { useMemo } from "react"
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    Legend,
    CartesianGrid,
    ResponsiveContainer
} from "recharts"
import { Box, Typography } from "@mui/material"
import { DistributionTimeSeriesResponse } from "../services/distributionService"

type Props = {
    data: DistributionTimeSeriesResponse | undefined
}

export const DistributionTimeSeriesChart: React.FC<Props> = ({ data }) => {
    const { chartData, allFlowKeys } = useMemo(() => {
        if (!data || data.series.length === 0) {
            return { chartData: [], allFlowKeys: [] }
        }

        // Najdeme všechny unikátní source → target kombinace
        const allFlowKeys = Array.from(
            new Set(data.series.flatMap(entry =>
                entry.flows.map(flow => `${flow.source} → ${flow.target}`)
            ))
        )

        // Připravíme data pro graf
        const chartData = data.series.map(entry => {
            const flowMap = Object.fromEntries(
                entry.flows.map(flow => [`${flow.source} → ${flow.target}`, flow.value])
            )

            const result: any = { name: entry.period }
            allFlowKeys.forEach(key => {
                result[key] = flowMap[key] || 0
            })

            return result
        })

        return { chartData, allFlowKeys }
    }, [data])

    // Paleta barev (můžeš si upravit, nebo přidat více barev)
    const colorPalette = [
        "#1976d2", // modrá
        "#2e7d32", // zelená
        "#d32f2f", // červená
        "#f9a825", // žlutá
        "#6a1b9a", // fialová
        "#00838f", // tyrkysová
        "#c2185b", // růžová
        "#5d4037"  // hnědá
    ]

    if (!data || data.series.length === 0) {
        return (
            <Typography color="text.secondary">
                Žádná data k zobrazení.
            </Typography>
        )
    }

    return (
        <Box mt={5}>
            <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                    <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip
                        formatter={(value: number) => [`${value.toLocaleString("cs-CZ")} balení`]}
                        labelFormatter={(label) => `Období: ${label}`}
                    />
                    <Legend />

                    {allFlowKeys.map((key, index) => (
                        <Line
                            key={key}
                            type="monotone"
                            dataKey={key}
                            stroke={colorPalette[index % colorPalette.length]}
                            strokeWidth={2}
                            dot={{ r: 2 }}
                            activeDot={{ r: 5 }}
                        />
                    ))}
                </LineChart>
            </ResponsiveContainer>
        </Box>
    )
}
