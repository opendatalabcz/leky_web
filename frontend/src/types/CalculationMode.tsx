export enum CalculationMode {
    UNITS = "UNITS",
    DAILY_DOSES = "DAILY_DOSES"
}

export const CalculationModeLabels: Record<CalculationMode, string> = {
    [CalculationMode.UNITS]: "Počet kusů",
    [CalculationMode.DAILY_DOSES]: "Denní dávky (DDD)"
}
