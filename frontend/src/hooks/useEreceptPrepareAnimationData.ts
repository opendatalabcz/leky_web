import { useMemo } from "react"
import { SummaryValues } from "../services/ereceptService"

export function useEreceptPrepareAnimationData(series: MonthSeriesEntryWithSummary[]) {

    const monthly = useMemo(() => {
        const map = new Map()
        series.forEach(s => {map.set(s.month, s.districtValues)})
        return map
    }, [series])

    const monthlySummaries = useMemo(() => {
        const map = new Map<string, SummaryValues>()
        series.forEach(s => map.set(s.month, s.summary))
        return map
    }, [series])

    const aggregated = useMemo(() => {
        const agg: Record<string, number> = {}
        series.forEach(s => {
            Object.entries(s.districtValues).forEach(([dist, val]) => {
                agg[dist] = (agg[dist] ?? 0) + val
            })
        })
        return agg
    }, [series])

    const aggregatedSummary = useMemo<SummaryValues>(() => {
        const summaryInit: SummaryValues = {
            prescribed: 0,
            dispensed: 0,
            difference: 0,
            percentageDifference: 0
        }

        const sum = series.reduce(
            (acc, s) => {
                acc.prescribed += s.summary.prescribed
                acc.dispensed += s.summary.dispensed
                acc.difference += s.summary.difference
                return acc
            },
            { ...summaryInit }
        )

        const total = sum.prescribed || sum.dispensed
        sum.percentageDifference = total === 0
            ? 0
            : (sum.difference / total) * 100

        return sum
    }, [series])

    return {
        monthly,
        monthlySummaries,
        aggregated,
        aggregatedSummary
    }

}

export interface MonthSeriesEntryWithSummary {
    month: string
    districtValues: Record<string, number>
    summary: SummaryValues
}
