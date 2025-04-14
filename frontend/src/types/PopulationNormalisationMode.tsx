export enum PopulationNormalisationMode {
    ABSOLUTE = "ABSOLUTE",
    PER_1000_CAPITA = "PER_1000_CAPITA"
}

export const PopulationNormalisationModeLabels: Record<PopulationNormalisationMode, string> = {
    [PopulationNormalisationMode.ABSOLUTE]: "Absolutní počet",
    [PopulationNormalisationMode.PER_1000_CAPITA]: "Počet na 1000 obyvatel"
}
