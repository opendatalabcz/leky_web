import React, { useState } from "react"
import { Box, Button, Typography, Paper } from "@mui/material"
import { useFilters } from "../components/FilterContext"
import { EReceptFiltersPanel } from "../components/EReceptFiltersPanel"
import { MedicineSelectorModal } from "../components/MedicineSelectorModal"
import { SelectedMedicinalProductSummary } from "../components/SelectedMedicinalProductSummary"
import {DataStatusFooter} from "../components/DataStatusFooter";
import MedicalServicesIcon from "@mui/icons-material/MedicalServices"

export function EReceptPage() {
    const { common, setCommon, prescriptionDispense, setPrescriptionDispense } = useFilters()
    const [isModalOpen, setIsModalOpen] = useState(false)

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Předepisování a výdej
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
                            [Zde bude mapa nebo vizualizace]
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
