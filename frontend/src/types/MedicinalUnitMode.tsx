export enum MedicinalUnitMode {
    PACKAGES = "PACKAGES",
    DAILY_DOSES = "DAILY_DOSES"
}

export const MedicinalUnitModeLabels: Record<MedicinalUnitMode, string> = {
    [MedicinalUnitMode.PACKAGES]: "Počet balení",
    [MedicinalUnitMode.DAILY_DOSES]: "Počet doporučených denních dávek"
}
