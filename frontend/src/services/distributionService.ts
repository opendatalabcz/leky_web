// services/distributionService.ts

import { TimeGranularity } from "../types/TimeGranularity"
import { MedicinalUnitMode } from "../types/MedicinalUnitMode"
import {MedicineProductInfo} from "../types/MedicineProductInfo";

// ======= DTOs: Sankey Diagram =======

export type DistributionSankeyRequest = {
    medicinalProductIds: number[]
    registrationNumbers: string[]
    dateFrom: string // "yyyy-MM"
    dateTo: string // "yyyy-MM"
    medicinalUnitMode: MedicinalUnitMode
}

export type SankeyNodeDto = {
    id: string
    label: string
}

export type SankeyLinkDto = {
    source: string
    target: string
    value: number
}

export type DistributionSankeyResponse = {
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
    dateFrom: string
    dateTo: string
    medicinalUnitMode: MedicinalUnitMode
    nodes: SankeyNodeDto[]
    links: SankeyLinkDto[]
}

export async function fetchDistributionSankeyDiagram(
    request: DistributionSankeyRequest
): Promise<DistributionSankeyResponse> {
    const response = await fetch("/api/distribution/sankey-diagram", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
    })

    if (!response.ok) {
        throw new Error("Failed to fetch data for distribution Sankey diagram")
    }

    return response.json()
}

// ======= DTOs: Time Series =======

export type DistributionTimeSeriesRequest = {
    medicinalProductIds: number[]
    registrationNumbers: string[]
    dateFrom: string // "yyyy-MM"
    dateTo: string // "yyyy-MM"
    medicinalUnitMode: MedicinalUnitMode
    timeGranularity: TimeGranularity
}

export type DistributionFlowEntry = {
    source: string
    target: string
    value: number
}

export type DistributionTimeSeriesPeriodEntry = {
    period: string
    flows: DistributionFlowEntry[]
}

export type DistributionTimeSeriesResponse = {
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
    dateFrom: string
    dateTo: string
    medicinalUnitMode: MedicinalUnitMode
    timeGranularity: TimeGranularity
    series: DistributionTimeSeriesPeriodEntry[]
}

export async function fetchDistributionTimeSeries(
    request: DistributionTimeSeriesRequest
): Promise<DistributionTimeSeriesResponse> {
    const response = await fetch("/api/distribution/time-series", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
    })

    if (!response.ok) {
        throw new Error("Failed to fetch data for distribution time series")
    }

    return response.json()
}
