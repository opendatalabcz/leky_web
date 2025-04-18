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

export function DistributionPage() {
    const { common, setCommon } = useFilters()
    const [isModalOpen, setIsModalOpen] = useState(false)

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

                    <Box
                        mt={2}
                        height={500}
                        display="flex"
                        alignItems="center"
                        justifyContent="center"
                        border="1px dashed #ccc"
                        borderRadius={2}
                    >
                        <Typography variant="body2" color="text.secondary">
                            [Zde bude vizualizace distribučního toku]
                        </Typography>
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
