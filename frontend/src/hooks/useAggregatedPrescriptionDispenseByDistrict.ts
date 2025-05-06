import { useQuery } from "@tanstack/react-query"
import {
    Params,
    getAggregatedPrescriptionDispenseByDistrict,
    EReceptDistrictDataResponseWithSummary
} from "../services/ereceptService"

export function useAggregatedPrescriptionDispenseByDistrict(params?: Params) {
    return useQuery<EReceptDistrictDataResponseWithSummary>({
        queryKey: params ? ["district-aggregate", params] : ["district-aggregate"],
        queryFn: () => {
            if (!params) {
                return Promise.resolve({
                    aggregationType: "PRESCRIBED",
                    calculationMode: "PACKAGES",
                    normalisationMode: "ABSOLUTE",
                    dateFrom: "",
                    dateTo: "",
                    districtValues: {},
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
            return getAggregatedPrescriptionDispenseByDistrict(params)
        },
        enabled: !!params
    })
}
