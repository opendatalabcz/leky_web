import { useEffect, useState } from "react"

export type ProcessedDatasetResponse = {
    datasetType: string
    createdAt: string
    year: number
    month: number
}

export function useProcessedDataset(datasetTypes: string[]) {
    const [data, setData] = useState<ProcessedDatasetResponse | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<Error | null>(null)

    useEffect(() => {
        setLoading(true)
        fetch(`/api/processed-datasets/latest?${datasetTypes.map(t => `types=${t}`).join("&")}`)
            .then(res => {
                if (!res.ok) throw new Error("Failed to fetch dataset status")
                return res.json()
            })
            .then(json => {
                setData(json)
                setLoading(false)
            })
            .catch(err => {
                setError(err)
                setLoading(false)
            })
    }, [datasetTypes])

    return { data, loading, error }
}
