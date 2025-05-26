export enum MedicinalUnitMode {
    DAILY_DOSES = "DAILY_DOSES",
    PACKAGES = "PACKAGES"
}

export const MedicinalUnitModeLabels: Record<MedicinalUnitMode, string> = {
    [MedicinalUnitMode.DAILY_DOSES]: "Počet doporučených denních dávek",
    [MedicinalUnitMode.PACKAGES]: "Počet balení"
};

export const MedicinalUnitModeUnits: Record<MedicinalUnitMode, string> = {
    [MedicinalUnitMode.DAILY_DOSES]: "denních dávek",
    [MedicinalUnitMode.PACKAGES]: "balení"
};

