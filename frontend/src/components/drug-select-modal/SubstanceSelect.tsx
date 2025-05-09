import React from "react"
import AsyncSelect from "react-select/async"
import { Box } from "@mui/material"

export interface SubstanceOption {
    id: number
    name: string
    code: string
}

interface Option {
    label: string
    value: number
}

interface Props {
    selectedSubstanceId: number | null
    onChange: (substanceId: number | null) => void
}

export function SubstanceSelect({ selectedSubstanceId, onChange }: Props) {
    const loadOptions = async (inputValue: string): Promise<Option[]> => {
        if (inputValue.length < 3) return []

        const response = await fetch(`/api/substances?query=${inputValue}`)
        const data: SubstanceOption[] = await response.json()

        return data.map(sub => ({
            label: `${sub.name} (${sub.code})`,
            value: sub.id
        }))
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
                placeholder="Začněte psát název látky..."
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
