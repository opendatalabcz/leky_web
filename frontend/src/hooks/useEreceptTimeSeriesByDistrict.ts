import { useQuery } from "@tanstack/react-query"
import {
    EreceptTimeSeriesByDistrictRequest,
    EreceptTimeSeriesByDistrictResponse,
    fetchEreceptTimeSeriesByDistrict
} from "../services/ereceptService"

export function useEreceptTimeSeriesByDistrict(req?: EreceptTimeSeriesByDistrictRequest) {
    return useQuery<EreceptTimeSeriesByDistrictResponse>({
        queryKey: ["erecept-time-series-by-district", req],
        queryFn: () => {
            if (!req) throw new Error("Request params missing")
            return fetchEreceptTimeSeriesByDistrict(req)
        },
        enabled: !!req
    })
}
