export enum PopulationNormalisationMode {
    PER_100000_CAPITA = "PER_100000_CAPITA",
    ABSOLUTE = "ABSOLUTE"
}

export const PopulationNormalisationModeLabels: Record<PopulationNormalisationMode, string> = {
    [PopulationNormalisationMode.PER_100000_CAPITA]: "Počet na 100 000 obyvatel",
    [PopulationNormalisationMode.ABSOLUTE]: "Absolutní počet"
}
