// services/distributionService.ts

import { TimeGranularity } from "../types/TimeGranularity"
import { MedicinalUnitMode } from "../types/MedicinalUnitMode"

// ======= DTOs: Sankey Diagram =======

export type DistributionSankeyRequest = {
    medicinalProductIds: number[]
    registrationNumbers: string[]
    dateFrom: string // "yyyy-MM"
    dateTo: string // "yyyy-MM"
    calculationMode: MedicinalUnitMode
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
    includedMedicineProducts: { id: number; suklCode: string }[]
    ignoredMedicineProducts: { id: number; suklCode: string }[]
    dateFrom: string
    dateTo: string
    calculationMode: MedicinalUnitMode
    nodes: SankeyNodeDto[]
    links: SankeyLinkDto[]
}

export async function fetchDistributionSankeyDiagram(
    req: DistributionSankeyRequest
): Promise<DistributionSankeyResponse> {
    const res = await fetch("/api/distribution/sankey-diagram", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst data pro Sankey diagram distribuce")
    }

    return res.json()
}

// ======= DTOs: Time Series =======

export type DistributionTimeSeriesRequest = {
    medicinalProductIds: number[]
    registrationNumbers: string[]
    dateFrom: string // "yyyy-MM"
    dateTo: string // "yyyy-MM"
    calculationMode: MedicinalUnitMode
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
    includedMedicineProducts: { id: number; suklCode: string }[]
    ignoredMedicineProducts: { id: number; suklCode: string }[]
    dateFrom: string
    dateTo: string
    calculationMode: MedicinalUnitMode
    timeGranularity: TimeGranularity
    series: DistributionTimeSeriesPeriodEntry[]
}

export async function fetchDistributionTimeSeries(
    req: DistributionTimeSeriesRequest
): Promise<DistributionTimeSeriesResponse> {
    const res = await fetch("/api/distribution/time-series", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst data pro časovou řadu distribuce")
    }

    return res.json()
}
