import { useQuery } from "@tanstack/react-query"
import {
    EreceptFullTimeSeriesRequest,
    EreceptFullTimeSeriesResponse,
    fetchEreceptFullTimeSeries
} from "../services/ereceptService"

export function useEreceptFullTimeSeries(req?: EreceptFullTimeSeriesRequest) {
    return useQuery<EreceptFullTimeSeriesResponse>({
        queryKey: ["erecept-full-time-series", req],
        queryFn: () => {
            if (!req) throw new Error("Request params missing")
            return fetchEreceptFullTimeSeries(req)
        },
        enabled: !!req
    })
}
