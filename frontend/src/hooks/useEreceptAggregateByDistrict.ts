import { useQuery } from "@tanstack/react-query"
import {
    EreceptAggregateByDistrictRequest,
    EreceptAggregateByDistrictResponse,
    fetchEreceptAggregateByDistrict
} from "../services/ereceptService"

export function useEreceptAggregateByDistrict(req?: EreceptAggregateByDistrictRequest) {
    return useQuery<EreceptAggregateByDistrictResponse>({
        queryKey: ["erecept-aggregate-by-district", req],
        queryFn: () => {
            if (!req) throw new Error("Request params missing")
            return fetchEreceptAggregateByDistrict(req)
        },
        enabled: !!req
    })
}
