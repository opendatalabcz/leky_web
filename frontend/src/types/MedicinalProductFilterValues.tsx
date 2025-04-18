import {MedicinalProductSearchMode} from "./MedicinalProductSearchMode";

export interface MedicinalProductFilterValues {
    searchMode: MedicinalProductSearchMode
    atcGroupId: number | null
    substanceId: number | null
    medicinalProductQuery: string
    period: string
}
