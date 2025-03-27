import React, { useEffect, useState } from "react"
import Select from "react-select"
import { fetchAtcGroups } from "../services/atcService"

export type FilterValues = {
    atcGroup: string
    medicineName: string
    period: string
}

type FiltersProps = {
    filters: FilterValues
    onChange: (values: FilterValues) => void
}

type Option = {
    value: string
    label: string
}

export function Filters({ filters, onChange }: FiltersProps) {
    const [options, setOptions] = useState<Option[]>([])

    useEffect(() => {
        fetchAtcGroups()
            .then(groups => {
                const opts = groups.map(g => ({
                    value: g.id.toString(),
                    label: `${g.name} (${g.code})`
                }))
                setOptions(opts)
            })
            .catch(err => console.error("Chyba při načítání ATC skupin", err))
    }, [])

    const handleSelectChange = (selected: Option | null) => {
        onChange({
            ...filters,
            atcGroup: selected?.value ?? ""
        })
    }

    return (
        <div style={{ marginBottom: "1.5rem" }}>
            <label style={{ display: "block", marginBottom: "0.5rem" }}>
                ATC skupina:
            </label>
            <Select
                options={options}
                value={options.find(o => o.value === filters.atcGroup) || null}
                onChange={handleSelectChange}
                placeholder="Vyberte nebo napište ATC skupinu..."
                isClearable
            />
        </div>
    )
}
