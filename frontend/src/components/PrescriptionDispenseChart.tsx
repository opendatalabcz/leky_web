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
import {
    Box,
    MenuItem,
    Select,
    FormControl,
    InputLabel,
    Typography
} from "@mui/material"
import { format } from "date-fns"
import { FullTimeSeriesResponse } from "../services/ereceptService"
import { TimeGranularity, TimeGranularityLabels } from "../types/TimeGranularity"

type Props = {
    data: FullTimeSeriesResponse | undefined
    selectedDistrict: string | null
    onDistrictChange: (value: string | null) => void
    districtNameMap: Record<string, string>
    timeGranularity: TimeGranularity
    onTimeGranularityChange: (value: TimeGranularity) => void
    dateFrom?: Date | null
    dateTo?: Date | null
}

export const PrescriptionDispenseChart: React.FC<Props> = ({
                                                               data,
                                                               selectedDistrict,
                                                               onDistrictChange,
                                                               districtNameMap,
                                                               timeGranularity,
                                                               onTimeGranularityChange,
                                                               dateFrom,
                                                               dateTo
                                                           }) => {
    const chartData = useMemo(() => {
        if (!data || data.series.length === 0) {
            // Vrať 12 měsíců pro placeholder
            return Array.from({ length: 12 }).map((_, i) => ({
                name: format(new Date(2023, i, 1), timeGranularity === TimeGranularity.YEAR ? "yyyy" : "yyyy-MM"),
                Předepsané: 0,
                Vydané: 0
            }))
        }

        return data.series.map(item => ({
            name: item.period,
            Předepsané: item.prescribed,
            Vydané: item.dispensed
        }))
    }, [data, timeGranularity])

    const highlightRange = useMemo(() => {
        if (!dateFrom || !dateTo) return null
        const start = format(dateFrom, timeGranularity === TimeGranularity.YEAR ? "yyyy" : "yyyy-MM")
        const end = format(dateTo, timeGranularity === TimeGranularity.YEAR ? "yyyy" : "yyyy-MM")
        return { start, end }
    }, [dateFrom, dateTo, timeGranularity])

    return (
        <Box mt={5}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">
                    Vývoj předepsaných a vydaných léčiv v čase
                </Typography>

                <Box display="flex" gap={2}>
                    <FormControl size="small">
                        <InputLabel>Okres</InputLabel>
                        <Select
                            label="Okres"
                            value={selectedDistrict ?? ""}
                            onChange={(e) =>
                                onDistrictChange(e.target.value === "" ? null : e.target.value)
                            }
                            sx={{ minWidth: 160 }}
                        >
                            <MenuItem value="">Celá ČR</MenuItem>

                            {Object.entries(districtNameMap)
                                .sort(([codeA, nameA], [codeB, nameB]) => {
                                    if (nameA === "Hlavní město Praha") return -1
                                    if (nameB === "Hlavní město Praha") return 1
                                    return nameA.localeCompare(nameB, "cs") // seřazení podle češtiny
                                })
                                .map(([code, name]) => (
                                    <MenuItem key={code} value={code}>
                                        {name}
                                    </MenuItem>
                                ))}
                        </Select>
                    </FormControl>

                    <FormControl size="small">
                        <InputLabel>Granularita</InputLabel>
                        <Select
                            label="Granularita"
                            value={timeGranularity}
                            onChange={(e) => onTimeGranularityChange(e.target.value as TimeGranularity)}
                        >
                            {Object.entries(TimeGranularityLabels).map(([value, label]) => (
                                <MenuItem key={value} value={value}>
                                    {label}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Box>
            </Box>

            <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                    <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend />

                    {highlightRange && (
                        <ReferenceArea
                            x1={highlightRange.start}
                            x2={highlightRange.end}
                            strokeOpacity={0}
                            fill="#1976d2"
                            fillOpacity={0.1}
                        />
                    )}

                    <Line
                        type="monotone"
                        dataKey="Předepsané"
                        stroke="#1976d2"
                        strokeWidth={2}
                        dot={{ r: 2 }}
                        activeDot={{ r: 5 }}
                    />
                    <Line
                        type="monotone"
                        dataKey="Vydané"
                        stroke="#2e7d32"
                        strokeWidth={2}
                        dot={{ r: 2 }}
                        activeDot={{ r: 5 }}
                    />
                </LineChart>
            </ResponsiveContainer>
        </Box>
    )
}
