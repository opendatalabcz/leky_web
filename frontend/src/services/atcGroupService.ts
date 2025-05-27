// services/atcGroupService.ts

import { AtcGroup } from "../types/AtcGroup"

export async function fetchAtcGroups(): Promise<AtcGroup[]> {
    const response = await fetch("/api/atc-groups")
    if (!response.ok) {
        throw new Error("Failed to fetch ATC groups")
    }
    return response.json()
}
