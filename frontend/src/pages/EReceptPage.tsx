import React, { useEffect, useState } from "react"
import {
    Box,
    Button,
    Typography,
    Paper,
    IconButton,
    Slider,
    Alert
} from "@mui/material"
import { PlayArrow, Pause } from "@mui/icons-material"
import { format, addMonths, isBefore } from "date-fns"
import { useFilters } from "../components/FilterContext"
import { EreceptFiltersPanel } from "../components/erecept/EreceptFiltersPanel"
import { DrugSelectorModal } from "../components/drug-select-modal/DrugSelectorModal"
import { SelectedMedicinalProductSummary } from "../components/SelectedMedicinalProductSummary"
import { DataStatusFooter } from "../components/DataStatusFooter"
import DistrictMap from "../components/erecept/DistrictMap"
import { FeatureCollection } from "geojson"
import { useDrugCart } from "../components/drug-select-modal/DrugCartContext"
import { SummaryTiles } from "../components/erecept/SummaryTiles"
import { useEreceptAggregateByDistrict } from "../hooks/useEreceptAggregateByDistrict"
import { useEreceptTimeSeriesByDistrict } from "../hooks/useEreceptTimeSeriesByDistrict"
import { useEreceptPrepareAnimationData } from "../hooks/useEreceptPrepareAnimationData"
import { EreceptTimeSeriesChart } from "../components/erecept/EreceptTimeSeriesChart"
import { useEreceptFullTimeSeries } from "../hooks/useEreceptFullTimeSeries"
import { TimeGranularity } from "../types/TimeGranularity"
import { ERECEPT_DATASETS } from "../types/DatasetType"

interface DistrictFeatureProperties {
    nationalCode: string
    name: string
}

type DistrictFeatureCollection = GeoJSON.FeatureCollection

export function EReceptPage() {
    const { common, setCommon, prescriptionDispense, setPrescriptionDispense } = useFilters()
    const { drugs, registrationNumbers } = useDrugCart()

    const [geojsonData, setGeojsonData] = useState<FeatureCollection | null>(null)
    const [districtNamesMap, setDistrictNamesMap] = useState<Record<string, string>>({})

    const [isModalOpen, setIsModalOpen] = useState(false)

    const [isPlaying, setIsPlaying] = useState(false)
    const [sliderActive, setSliderActive] = useState(false)
    const [months, setMonths] = useState<string[]>([])
    const [monthIndex, setMonthIndex] = useState(0)

    const [timeGranularity, setTimeGranularity] = useState<TimeGranularity>(TimeGranularity.MONTH)
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

    const hasSelection = drugs.length > 0 || registrationNumbers.length > 0
    const params = hasSelection ? {
        dateFrom: format(common.dateFrom!, "yyyy-MM"),
        dateTo: format(common.dateTo!, "yyyy-MM"),
        medicinalUnitMode: common.medicinalUnitMode,
        aggregationType: prescriptionDispense.aggregationType,
        normalisationMode: prescriptionDispense.normalisationMode,
        medicinalProductIds: drugs.map(d => Number(d.id)),
        registrationNumbers: registrationNumbers
    } : undefined

    const aggregateQuery = useEreceptAggregateByDistrict(params)
    const seriesQuery = useEreceptTimeSeriesByDistrict(params)

    const {monthly, monthlySummaries} = useEreceptPrepareAnimationData(seriesQuery.data?.series ?? [])

    const currentMonthStr = months[monthIndex]
    const monthValues = monthly.get(currentMonthStr) ?? {}
    const monthSummary = monthlySummaries.get(currentMonthStr)
    const districtValues = sliderActive ? monthValues : (aggregateQuery.data?.districtValues ?? {})
    const summary = sliderActive ? monthSummary : aggregateQuery.data?.summary

    const fullTimeSeriesQuery = useEreceptFullTimeSeries(
        hasSelection ? {
            medicinalUnitMode: common.medicinalUnitMode,
            normalisationMode: prescriptionDispense.normalisationMode,
            medicinalProductIds: drugs.map(d => Number(d.id)),
            registrationNumbers: registrationNumbers,
            timeGranularity: timeGranularity,
            district: selectedDistrict
        } : undefined
    )

    const hasIgnored =
        fullTimeSeriesQuery.data &&
        fullTimeSeriesQuery.data.ignoredMedicineProducts.length > 0

    useEffect(() => {
        if (!seriesQuery.data?.series?.length) return
        const all = new Set<string>()
        seriesQuery.data.series.forEach(entry => {
            Object.keys(entry.districtValues).forEach(code => all.add(code))
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
                Sledujte, kolik léčiv se v České republice předepisuje a vydává,
                a to na základě dat ze systému eRecept. Vyberte léčiva, která vás zajímají,
                nastavte časové období a způsob zobrazení – výsledky se promítnou do mapy okresů.
            </Typography>

            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 2,
                    '@media (min-width:1000px)': {
                        flexDirection: 'row'
                    }
                }}
            >
                <Box width={{ xs: '100%', md: 300 }} flexShrink={0}>
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
                                "&:hover": { backgroundColor: "#2c4773" }
                            }}
                        >
                            Vybrat léčiva
                        </Button>

                        <SelectedMedicinalProductSummary />
                    </Paper>
                </Box>

                <Box flex={1} minWidth={0}>
                    <EreceptFiltersPanel
                        dateFrom={common.dateFrom}
                        dateTo={common.dateTo}
                        onChangeDateFrom={(date) => setCommon({ ...common, dateFrom: date })}
                        onChangeDateTo={(date) => setCommon({ ...common, dateTo: date })}
                        medicinalUnitMode={common.medicinalUnitMode}
                        onChangeMedicinalUnitMode={(mode) => setCommon({ ...common, medicinalUnitMode: mode })}
                        normalisationMode={prescriptionDispense.normalisationMode}
                        onChangeNormalisationMode={(nm) =>
                            setPrescriptionDispense({ ...prescriptionDispense, normalisationMode: nm })
                        }
                        aggregationType={prescriptionDispense.aggregationType}
                        onChangeAggregationType={(val) =>
                            setPrescriptionDispense({ ...prescriptionDispense, aggregationType: val })
                        }
                    />

                    {!hasSelection && (
                        <Alert severity="warning" sx={{ mt: 2, mb: 2 }}>
                            Vyberte alespoň jedno léčivo, aby bylo možné zobrazit data
                            v mapě okresů a graf s vývojem v čase.
                        </Alert>
                    )}

                    {hasSelection && hasIgnored && (
                        <Alert severity="warning" sx={{ mt: 2, mb: 2 }}>
                            Pozor! Některá vybraná léčiva nebyla do výpočtu zahrnuta,
                            protože pro ně není definována doporučená denní dávka (DDD).
                        </Alert>
                    )}

                    {months.length > 1 && (
                        <Box
                            mt={3}
                            display="flex"
                            alignItems="center"
                            gap={2}
                            flexDirection={{ xs: "column", sm: "row" }}
                        >
                            <IconButton
                                onClick={() => {
                                    setIsPlaying(p => !p)
                                    setSliderActive(true)
                                }}
                            >
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
                                sx={{ flex: 1, width: '100%' }}
                            />

                            <Button
                                size="small"
                                onClick={() => setSliderActive(false)}
                            >
                                ↺ Celé období
                            </Button>
                        </Box>
                    )}

                    <Box mt={2}>
                        <Paper variant="outlined" sx={{ p: 2 }}>
                            <Typography variant="h6" fontWeight={600} mb={2}>
                                Předepisování a výdej vybraných léčiv (
                                {sliderActive
                                    ? months[monthIndex]
                                    : `${format(common.dateFrom!, "yyyy-MM")} až ${format(
                                        common.dateTo!,
                                        "yyyy-MM"
                                    )}`}
                                )
                            </Typography>

                            <Box display="flex" flexWrap="wrap" gap={2}>
                                <Box
                                    sx={{
                                        flex: '1 1 700px',
                                        minWidth: 300,
                                        maxWidth: '100%',
                                        height: { xs: 300, sm: 420 },
                                        boxSizing: 'border-box'
                                    }}
                                >
                                    {geojsonData && (
                                        <DistrictMap
                                            geojsonData={geojsonData}
                                            districtData={districtValues}
                                            filter={prescriptionDispense.aggregationType}
                                            medicinalUnitMode={common.medicinalUnitMode}
                                        />
                                    )}
                                </Box>

                                <Box
                                    sx={{
                                        flex: { xs: '1 1 100%', md: '0 0 300px' },
                                        width: { xs: '100%', md: '300px' },
                                        boxSizing: 'border-box'
                                    }}
                                >
                                    <SummaryTiles summary={summary} />
                                </Box>
                            </Box>
                        </Paper>
                    </Box>

                    <Box mt={6}>
                        <Paper variant="outlined" sx={{ p: 2 }}>
                            <Typography variant="h6" fontWeight={600} mb={2}>
                                Vývoj předepisování a výdeje v čase
                            </Typography>

                            {fullTimeSeriesQuery.isLoading ? (
                                <Typography>Načítám časovou řadu...</Typography>
                            ) : (
                                <EreceptTimeSeriesChart
                                    data={fullTimeSeriesQuery.data}
                                    selectedDistrict={selectedDistrict}
                                    onDistrictChange={setSelectedDistrict}
                                    districtNameMap={districtNamesMap}
                                    timeGranularity={timeGranularity}
                                    onTimeGranularityChange={setTimeGranularity}
                                    dateFrom={common.dateFrom}
                                    dateTo={common.dateTo}
                                />
                            )}
                        </Paper>
                    </Box>
                </Box>
            </Box>

            <DataStatusFooter datasetTypes={ERECEPT_DATASETS} />

            <DrugSelectorModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />
        </Box>
    )
}
