export enum PopulationNormalisationMode {
    ABSOLUTE = "ABSOLUTE",
    PER_100000_CAPITA = "PER_100000_CAPITA"
}

export const PopulationNormalisationModeLabels: Record<PopulationNormalisationMode, string> = {
    [PopulationNormalisationMode.ABSOLUTE]: "Absolutní počet",
    [PopulationNormalisationMode.PER_100000_CAPITA]: "Počet na 100 000 obyvatel"
}
