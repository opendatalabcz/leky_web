import { useQuery } from "@tanstack/react-query"
import {
    DistributionSankeyRequest,
    DistributionSankeyResponse,
    fetchDistributionSankeyDiagram
} from "../services/distributionService"

export function useDistributionSankeyDiagram(
    req: DistributionSankeyRequest | undefined
) {
    return useQuery<DistributionSankeyResponse>({
        queryKey: ["distribution-sankey-diagram", req],
        queryFn: () => {
            if (!req) throw new Error("Request params missing")
            return fetchDistributionSankeyDiagram(req)
        },
        enabled: !!req
    })
}
