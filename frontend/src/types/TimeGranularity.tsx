export enum TimeGranularity {
    MONTH = "MONTH",
    YEAR = "YEAR"
}

export const TimeGranularityLabels: Record<TimeGranularity, string> = {
    [TimeGranularity.MONTH]: "Měsíční",
    [TimeGranularity.YEAR]: "Roční"
}
