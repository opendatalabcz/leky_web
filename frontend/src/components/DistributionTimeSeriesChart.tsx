import React, { useMemo } from "react"
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    Legend,
    CartesianGrid,
    ResponsiveContainer,
    ReferenceArea
} from "recharts"
import { Box } from "@mui/material"
import { format, eachMonthOfInterval } from "date-fns"
import { DistributionTimeSeriesResponse } from "../services/distributionService"
import { MedicinalUnitMode, MedicinalUnitModeUnits } from "../types/MedicinalUnitMode"

type Props = {
    data: DistributionTimeSeriesResponse | undefined
    medicinalUnitMode: MedicinalUnitMode
    dateFrom?: Date | null
    dateTo?: Date | null
}

export const DistributionTimeSeriesChart: React.FC<Props> = ({
                                                                 data,
                                                                 medicinalUnitMode,
                                                                 dateFrom,
                                                                 dateTo
                                                             }) => {
    const { chartData, allFlowKeys, isPlaceholder } = useMemo(() => {
        if (!data || data.series.length === 0) {
            if (!dateFrom || !dateTo) {
                return {
                    chartData: [],
                    allFlowKeys: [],
                    isPlaceholder: true
                }
            }

            const intervals = eachMonthOfInterval({
                start: dateFrom,
                end: dateTo
            })

            return {
                chartData: intervals.map(d => ({
                    name: format(d, "yyyy-MM"),
                    "Distribuční tok": 0
                })),
                allFlowKeys: ["Distribuční tok"],
                isPlaceholder: true
            }
        }

        const allFlowKeys = Array.from(
            new Set(
                data.series.flatMap(entry =>
                    entry.flows.map(flow => `${flow.source} → ${flow.target}`)
                )
            )
        )

        const chartData = data.series.map(entry => {
            const flowMap = Object.fromEntries(
                entry.flows.map(flow => [
                    `${flow.source} → ${flow.target}`,
                    flow.value
                ])
            )

            const result: any = { name: entry.period }
            allFlowKeys.forEach(key => {
                result[key] = flowMap[key] || 0
            })

            return result
        })

        return {
            chartData,
            allFlowKeys,
            isPlaceholder: false
        }
    }, [data, dateFrom, dateTo])

    const highlightRange = useMemo(() => {
        if (!dateFrom || !dateTo) return null
        return {
            start: format(dateFrom, "yyyy-MM"),
            end: format(dateTo, "yyyy-MM")
        }
    }, [dateFrom, dateTo])

    const unitLabel = data ? MedicinalUnitModeUnits[data.medicinalUnitMode as MedicinalUnitMode] : ""

    const colorPalette = [
        "#1976d2",
        "#2e7d32",
        "#d32f2f",
        "#f9a825",
        "#6a1b9a",
        "#00838f",
        "#c2185b",
        "#5d4037"
    ]

    return (
        <Box sx={{ width: "100%", overflowX: "auto" }}>
            <Box sx={{ minWidth: "800px" }}>
                <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={chartData}>
                        <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip
                            formatter={(value: number) => [
                                `${value.toLocaleString("cs-CZ")} ${unitLabel}`
                            ]}
                            labelFormatter={(label) => `Období: ${label}`}
                        />
                        <Legend />

                        {!isPlaceholder && highlightRange && (
                            <ReferenceArea
                                x1={highlightRange.start}
                                x2={highlightRange.end}
                                strokeOpacity={0}
                                fill="#1976d2"
                                fillOpacity={0.1}
                            />
                        )}

                        {allFlowKeys.map((key, index) => (
                            <Line
                                key={key}
                                type="monotone"
                                dataKey={key}
                                stroke={
                                    isPlaceholder
                                        ? "#b0b0b0"
                                        : colorPalette[index % colorPalette.length]
                                }
                                strokeWidth={2}
                                dot={!isPlaceholder}
                                isAnimationActive={!isPlaceholder}
                            />
                        ))}
                    </LineChart>
                </ResponsiveContainer>
            </Box>
        </Box>
    )
}
