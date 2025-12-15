import React, { useState } from "react"
import { Box, Button, Paper, Typography, Alert } from "@mui/material"
import { useFilters } from "../components/FilterContext"
import { DistributionFiltersPanel } from "../components/distribution/DistributionFiltersPanel"
import { DrugSelectorModal } from "../components/drug-select-modal/DrugSelectorModal"
import { SelectedMedicinalProductSummary } from "../components/SelectedMedicinalProductSummary"
import { DataStatusFooter } from "../components/DataStatusFooter"
import { SankeyChart } from "../components/distribution/SankeyChart"
import { useDrugCart } from "../components/drug-select-modal/DrugCartContext"
import { format } from "date-fns"
import { useDistributionTimeSeries } from "../hooks/useDistributionTimeSeries"
import { useDistributionSankeyDiagram } from "../hooks/useDistributionSankeyDiagram"
import { MedicinalUnitMode } from "../types/MedicinalUnitMode"
import { TimeGranularity } from "../types/TimeGranularity"
import { DistributionTimeSeriesChart } from "../components/distribution/DistributionTimeSeriesChart"
import { DISTRIBUTION_DATASETS } from "../types/DatasetType"

export function DistributionPage() {
    const { common, setCommon } = useFilters()
    const { drugs, registrationNumbers } = useDrugCart()

    const [isModalOpen, setIsModalOpen] = useState(false)

    const hasSelection = drugs.length > 0 || registrationNumbers.length > 0

    const sankeyQuery = useDistributionSankeyDiagram(
        hasSelection && common.dateFrom && common.dateTo
            ? {
                dateFrom: format(common.dateFrom, "yyyy-MM"),
                dateTo: format(common.dateTo, "yyyy-MM"),
                medicinalProductIds: drugs.map(d => Number(d.id)),
                registrationNumbers: registrationNumbers,
                medicinalUnitMode: common.medicinalUnitMode as MedicinalUnitMode
            }
            : undefined
    )

    const timeSeriesQuery = useDistributionTimeSeries(
        hasSelection && common.dateFrom && common.dateTo
            ? {
                dateFrom: format(common.dateFrom, "yyyy-MM"),
                dateTo: format(common.dateTo, "yyyy-MM"),
                medicinalProductIds: drugs.map(d => Number(d.id)),
                registrationNumbers: registrationNumbers,
                medicinalUnitMode: common.medicinalUnitMode as MedicinalUnitMode,
                timeGranularity: TimeGranularity.MONTH
            }
            : undefined
    )
    const hasIgnored =
        timeSeriesQuery.data &&
        timeSeriesQuery.data.ignoredMedicineProducts.length > 0

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Distribuční tok léčiv
            </Typography>

            <Typography variant="body1" color="text.secondary" mb={3}>
                Sledujte distribuční tok léčiv od držitelů registrace přes distributory až k pacientům.
                Vyberte si léčiva, která vás zajímají, nastavte časové období a vizualizujte cestu léčiv
                napříč jednotlivými články distribučního řetězce.
            </Typography>

            <Box
                sx={{
                    display: "flex",
                    flexDirection: "column",
                    gap: 2,
                    "@media (min-width:1000px)": {
                        flexDirection: "row"
                    }
                }}
            >
                <Box width={{ xs: "100%", md: 300 }} flexShrink={0}>
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
                    <DistributionFiltersPanel
                        dateFrom={common.dateFrom}
                        dateTo={common.dateTo}
                        onChangeDateFrom={(val) => setCommon(prev => ({ ...prev, dateFrom: val }))}
                        onChangeDateTo={(val) => setCommon(prev => ({ ...prev, dateTo: val }))}
                        medicinalUnitMode={common.medicinalUnitMode}
                        onChangeMedicinalUnitMode={(val) =>
                            setCommon(prev => ({ ...prev, medicinalUnitMode: val }))
                        }
                    />

                    {!hasSelection && (
                        <Alert severity="warning" sx={{ mt: 2, mb: 2 }}>
                            Vyberte alespoň jedno léčivo, aby bylo možné zobrazit
                            graf distribučních toků a graf s vývojem v čase.
                        </Alert>
                    )}

                    {hasSelection && hasIgnored && (
                        <Alert severity="warning" sx={{ mt: 2, mb: 2 }}>
                            Pozor! Některá vybraná léčiva nebyla do výpočtu zahrnuta,
                            protože pro ně není definována doporučená denní dávka (DDD).
                        </Alert>
                    )}

                    <Box mt={2}>
                        <Paper variant="outlined" sx={{ p: 2 }}>
                            <Typography variant="h6" fontWeight={600} mb={2}>
                                Distribuční tok vybraných léčiv mezi aktéry (
                                {common.dateFrom && common.dateTo
                                    ? `${format(common.dateFrom, "yyyy-MM")} až ${format(common.dateTo, "yyyy-MM")}`
                                    : "nezvolené období"}
                                )
                            </Typography>

                            {sankeyQuery.isLoading ? (
                                <Typography>Načítám data...</Typography>
                            ) : (
                                <Box sx={{ width: '100%', overflowX: 'auto' }}>
                                    <Box sx={{ minWidth: '600px' }}>
                                        <SankeyChart
                                            nodes={sankeyQuery.data?.nodes ?? []}
                                            links={sankeyQuery.data?.links ?? []}
                                            medicinalUnitMode={
                                                (sankeyQuery.data?.medicinalUnitMode ??
                                                    common.medicinalUnitMode) as MedicinalUnitMode
                                            }
                                            height={300}
                                        />
                                    </Box>
                                </Box>
                            )}
                        </Paper>
                    </Box>

                    <Box mt={6}>
                        <Paper variant="outlined" sx={{ p: 2 }}>
                            <Typography variant="h6" fontWeight={600} mb={2}>
                                Časový vývoj distribučních pohybů
                            </Typography>

                            {timeSeriesQuery.isLoading ? (
                                <Typography>Načítám časovou řadu...</Typography>
                            ) : (
                                <DistributionTimeSeriesChart
                                    data={timeSeriesQuery.data}
                                    medicinalUnitMode={common.medicinalUnitMode as MedicinalUnitMode}
                                    dateFrom={common.dateFrom}
                                    dateTo={common.dateTo}
                                />
                            )}
                        </Paper>
                    </Box>
                </Box>
            </Box>

            <DataStatusFooter datasetTypes={DISTRIBUTION_DATASETS} />

            <DrugSelectorModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />
        </Box>
    )
}
