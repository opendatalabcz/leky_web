import { useQuery } from "@tanstack/react-query"
import {
    Params,
    getPrescriptionDispenseTimeAggregateByDistrict,
    EReceptDistrictDataResponseWithSummary
} from "../services/ereceptService"
import {MedicinalUnitMode} from "../types/MedicinalUnitMode";

export function useAggregatedPrescriptionDispenseByDistrict(params?: Params) {
    return useQuery<EReceptDistrictDataResponseWithSummary>({
        queryKey: params ? ["district-aggregate", params] : ["district-aggregate"],
        queryFn: () => {
            if (!params) {
                return Promise.resolve({
                    aggregationType: "PRESCRIBED",
                    medicinalUnitMode: MedicinalUnitMode.PACKAGES,
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
            return getPrescriptionDispenseTimeAggregateByDistrict(params)
        },
        enabled: !!params
    })
}
