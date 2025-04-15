import React, { useEffect, useState } from "react"
import { Box, Button, Typography, Paper } from "@mui/material"
import { useFilters } from "../components/FilterContext"
import { EReceptFiltersPanel } from "../components/EReceptFiltersPanel"
import { MedicineSelectorModal } from "../components/MedicineSelectorModal"
import { SelectedMedicinalProductSummary } from "../components/SelectedMedicinalProductSummary"
import { DataStatusFooter } from "../components/DataStatusFooter"
import DistrictMap from "../components/DistrictMap"
import { FeatureCollection } from "geojson"
import { useUnifiedCart } from "../components/UnifiedCartContext"
import { getEReceptDistrictData } from "../services/ereceptService"
import { format } from "date-fns"
import {SummaryTiles} from "../components/SummaryTiles";

export function EReceptPage() {
    const { common, setCommon, prescriptionDispense, setPrescriptionDispense } = useFilters()
    const { drugs, groupedDrugs } = useUnifiedCart()
    const [isModalOpen, setIsModalOpen] = useState(false)

    const [geojsonData, setGeojsonData] = useState<FeatureCollection | null>(null)
    const [districtData, setDistrictData] = useState<Record<string, number>>({})

    useEffect(() => {
        fetch("/okresy.json")
            .then(res => res.json())
            .then(setGeojsonData)
            .catch(err => console.error("GeoJSON load error", err))
    }, [])

    useEffect(() => {
        if (!common.dateFrom || !common.dateTo) return

        const payload = {
            dateFrom: format(common.dateFrom, "yyyy-MM"),
            dateTo: format(common.dateTo, "yyyy-MM"),
            calculationMode: common.calculationMode,
            aggregationType: prescriptionDispense.aggregationType,
            normalisationMode: prescriptionDispense.normalisationMode,
            medicinalProductIds: [
                ...drugs.map(d => Number(d.id)),
            ]
        }

        getEReceptDistrictData(payload)
            .then(response => {
                setDistrictData(response.districtValues)
            })
            .catch(err => console.error("Chyba při načítání dat z API:", err))
    }, [common, prescriptionDispense, drugs, groupedDrugs])

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

            <Box display="flex" gap={4} alignItems="flex-start">
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
                        onChangeCalculationMode={(mode) =>
                            setCommon({ ...common, calculationMode: mode })
                        }
                        normalisationMode={prescriptionDispense.normalisationMode}
                        onChangeNormalisationMode={(nm) =>
                            setPrescriptionDispense({
                                ...prescriptionDispense,
                                normalisationMode: nm
                            })
                        }
                        aggregationType={prescriptionDispense.aggregationType}
                        onChangeAggregationType={(val) =>
                            setPrescriptionDispense({
                                ...prescriptionDispense,
                                aggregationType: val
                            })
                        }
                    />

                    <Box mt={2} display="flex" gap={2}>
                        <Box flex={1} height={500}>
                            {geojsonData ? (
                                <DistrictMap
                                    geojsonData={geojsonData}
                                    districtData={districtData}
                                    filter={prescriptionDispense.aggregationType}
                                />
                            ) : (
                                <Box
                                    height="100%"
                                    display="flex"
                                    alignItems="center"
                                    justifyContent="center"
                                    border="1px dashed #ccc"
                                    borderRadius={2}
                                >
                                    <Typography variant="body2" color="text.secondary">
                                        Načítání mapy...
                                    </Typography>
                                </Box>
                            )}
                        </Box>

                        <Box width={180}>
                            <SummaryTiles />
                        </Box>
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
