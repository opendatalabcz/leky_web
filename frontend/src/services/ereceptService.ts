import { MedicinalUnitMode } from "../types/MedicinalUnitMode"
import { PopulationNormalisationMode } from "../types/PopulationNormalisationMode"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"
import { TimeGranularity } from "../types/TimeGranularity"

// ======= DTOs: Erecept Aggregate by District =======

export type EreceptAggregateByDistrictRequest = {
    medicinalProductIds: number[]
    registrationNumbers: string[]
    dateFrom: string
    dateTo: string
    aggregationType: EReceptDataTypeAggregation
    medicinalUnitMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
}

export type MedicineProductInfo = {
    id: number
    suklCode: string
}

export type SummaryValues = {
    prescribed: number
    dispensed: number
    difference: number
    percentageDifference: number
}

export type EreceptAggregateByDistrictResponse = {
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
    dateFrom: string
    dateTo: string
    aggregationType: string
    medicinalUnitMode: string
    normalisationMode: string
    districtValues: Record<string, number>
    summary: SummaryValues
}

export async function fetchEreceptAggregateByDistrict(
    req: EreceptAggregateByDistrictRequest
): Promise<EreceptAggregateByDistrictResponse> {
    const res = await fetch("/api/erecept/prescription-dispense/time-aggregate/by-district", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst agregovaná data podle okresů")
    }

    return res.json()
}

// ======= DTOs: Erecept Time Series by District =======

export type EreceptTimeSeriesByDistrictRequest = EreceptAggregateByDistrictRequest

export type EreceptMonthSeriesEntry = {
    month: string
    districtValues: Record<string, number>
    summary: SummaryValues
}

export type EreceptTimeSeriesByDistrictResponse = {
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
    dateFrom: string
    dateTo: string
    aggregationType: string
    medicinalUnitMode: string
    normalisationMode: string
    series: EreceptMonthSeriesEntry[]
}

export async function fetchEreceptTimeSeriesByDistrict(
    req: EreceptTimeSeriesByDistrictRequest
): Promise<EreceptTimeSeriesByDistrictResponse> {
    const res = await fetch("/api/erecept/prescription-dispense/time-series/by-district", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst časovou řadu podle okresů")
    }

    return res.json()
}

// ======= DTOs: Erecept Full Time Series =======

export type EreceptFullTimeSeriesRequest = {
    medicinalProductIds: number[]
    registrationNumbers: string[]
    medicinalUnitMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
    timeGranularity: TimeGranularity
    district?: string | null
}

export type EreceptFullTimeSeriesEntry = {
    period: string
    prescribed: number
    dispensed: number
    difference: number
}

export type EreceptFullTimeSeriesResponse = {
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
    medicinalUnitMode: string
    normalisationMode: string
    timeGranularity: TimeGranularity
    district?: string | null
    series: EreceptFullTimeSeriesEntry[]
}

export async function fetchEreceptFullTimeSeries(
    req: EreceptFullTimeSeriesRequest
): Promise<EreceptFullTimeSeriesResponse> {
    const res = await fetch("/api/erecept/prescription-dispense/time-series", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst úplnou časovou řadu")
    }

    return res.json()
}
