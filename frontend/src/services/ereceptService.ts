import { MedicinalUnitMode } from "../types/MedicinalUnitMode"
import { PopulationNormalisationMode } from "../types/PopulationNormalisationMode"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"

interface Params {
    dateFrom: string
    dateTo: string
    aggregationType: EReceptDataTypeAggregation
    calculationMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
    medicinalProductIds: number[]
}

export interface MedicineProductInfo {
    id: number
    suklCode: string
}

export interface EReceptDistrictDataResponse {
    aggregationType: EReceptDataTypeAggregation
    calculationMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
    dateFrom: string | null
    dateTo: string | null
    districtValues: Record<string, number>
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
}

export async function getEReceptDistrictData(params: Params): Promise<EReceptDistrictDataResponse> {
    const res = await fetch("/api/district-data", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(params)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst data z backendu.")
    }

    return await res.json()
}
