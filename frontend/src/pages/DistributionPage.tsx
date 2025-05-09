import React, {useState} from "react"
import {Box, Button, Paper, Typography} from "@mui/material"
import {useFilters} from "../components/FilterContext"
import {DistributionFiltersPanel} from "../components/DistributionFiltersPanel"
import {DrugSelectorModal} from "../components/drug-select-modal/DrugSelectorModal"
import {SelectedMedicinalProductSummary} from "../components/SelectedMedicinalProductSummary"
import {DataStatusFooter} from "../components/DataStatusFooter"
import {SankeyChart} from "../components/distribution/SankeyChart"
import {useDrugCart} from "../components/drug-select-modal/DrugCartContext"
import {format} from "date-fns"
import {useDistributionTimeSeries} from "../hooks/useDistributionTimeSeries"
import {useDistributionSankeyDiagram} from "../hooks/useDistributionSankeyDiagram"
import {MedicinalUnitMode} from "../types/MedicinalUnitMode"
import {TimeGranularity} from "../types/TimeGranularity";
import {DistributionTimeSeriesChart} from "../components/DistributionTimeSeriesChart";

export function DistributionPage() {
    const { common, setCommon } = useFilters()
    const [isModalOpen, setIsModalOpen] = useState(false)
    const { drugs, registrationNumbers } = useDrugCart()

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

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Distribuční tok léčiv
            </Typography>
            <Typography variant="body1" color="text.secondary" mb={3}>
                Sledujte distribuční tok léčiv od držitelů registrace přes distributory až k pacientům.
                Vyberte si léčiva, která vás zajímají, nastavte časové období a vizualizujte cestu léčiv napříč jednotlivými články distribučního řetězce.
            </Typography>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, '@media (min-width:1000px)': { flexDirection: 'row' } }}>
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
                        medicinalUnitMode={common.medicinalUnitMode}
                        onChangeMedicinalUnitMode={(val) =>
                            setCommon(prev => ({ ...prev, medicinalUnitMode: val }))
                        }
                    />

                    <Box mt={6}>
                        {sankeyQuery.isLoading ? (
                            <Typography>Načítám data...</Typography>
                        ) : sankeyQuery.data ? (
                            <>
                                <Paper variant="outlined" sx={{ p: 2, mb: 4 }}>
                                    <Typography variant="h6" fontWeight={600} mb={2}>
                                        Distribuční tok vybraných léčiv mezi aktéry ({format(common.dateFrom!, "yyyy-MM")} až {format(common.dateTo!, "yyyy-MM")})
                                    </Typography>

                                    <Box sx={{ width: '100%', overflowX: 'auto' }}>
                                        <Box sx={{ minWidth: '600px' }}>
                                            <SankeyChart
                                                nodes={sankeyQuery.data.nodes}
                                                links={sankeyQuery.data.links}
                                                medicinalUnitMode={sankeyQuery.data.medicinalUnitMode as MedicinalUnitMode}
                                                height={300}
                                            />
                                        </Box>
                                    </Box>
                                </Paper>

                                <Paper variant="outlined" sx={{ p: 2 }}>
                                    <Typography variant="h6" fontWeight={600} mb={2}>
                                        Časový vývoj distribučních pohybů
                                    </Typography>

                                    <Box sx={{ width: '100%', overflowX: 'auto' }}>
                                        <Box sx={{ minWidth: '600px' }}>
                                            <DistributionTimeSeriesChart
                                                data={timeSeriesQuery.data}
                                                medicinalUnitMode={timeSeriesQuery.data?.medicinalUnitMode as MedicinalUnitMode}
                                            />
                                        </Box>
                                    </Box>
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
