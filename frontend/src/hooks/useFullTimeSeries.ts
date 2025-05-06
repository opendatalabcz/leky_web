import { useQuery } from "@tanstack/react-query"
import { getPrescriptionDispenseTimeSeries, FullTimeSeriesParams, FullTimeSeriesResponse } from "../services/ereceptService"

export function useFullTimeSeries(params?: FullTimeSeriesParams) {
    return useQuery<FullTimeSeriesResponse>({
        queryKey: params ? ["full-time-series", params] : ["full-time-series"],
        queryFn: () => params ? getPrescriptionDispenseTimeSeries(params) : Promise.reject("Missing params"),
        enabled: !!params
    })
}
