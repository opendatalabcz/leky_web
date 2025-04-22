import React, { useState } from "react"
import {
    Box,
    Button,
    Typography,
    Paper
} from "@mui/material"
import { useFilters } from "../components/FilterContext"
import { DistributionFiltersPanel } from "../components/DistributionFiltersPanel"
import { MedicineSelectorModal } from "../components/MedicineSelectorModal"
import { SelectedMedicinalProductSummary } from "../components/SelectedMedicinalProductSummary"
import { DataStatusFooter } from "../components/DataStatusFooter"
import { SankeyChart } from "../components/distribution/SankeyChart"
import { useUnifiedCart } from "../components/UnifiedCartContext"
import { useDistributionSankey } from "../hooks/useDistributionSankey"
import {format} from "date-fns";
import {useDistributionFromDistributorsSankey} from "../hooks/useDistributionFromDistributorsSankey";

export function DistributionPage() {
    const { common, setCommon } = useFilters()
    const [isModalOpen, setIsModalOpen] = useState(false)
    const { drugs } = useUnifiedCart()

    const hasDrugs = drugs.length > 0
    const sankeyQuery = useDistributionSankey(
        hasDrugs && common.dateFrom && common.dateTo
            ? {
                dateFrom: format(common.dateFrom, "yyyy-MM"),
                dateTo: format(common.dateTo, "yyyy-MM"),
                medicinalProductIds: drugs.map(d => Number(d.id))
            }
            : undefined
    )

    const sankeyyQuery = useDistributionFromDistributorsSankey(
        hasDrugs && common.dateFrom && common.dateTo
            ? {
                dateFrom: format(common.dateFrom, "yyyy-MM"),
                dateTo: format(common.dateTo, "yyyy-MM"),
                medicinalProductIds: drugs.map(d => Number(d.id))
            }
            : undefined
    )

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Distribuční tok léčiv
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

                    <Box mt={3}>
                        {
                            sankeyQuery.isLoading ? (
                                <Typography>Načítám data...</Typography>
                            ) : sankeyQuery.data ? (
                                <SankeyChart
                                    nodes={sankeyQuery.data.nodes}
                                    links={sankeyQuery.data.links}
                                    height={500}
                                />
                            ) : (
                                <Typography color="text.secondary">
                                    Vyberte léčiva a časové období.
                                </Typography>
                            )
                        }
                    </Box>
                    <Box mt={6}>
                        <Typography variant="h6" gutterBottom>
                            Distribuční tok od distributorů
                        </Typography>
                        {
                            sankeyyQuery.isLoading ? (
                                <Typography>Načítám data...</Typography>
                            ) : sankeyyQuery.data ? (
                                <SankeyChart
                                    nodes={sankeyyQuery.data.nodes}
                                    links={sankeyyQuery.data.links}
                                    height={500}
                                />
                            ) : (
                                <Typography color="text.secondary">
                                    Vyberte léčiva a časové období.
                                </Typography>
                            )
                        }
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
