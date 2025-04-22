import React, { useEffect, useState } from "react"
import {
    Box, Button, Typography, Paper, IconButton, Slider
} from "@mui/material"
import { PlayArrow, Pause } from "@mui/icons-material"
import { format, addMonths, isBefore } from "date-fns"
import { useFilters } from "../components/FilterContext"
import { EReceptFiltersPanel } from "../components/EReceptFiltersPanel"
import { MedicineSelectorModal } from "../components/MedicineSelectorModal"
import { SelectedMedicinalProductSummary } from "../components/SelectedMedicinalProductSummary"
import { DataStatusFooter } from "../components/DataStatusFooter"
import DistrictMap from "../components/DistrictMap"
import { FeatureCollection } from "geojson"
import { useUnifiedCart } from "../components/UnifiedCartContext"
import { SummaryTiles } from "../components/SummaryTiles"
import { useDistrictAggregate } from "../hooks/useDistrictAggregate"
import { useDistrictTimeSeries } from "../hooks/useDistrictTimeSeries"
import { usePreparedDistrictData } from "../hooks/usePreparedDistrictData"
import { PrescriptionDispenseChart } from "../components/PrescriptionDispenseChart"
import { useFullTimeSeries } from "../hooks/useFullTimeSeries"
import { TimeGranularity } from "../types/TimeGranularity"

interface DistrictFeatureProperties {
    nationalCode: string
    name: string
}

interface DistrictFeature extends GeoJSON.Feature {
    properties: DistrictFeatureProperties
}

type DistrictFeatureCollection = GeoJSON.FeatureCollection


export function EReceptPage() {
    const { common, setCommon, prescriptionDispense, setPrescriptionDispense } = useFilters()
    const { drugs } = useUnifiedCart()

    const [geojsonData, setGeojsonData] = useState<FeatureCollection | null>(null)
    const [districtNamesMap, setDistrictNamesMap] = useState<Record<string, string>>({})

    const [isModalOpen, setIsModalOpen] = useState(false)

    const [isPlaying, setIsPlaying] = useState(false)
    const [sliderActive, setSliderActive] = useState(false)
    const [months, setMonths] = useState<string[]>([])
    const [monthIndex, setMonthIndex] = useState(0)

    const [granularity, setGranularity] = useState<TimeGranularity>(TimeGranularity.MONTH)
    const [selectedDistrict, setSelectedDistrict] = useState<string | null>(null)
    const [availableDistrictCodes, setAvailableDistrictCodes] = useState<string[]>([])

    useEffect(() => {
        fetch("/okresy.json")
            .then(res => res.json())
            .then((geo: DistrictFeatureCollection) => {
                setGeojsonData(geo)

                const nameMap: Record<string, string> = {}
                geo.features.forEach((feature: any) => {
                    const code = feature.nationalCode || feature.properties?.nationalCode
                    const name = feature.name || feature.properties?.name
                    if (code && name) {
                        nameMap[code] = name
                    }
                })

                setDistrictNamesMap(nameMap)
            })
    }, [])

    useEffect(() => {
        if (!common.dateFrom || !common.dateTo) return
        const tmp: string[] = []
        let cur = common.dateFrom
        while (!isBefore(common.dateTo, cur)) {
            tmp.push(format(cur, "yyyy-MM"))
            cur = addMonths(cur, 1)
        }
        setMonths(tmp)
        setMonthIndex(0)
    }, [common.dateFrom, common.dateTo])

    const hasDrugs = drugs.length > 0
    const params = hasDrugs ? {
        dateFrom: format(common.dateFrom!, "yyyy-MM"),
        dateTo: format(common.dateTo!, "yyyy-MM"),
        calculationMode: common.calculationMode,
        aggregationType: prescriptionDispense.aggregationType,
        normalisationMode: prescriptionDispense.normalisationMode,
        medicinalProductIds: drugs.map(d => Number(d.id))
    } : undefined

    const aggregateQuery = useDistrictAggregate(params)
    const seriesQuery = useDistrictTimeSeries(params, !!aggregateQuery.data)

    const {
        monthly,
        aggregated,
        monthlySummaries,
        aggregatedSummary
    } = usePreparedDistrictData(seriesQuery.data?.series ?? [])

    const currentMonthStr = months[monthIndex]
    const monthValues = monthly.get(currentMonthStr) ?? {}
    const monthSummary = monthlySummaries.get(currentMonthStr)
    const districtValues = sliderActive ? monthValues : (aggregateQuery.data?.districtValues ?? {})
    const summary = sliderActive ? monthSummary : aggregateQuery.data?.summary

    // Full time series for chart
    const fullTimeSeriesQuery = useFullTimeSeries(
        hasDrugs ? {
            aggregationType: prescriptionDispense.aggregationType,
            calculationMode: common.calculationMode,
            normalisationMode: prescriptionDispense.normalisationMode,
            medicinalProductIds: drugs.map(d => Number(d.id)),
            granularity: granularity,
            district: selectedDistrict
        } : undefined
    )

    // Collect all district codes from series for dropdown
    useEffect(() => {
        if (!seriesQuery.data?.series?.length) return
        const all = new Set<string>()
        seriesQuery.data.series.forEach(entry => {
            Object.keys(entry.values).forEach(code => all.add(code))
        })
        setAvailableDistrictCodes(Array.from(all).sort())
    }, [seriesQuery.data])

    useEffect(() => {
        if (!isPlaying) return
        const int = setInterval(() => {
            setMonthIndex(prev => {
                const next = prev + 1
                if (next >= months.length) {
                    setIsPlaying(false)
                    return prev
                }
                return next
            })
        }, 1000)
        return () => clearInterval(int)
    }, [isPlaying, months])

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Předepisování a výdej léčiv
            </Typography>

            <Typography variant="body1" color="text.secondary" mb={3}>
                Zjistěte, kolik léčiv se v České republice předepisuje a vydává,
                a to na základě dat ze systému eRecept. Vyberte léčiva, která vás zajímají,
                nastavte časové období a způsob zobrazení – výsledky se promítnou do mapy okresů.
            </Typography>

            <Box display="flex" gap={2} alignItems="flex-start">
                <Box width={300} flexShrink={0}>
                    <Paper variant="outlined" sx={{ p: 2 }}>
                        <Button
                            variant="contained"
                            fullWidth
                            onClick={() => setIsModalOpen(true)}
                            sx={{
                                mb: 2,
                                backgroundColor: "#34558a",
                                textTransform: "none",
                                fontWeight: 600,
                                "&:hover": {
                                    backgroundColor: "#2c4773"
                                }
                            }}
                        >
                            Vybrat léčiva
                        </Button>

                        <SelectedMedicinalProductSummary />
                    </Paper>
                </Box>

                <Box flex={1} minWidth={0}>
                    <EReceptFiltersPanel
                        dateFrom={common.dateFrom}
                        dateTo={common.dateTo}
                        onChangeDateFrom={(date) => setCommon({ ...common, dateFrom: date })}
                        onChangeDateTo={(date) => setCommon({ ...common, dateTo: date })}
                        calculationMode={common.calculationMode}
                        onChangeCalculationMode={(mode) => setCommon({ ...common, calculationMode: mode })}
                        normalisationMode={prescriptionDispense.normalisationMode}
                        onChangeNormalisationMode={(nm) =>
                            setPrescriptionDispense({ ...prescriptionDispense, normalisationMode: nm })
                        }
                        aggregationType={prescriptionDispense.aggregationType}
                        onChangeAggregationType={(val) =>
                            setPrescriptionDispense({ ...prescriptionDispense, aggregationType: val })
                        }
                    />

                    {months.length > 1 && (
                        <Box mt={3} display="flex" alignItems="center" gap={2}>
                            <IconButton onClick={() => { setIsPlaying(p => !p); setSliderActive(true) }}>
                                {isPlaying ? <Pause /> : <PlayArrow />}
                            </IconButton>
                            <Slider
                                min={0}
                                max={months.length - 1}
                                value={monthIndex}
                                onChange={(_, v) => {
                                    setMonthIndex(v as number)
                                    setSliderActive(true)
                                }}
                                valueLabelDisplay="on"
                                valueLabelFormat={(i) => months[i]}
                                sx={{ flex: 1 }}
                            />
                            <Button size="small" onClick={() => setSliderActive(false)}>↺ Celé období</Button>
                        </Box>
                    )}

                    <Box mt={2} display="flex" gap={2}>
                        <Box flex={1} height={500}>
                            {geojsonData && (
                                <DistrictMap
                                    geojsonData={geojsonData}
                                    districtData={districtValues}
                                    filter={prescriptionDispense.aggregationType}
                                />
                            )}
                        </Box>
                        <SummaryTiles summary={summary} />
                    </Box>
                    <Box mt={6}>
                        {fullTimeSeriesQuery.isLoading ? (
                            <Typography mt={4}>Načítám časovou řadu...</Typography>
                        ) : (
                            <PrescriptionDispenseChart
                                data={fullTimeSeriesQuery.data}
                                selectedDistrict={selectedDistrict}
                                onDistrictChange={setSelectedDistrict}
                                districtNameMap={districtNamesMap}
                                granularity={granularity}
                                onGranularityChange={setGranularity}
                                dateFrom={common.dateFrom}
                                dateTo={common.dateTo}
                            />
                        )}
                    </Box>
                </Box>
            </Box>

            <DataStatusFooter />

            <MedicineSelectorModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />
        </Box>
    )
}
