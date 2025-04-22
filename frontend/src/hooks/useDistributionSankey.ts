// hooks/useDistributionSankey.ts

import { useQuery } from "@tanstack/react-query"
import { fetchDistributionSankey, DistributionSankeyRequest } from "../services/distributionService"

export const useDistributionSankey = (req: DistributionSankeyRequest | undefined) => {
    return useQuery({
        queryKey: ["distributionSankey", req],
        queryFn: () => req ? fetchDistributionSankey(req) : Promise.resolve(null),
        enabled: !!req
    })
}
