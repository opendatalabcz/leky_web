import React from "react"
import { Box, Typography } from "@mui/material"
import { format } from "date-fns"
import { cs } from "date-fns/locale"
import { useProcessedDataset } from "../hooks/useProcessedDataset"

export const DataStatusFooter: React.FC<{ datasetTypes: string[] }> = ({ datasetTypes }) => {
    const { data, loading, error } = useProcessedDataset(datasetTypes)

    const datasetTypeLabels: Record<string, string> = {
        "ERECEPT_PRESCRIPTIONS": "o předpisech léčiv ze systému eRecept",
        "ERECEPT_DISPENSES": "o výdejích léčiv ze systému eRecept",
        "DISTRIBUTIONS_FROM_MAHS": "z hlášení držitelů registrace",
        "DISTRIBUTIONS_FROM_DISTRIBUTORS": "z hlášení distributorů",
        "DISTRIBUTIONS_FROM_PHARMACIES": "z hlášení lékáren"
    }


    if (loading) {
        return (
            <Box mt={4} textAlign="center">
                <Typography variant="body2" color="text.secondary">Načítám stav dat...</Typography>
            </Box>
        )
    }

    if (error || !data) {
        return (
            <Box mt={4} textAlign="center">
                <Typography variant="body2" color="error">Nepodařilo se načíst stav dat.</Typography>
            </Box>
        )
    }

    const createdAt = new Date(data.createdAt)
    const dataMonth = new Date(data.year, data.month - 1)
    const datasetTypeLabel = datasetTypeLabels[data.datasetType] ?? data.datasetType

    return (
        <Box mt={4} textAlign="center">
            <Typography variant="body2" color="text.secondary">
                Poslední aktualizace dat proběhla {format(createdAt, "d. MMMM yyyy", { locale: cs })}
                {" "}stažením dat {datasetTypeLabel} za {format(dataMonth, "LLLL yyyy", { locale: cs })}.
            </Typography>
        </Box>
    )
}
