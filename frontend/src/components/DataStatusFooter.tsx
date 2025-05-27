import React from "react"
import { Box, Typography } from "@mui/material"
import { format } from "date-fns"
import { cs } from "date-fns/locale"
import { useLatestProcessedDataset } from "../hooks/useLatestProcessedDataset"
import {DatasetType} from "../types/DatasetType";

export const DataStatusFooter: React.FC<{ datasetTypes: DatasetType[] }> = ({ datasetTypes }) => {
    const { data, isLoading, isError } = useLatestProcessedDataset(datasetTypes)

    const datasetTypeLabels: Record<DatasetType, string> = {
        [DatasetType.ERECEPT_PRESCRIPTIONS]: "o předpisech léčiv ze systému eRecept",
        [DatasetType.ERECEPT_DISPENSES]: "o výdejích léčiv ze systému eRecept",
        [DatasetType.DISTRIBUTIONS_FROM_MAHS]: "z hlášení držitelů registrace",
        [DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS]: "z hlášení distributorů",
        [DatasetType.DISTRIBUTIONS_FROM_PHARMACIES]: "z hlášení lékáren"
    }

    if (isLoading) {
        return (
            <Box mt={4} textAlign="center">
                <Typography variant="body2" color="text.secondary">Načítám stav dat...</Typography>
            </Box>
        )
    }

    if (isError || !data) {
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
