// services/atcGroupService.ts

import { AtcGroup } from "../types/AtcGroup"

export async function fetchAtcGroups(query?: string): Promise<AtcGroup[]> {
    const url = query
        ? `/api/atc-groups?query=${encodeURIComponent(query)}`
        : "/api/atc-groups"

    const response = await fetch(url)
    if (!response.ok) {
        throw new Error("Failed to fetch ATC groups")
    }

    return response.json()
}
