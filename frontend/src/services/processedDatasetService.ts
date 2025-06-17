// services/processedDatasetService.ts

import { DatasetType } from "../types/DatasetType"

export type ProcessedDatasetResponse = {
    datasetType: DatasetType
    createdAt: string
    year: number
    month: number
}

export async function fetchLatestProcessedDataset(datasetTypes: DatasetType[]): Promise<ProcessedDatasetResponse> {
    const query = datasetTypes.map(t => `types=${t}`).join("&")
    const response = await fetch(`/api/processed-datasets/latest?${query}`)
    if (!response.ok) {
        throw new Error("Failed to fetch latest dataset status")
    }
    return response.json()
}
