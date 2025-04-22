import { useQuery } from "@tanstack/react-query"
import {
    Params,
    fetchDistrictTimeSeries,
    DistrictTimeSeriesResponseWithSummary
} from "../services/ereceptService"

export function useDistrictTimeSeries(params?: Params, enabled: boolean = !!params) {
    return useQuery<DistrictTimeSeriesResponseWithSummary>({
        queryKey: params ? ["district-series", params] : ["district-series"],
        queryFn: () => {
            if (!params) {
                return Promise.resolve({
                    aggregationType: "PRESCRIBED",
                    calculationMode: "PACKAGES",
                    normalisationMode: "ABSOLUTE",
                    dateFrom: "",
                    dateTo: "",
                    series: [],
                    includedMedicineProducts: [],
                    ignoredMedicineProducts: [],
                    summary: {
                        prescribed: 0,
                        dispensed: 0,
                        difference: 0,
                        percentageDifference: 0
                    }
                })
            }
            return fetchDistrictTimeSeries(params)
        },
        enabled
    })
}

