import React from "react"
import { Box, Typography } from "@mui/material"
import { format } from "date-fns"
import { cs } from "date-fns/locale"
import { useLatestProcessedDataset } from "../hooks/useLatestProcessedDataset"
import { DatasetType } from "../types/DatasetType"

export const DataStatusFooter: React.FC<{ datasetTypes: DatasetType[] }> = ({ datasetTypes }) => {
    const { data, isLoading, isError } = useLatestProcessedDataset(datasetTypes)

    const datasetTypeMessages: Record<DatasetType, string> = {
        [DatasetType.ERECEPT_PRESCRIPTIONS]:
            "Zpracována byla data o předepisování léčiv ze systému eRecept",
        [DatasetType.ERECEPT_DISPENSES]:
            "Zpracována byla data o výdeji léčiv ze systému eRecept",
        [DatasetType.DISTRIBUTIONS_FROM_MAHS]:
            "Zpracována byla hlášení o pohybu léčiv od držitelů registrace",
        [DatasetType.DISTRIBUTIONS_FROM_DISTRIBUTORS]:
            "Zpracována byla hlášení o pohybu léčiv od distributorů",
        [DatasetType.DISTRIBUTIONS_FROM_PHARMACIES]:
            "Zpracována byla hlášení o pohybu léčiv z lékáren"
    }

    if (isLoading) {
        return (
            <Box mt={4} textAlign="center">
                <Typography variant="body2" color="text.secondary">
                    Načítám stav dat…
                </Typography>
            </Box>
        )
    }

    if (isError || !data) {
        return (
            <Box mt={4} textAlign="center">
                <Typography variant="body2" color="error">
                    Nepodařilo se načíst stav dat.
                </Typography>
            </Box>
        )
    }

    const createdAt = new Date(data.createdAt)
    const dataMonth = new Date(data.year, data.month - 1)
    const message = datasetTypeMessages[data.datasetType] ?? "Zpracována byla dostupná data"

    return (
        <Box mt={4} textAlign="center">
            <Typography variant="body2" color="text.secondary">
                Poslední aktualizace dat proběhla{" "}
                {format(createdAt, "d. MMMM yyyy", { locale: cs })}.{" "}
                {message} za{" "}
                {format(dataMonth, "LLLL yyyy", { locale: cs })}.
            </Typography>
        </Box>
    )
}
