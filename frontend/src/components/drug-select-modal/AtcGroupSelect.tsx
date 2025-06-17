import React from "react"
import AsyncSelect from "react-select/async"
import { Box } from "@mui/material"
import {fetchAtcGroups} from "../../services/atcGroupService";
import {AtcGroup} from "../../types/AtcGroup";

interface Option {
    label: string
    value: number
}

interface Props {
    selectedAtcGroupId: number | null
    onChange: (atcGroupId: number | null) => void
}

export function AtcGroupSelect({ selectedAtcGroupId, onChange }: Props) {
    const loadOptions = async (inputValue: string): Promise<Option[]> => {
        if (!inputValue) return []

        try {
            const data: AtcGroup[] = await fetchAtcGroups(inputValue)
            return data.map((group) => ({
                label: `${group.name} (${group.code})`,
                value: group.id
            }))
        } catch (error) {
            console.error("Error loading ATC groups:", error)
            return []
        }
    }

    const handleChange = (selectedOption: Option | null) => {
        onChange(selectedOption ? selectedOption.value : null)
    }

    return (
        <Box sx={{ width: '100%' }}>
            <AsyncSelect
                cacheOptions
                loadOptions={loadOptions}
                defaultOptions={false}
                onChange={handleChange}
                placeholder="Začněte psát ATC skupinu..."
                isClearable
                styles={{
                    container: (base) => ({
                        ...base,
                        width: '100%'
                    }),
                    control: (base) => ({
                        ...base,
                        minHeight: 40
                    })
                }}
            />
        </Box>
    )
}
