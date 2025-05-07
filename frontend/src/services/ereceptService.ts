import { MedicinalUnitMode } from "../types/MedicinalUnitMode"
import { PopulationNormalisationMode } from "../types/PopulationNormalisationMode"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"

export interface Params {
    dateFrom: string
    dateTo: string
    aggregationType: EReceptDataTypeAggregation
    medicinalUnitMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
    medicinalProductIds: number[]
}

export interface MedicineProductInfo {
    id: number
    suklCode: string
}

export type SummaryValues = {
    prescribed: number
    dispensed: number
    difference: number
    percentageDifference: number
}

export type EReceptDistrictDataResponseWithSummary = {
    aggregationType: string
    medicinalUnitMode: string
    normalisationMode: string
    dateFrom: string
    dateTo: string
    districtValues: Record<string, number>
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
    summary: SummaryValues
}

export type DistrictTimeSeriesResponseWithSummary = {
    aggregationType: string
    medicinalUnitMode: string
    normalisationMode: string
    dateFrom: string
    dateTo: string
    series: {
        month: string
        values: Record<string, number>
        summary: SummaryValues
    }[]
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
}

export async function getPrescriptionDispenseTimeAggregateByDistrict(params: Params): Promise<EReceptDistrictDataResponseWithSummary> {
    const res = await fetch("/api/erecept/prescription-dispense/time-aggregate/by-district", {
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

export async function getPrescriptionDispenseTimeSeriesByDistrict(params: Params) {
    const res = await fetch(
        "/api/erecept/prescription-dispense/time-series/by-district",
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(params)
        }
    )
    if (!res.ok) {
        throw new Error("Nepodařilo se načíst data z backendu.")
    }
    return (await res.json()) as DistrictTimeSeriesResponseWithSummary
}

export async function getPrescriptionDispenseTimeSeries(params: FullTimeSeriesParams): Promise<FullTimeSeriesResponse> {
    const res = await fetch("/api/erecept/prescription-dispense/time-series", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(params)
    })

    if (!res.ok) throw new Error("Nepodařilo se načíst časovou řadu")
    return res.json()
}

export interface MonthSeriesEntryWithSummary {
    month: string
    values: Record<string, number>
    summary: SummaryValues
}

export interface FullTimeSeriesParams {
    aggregationType: EReceptDataTypeAggregation
    medicinalUnitMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
    medicinalProductIds: number[]
    registrationNumbers: string[]
    granularity: "MONTH" | "YEAR"
    district?: string | null
}

export interface FullTimeSeriesEntry {
    period: string
    prescribed: number
    dispensed: number
    difference: number
}

export interface FullTimeSeriesResponse {
    aggregationType: string
    medicinalUnitMode: string
    normalisationMode: string
    granularity: "MONTH" | "YEAR"
    district?: string | null
    series: FullTimeSeriesEntry[]
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
}
