import { useQuery } from "@tanstack/react-query"
import {
    DistributionSankeyRequest,
    DistributionSankeyResponse,
    fetchDistributionFromDistributorsSankey
} from "../services/distributionService"

export function useDistributionFromDistributorsSankey(
    req: DistributionSankeyRequest | undefined
) {
    return useQuery<DistributionSankeyResponse>({
        queryKey: ["distribution-sankey-distributors", req],
        queryFn: () => {
            if (!req) throw new Error("Request params missing")
            return fetchDistributionFromDistributorsSankey(req)
        },
        enabled: !!req
    })
}
