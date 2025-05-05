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
    const chartData = useMemo(() => {
        if (!data || data.series.length === 0) return []

        return data.series.map(entry => ({
            name: entry.period,
            "MAH → Distributor": entry.mahToDistributor,
            "Distributor → Lékárna": entry.distributorToPharmacy,
            "Lékárna → Pacient": entry.pharmacyToPatient
        }))
    }, [data])

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

                    <Line
                        type="monotone"
                        dataKey="MAH → Distributor"
                        stroke="#1976d2"
                        strokeWidth={2}
                        dot={{ r: 2 }}
                        activeDot={{ r: 5 }}
                    />
                    <Line
                        type="monotone"
                        dataKey="Distributor → Lékárna"
                        stroke="#2e7d32"
                        strokeWidth={2}
                        dot={{ r: 2 }}
                        activeDot={{ r: 5 }}
                    />
                    <Line
                        type="monotone"
                        dataKey="Lékárna → Pacient"
                        stroke="#d32f2f"
                        strokeWidth={2}
                        dot={{ r: 2 }}
                        activeDot={{ r: 5 }}
                    />
                </LineChart>
            </ResponsiveContainer>
        </Box>
    )
}
