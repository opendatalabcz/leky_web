// services/distributionService.ts

export type DistributionSankeyRequest = {
    medicinalProductIds: number[]
    dateFrom: string // "yyyy-MM"
    dateTo: string // "yyyy-MM"
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
    nodes: SankeyNodeDto[]
    links: SankeyLinkDto[]
    includedMedicineProducts: { id: number; suklCode: string }[]
    ignoredMedicineProducts: { id: number; suklCode: string }[]
}

export async function fetchDistributionSankey(
    req: DistributionSankeyRequest
): Promise<DistributionSankeyResponse> {
    const res = await fetch("/api/distribution/graph", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst distribuční tok")
    }

    return res.json()
}

export async function fetchDistributionFromDistributorsSankey(
    req: DistributionSankeyRequest
): Promise<DistributionSankeyResponse> {
    const res = await fetch("/api/distribution/graph/from-distributors", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst distribuční tok (distributoři → pacienti)")
    }

    return res.json()
}

export async function fetchCombinedDistributionSankey(
    req: DistributionSankeyRequest
): Promise<DistributionSankeyResponse> {
    const res = await fetch("/api/distribution/graph/combined", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst kombinovaný distribuční tok")
    }

    return res.json()
}

// types
export type DistributionTimeSeriesRequest = {
    medicinalProductIds: number[]
    dateFrom: string // "yyyy-MM"
    dateTo: string // "yyyy-MM"
    granularity: "MONTH" | "YEAR"
}

export type DistributionTimeSeriesEntry = {
    period: string
    mahToDistributor: number
    distributorToPharmacy: number
    pharmacyToPatient: number
}

export type DistributionTimeSeriesResponse = {
    granularity: "MONTH" | "YEAR"
    series: DistributionTimeSeriesEntry[]
    includedMedicineProducts: { id: number; suklCode: string }[]
    ignoredMedicineProducts: { id: number; suklCode: string }[]
}

// API volání
export async function fetchDistributionTimeSeries(
    req: DistributionTimeSeriesRequest
): Promise<DistributionTimeSeriesResponse> {
    const res = await fetch("/api/distribution/time-series", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req)
    })

    if (!res.ok) {
        throw new Error("Nepodařilo se načíst časovou řadu distribuce")
    }

    return res.json()
}
