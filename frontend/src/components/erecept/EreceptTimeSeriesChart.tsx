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
import { format, eachMonthOfInterval, eachYearOfInterval } from "date-fns"
import { EreceptFullTimeSeriesResponse } from "../../services/ereceptService"
import { TimeGranularity, TimeGranularityLabels } from "../../types/TimeGranularity"
import { MedicinalUnitMode, MedicinalUnitModeUnits } from "../../types/MedicinalUnitMode"

type Props = {
    data: EreceptFullTimeSeriesResponse | undefined
    selectedDistrict: string | null
    onDistrictChange: (value: string | null) => void
    districtNameMap: Record<string, string>
    timeGranularity: TimeGranularity
    onTimeGranularityChange: (value: TimeGranularity) => void
    dateFrom?: Date | null
    dateTo?: Date | null
}

export const EreceptTimeSeriesChart: React.FC<Props> = ({
                                                               data,
                                                               selectedDistrict,
                                                               onDistrictChange,
                                                               districtNameMap,
                                                               timeGranularity,
                                                               onTimeGranularityChange,
                                                               dateFrom,
                                                               dateTo
                                                           }) => {
    const { chartData, isPlaceholder } = useMemo(() => {
        if (!data || data.series.length === 0) {
            if (!dateFrom || !dateTo) {
                return {
                    chartData: [],
                    isPlaceholder: true
                }
            }

            const intervals =
                timeGranularity === TimeGranularity.YEAR
                    ? eachYearOfInterval({ start: dateFrom, end: dateTo })
                    : eachMonthOfInterval({ start: dateFrom, end: dateTo })

            return {
                chartData: intervals.map(d => ({
                    name: format(
                        d,
                        timeGranularity === TimeGranularity.YEAR
                            ? "yyyy"
                            : "yyyy-MM"
                    ),
                    Předepsané: 0,
                    Vydané: 0
                })),
                isPlaceholder: true
            }
        }

        return {
            chartData: data.series.map(item => ({
                name: item.period,
                Předepsané: item.prescribed,
                Vydané: item.dispensed
            })),
            isPlaceholder: false
        }
    }, [data, dateFrom, dateTo, timeGranularity])

    const highlightRange = useMemo(() => {
        if (!dateFrom || !dateTo) return null
        return {
            start: format(
                dateFrom,
                timeGranularity === TimeGranularity.YEAR
                    ? "yyyy"
                    : "yyyy-MM"
            ),
            end: format(
                dateTo,
                timeGranularity === TimeGranularity.YEAR
                    ? "yyyy"
                    : "yyyy-MM"
            )
        }
    }, [dateFrom, dateTo, timeGranularity])

    const unitLabel = data ? MedicinalUnitModeUnits[data.medicinalUnitMode as MedicinalUnitMode] : ""

    return (
        <Box mt={5}>
            <Box
                display="flex"
                flexDirection={{ xs: 'column', sm: 'row' }}
                justifyContent="space-between"
                alignItems={{ xs: 'stretch', sm: 'center' }}
                mb={2}
                gap={2}
            >
                <Typography variant="h6"></Typography>

                <Box
                    display="flex"
                    flexWrap="wrap"
                    gap={2}
                    sx={{
                        width: { xs: '100%', sm: 'auto' },
                        maxWidth: { sm: '500px' },
                        justifyContent: { sm: 'flex-end' }
                    }}
                >
                    <FormControl
                        size="small"
                        sx={{
                            flex: 1,
                            minWidth: { xs: '100%', sm: 160 }
                        }}
                    >
                        <InputLabel>Okres</InputLabel>
                        <Select
                            label="Okres"
                            value={selectedDistrict ?? ""}
                            onChange={(e) =>
                                onDistrictChange(e.target.value === "" ? null : e.target.value)
                            }
                        >
                            <MenuItem value="">Celá ČR</MenuItem>
                            {Object.entries(districtNameMap)
                                .sort(([codeA, nameA], [codeB, nameB]) => {
                                    if (nameA === "Hlavní město Praha") return -1
                                    if (nameB === "Hlavní město Praha") return 1
                                    return nameA.localeCompare(nameB, "cs")
                                })
                                .map(([code, name]) => (
                                    <MenuItem key={code} value={code}>
                                        {name}
                                    </MenuItem>
                                ))}
                        </Select>
                    </FormControl>

                    <FormControl
                        size="small"
                        sx={{
                            flex: 1,
                            minWidth: { xs: '100%', sm: 160 }
                        }}
                    >
                        <InputLabel>Granularita</InputLabel>
                        <Select
                            label="Granularita"
                            value={timeGranularity}
                            onChange={(e) =>
                                onTimeGranularityChange(
                                    e.target.value as TimeGranularity
                                )
                            }
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

                            <Line
                                type="monotone"
                                dataKey="Předepsané"
                                stroke="#1976d2"
                                strokeWidth={2}
                                dot={!isPlaceholder}
                                activeDot={{ r: 5 }}
                                isAnimationActive={!isPlaceholder}
                            />
                            <Line
                                type="monotone"
                                dataKey="Vydané"
                                stroke="#2e7d32"
                                strokeWidth={2}
                                dot={!isPlaceholder}
                                activeDot={{ r: 5 }}
                                isAnimationActive={!isPlaceholder}
                            />
                        </LineChart>
                    </ResponsiveContainer>
                </Box>
            </Box>
        </Box>
    )
}