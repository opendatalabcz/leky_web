// services/ereceptService.ts

import { MedicinalUnitMode } from "../types/MedicinalUnitMode"
import { PopulationNormalisationMode } from "../types/PopulationNormalisationMode"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"
import { TimeGranularity } from "../types/TimeGranularity"
import {MedicineProductInfo} from "../types/MedicineProductInfo";

// ======= Shared Types =======

export type SummaryValues = {
    prescribed: number
    dispensed: number
    difference: number
    percentageDifference: number
}

// ======= DTOs: Aggregate by District =======

export type EreceptAggregateByDistrictRequest = {
    medicinalProductIds: number[]
    registrationNumbers: string[]
    dateFrom: string
    dateTo: string
    aggregationType: EReceptDataTypeAggregation
    medicinalUnitMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
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
    request: EreceptAggregateByDistrictRequest
): Promise<EreceptAggregateByDistrictResponse> {
    const response = await fetch("/api/erecept/prescription-dispense/time-aggregate/by-district", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
    })

    if (!response.ok) {
        throw new Error("Failed to fetch aggregated data by district")
    }

    return response.json()
}

// ======= DTOs: Time Series by District =======

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
    request: EreceptTimeSeriesByDistrictRequest
): Promise<EreceptTimeSeriesByDistrictResponse> {
    const response = await fetch("/api/erecept/prescription-dispense/time-series/by-district", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
    })

    if (!response.ok) {
        throw new Error("Failed to fetch time series by district")
    }

    return response.json()
}

// ======= DTOs: Full Time Series =======

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
    request: EreceptFullTimeSeriesRequest
): Promise<EreceptFullTimeSeriesResponse> {
    const response = await fetch("/api/erecept/prescription-dispense/time-series", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
    })

    if (!response.ok) {
        throw new Error("Failed to fetch full time series")
    }

    return response.json()
}
