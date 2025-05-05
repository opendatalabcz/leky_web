import { useQuery } from "@tanstack/react-query"
import {
    DistributionSankeyRequest,
    DistributionSankeyResponse,
    fetchCombinedDistributionSankey
} from "../services/distributionService"

export function useCombinedDistributionSankey(
    req: DistributionSankeyRequest | undefined
) {
    return useQuery<DistributionSankeyResponse>({
        queryKey: ["distribution-sankey-combined", req],
        queryFn: () => {
            if (!req) throw new Error("Request params missing")
            return fetchCombinedDistributionSankey(req)
        },
        enabled: !!req
    })
}
