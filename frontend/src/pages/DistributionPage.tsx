import React, { useState } from "react"
import {
    Box,
    Button,
    Typography,
    Paper
} from "@mui/material"
import { useFilters } from "../components/FilterContext"
import { DistributionFiltersPanel } from "../components/DistributionFiltersPanel"
import { DrugSelectorModal } from "../components/drug-select-modal/DrugSelectorModal"
import { SelectedMedicinalProductSummary } from "../components/SelectedMedicinalProductSummary"
import { DataStatusFooter } from "../components/DataStatusFooter"
import { SankeyChart } from "../components/distribution/SankeyChart"
import { useDrugCart } from "../components/drug-select-modal/DrugCartContext"
import { format } from "date-fns"
import { useCombinedDistributionSankey } from "../hooks/useCombinedDistributionSankey"
import { useDistributionTimeSeries } from "../hooks/useDistributionTimeSeries"
import { DistributionTimeSeriesChart } from "../components/DistributionTimeSeriesChart"

export function DistributionPage() {
    const { common, setCommon } = useFilters()
    const [isModalOpen, setIsModalOpen] = useState(false)
    const { drugs, registrationNumbers } = useDrugCart()

    const hasSelection = drugs.length > 0 || registrationNumbers.length > 0

    const sankeyQuery = useCombinedDistributionSankey(
        hasSelection && common.dateFrom && common.dateTo
            ? {
                dateFrom: format(common.dateFrom, "yyyy-MM"),
                dateTo: format(common.dateTo, "yyyy-MM"),
                medicinalProductIds: drugs.map(d => Number(d.id)),
                registrationNumbers: registrationNumbers
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
                granularity: "MONTH"
            }
            : undefined
    )

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Distribuční tok léčiv
            </Typography>
            <Typography variant="body1" color="text.secondary" mb={3}>
                Sledujte distribuční tok léčiv od držitelů registrace přes distributory až k pacientům.
                Vyberte si konkrétní léčiva, nastavte časové období a vizualizujte cestu léčiv napříč jednotlivými články distribučního řetězce.
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
                    <DistributionFiltersPanel
                        dateFrom={common.dateFrom}
                        dateTo={common.dateTo}
                        onChangeDateFrom={(val) => setCommon(prev => ({ ...prev, dateFrom: val }))}
                        onChangeDateTo={(val) => setCommon(prev => ({ ...prev, dateTo: val }))}
                        calculationMode={common.calculationMode}
                        onChangeCalculationMode={(val) =>
                            setCommon(prev => ({ ...prev, calculationMode: val }))
                        }
                    />

                    <Box mt={6}>
                        {sankeyQuery.isLoading ? (
                            <Typography>Načítám data...</Typography>
                        ) : sankeyQuery.data ? (
                            <>
                                <Paper variant="outlined" sx={{ p: 2, mb: 4 }}>
                                    <Box display="flex" justifyContent="space-between" alignItems="baseline" flexWrap="wrap" gap={1}>
                                        <Typography variant="h6" fontWeight={600}>
                                            Distribuční tok vybraných léčiv mezi aktéry ({format(common.dateFrom!, "yyyy-MM")} až {format(common.dateTo!, "yyyy-MM")})
                                        </Typography>
                                    </Box>

                                    <SankeyChart
                                        nodes={sankeyQuery.data.nodes}
                                        links={sankeyQuery.data.links}
                                        height={500}
                                    />
                                </Paper>

                                <Paper variant="outlined" sx={{ p: 2 }}>
                                    <Typography variant="h6" fontWeight={600} mb={2}>
                                        Časový vývoj distribučních pohybů
                                    </Typography>

                                    <DistributionTimeSeriesChart data={timeSeriesQuery.data} />
                                </Paper>
                            </>
                        ) : (
                            <Typography color="text.secondary">
                                Vyberte léčiva a časové období.
                            </Typography>
                        )}
                    </Box>

                </Box>
            </Box>

            <DataStatusFooter datasetTypes={[
                "DISTRIBUTIONS_FROM_MAHS",
                "DISTRIBUTIONS_FROM_DISTRIBUTORS",
                "DISTRIBUTIONS_FROM_PHARMACIES"
            ]} />

            <DrugSelectorModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />
        </Box>
    )
}
