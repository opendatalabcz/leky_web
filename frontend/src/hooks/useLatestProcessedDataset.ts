import { useQuery } from "@tanstack/react-query"
import {fetchLatestProcessedDataset, ProcessedDatasetResponse} from "../services/processedDatasetService"
import { DatasetType } from "../types/DatasetType"

export function useLatestProcessedDataset(datasetTypes: DatasetType[]) {
    return useQuery<ProcessedDatasetResponse>({
        queryKey: ["processed-dataset", datasetTypes],
        queryFn: () => fetchLatestProcessedDataset(datasetTypes),
        enabled: datasetTypes.length > 0
    })
}
