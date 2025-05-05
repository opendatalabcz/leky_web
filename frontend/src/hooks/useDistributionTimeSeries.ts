import { useQuery } from "@tanstack/react-query"
import {
    DistributionTimeSeriesRequest,
    DistributionTimeSeriesResponse,
    fetchDistributionTimeSeries
} from "../services/distributionService"

export function useDistributionTimeSeries(
    req: DistributionTimeSeriesRequest | undefined
) {
    return useQuery<DistributionTimeSeriesResponse>({
        queryKey: ["distribution-time-series", req],
        queryFn: () => {
            if (!req) throw new Error("Request params missing")
            return fetchDistributionTimeSeries(req)
        },
        enabled: !!req
    })
}
