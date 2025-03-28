import React, {useEffect, useState} from "react"

export interface FilterValues {
    atcGroupId: number | null
    substanceId: number | null
    medicinalProductQuery: string
    period: string
}

interface Props {
    filters: FilterValues
    onChange: (updated: FilterValues) => void
}

interface AtcGroupOption {
    id: number
    name: string
    code: string
}

interface SubstanceOption {
    id: number
    name: string
    code: string
}

export function Filters({ filters, onChange }: Props) {
    const [atcOptions, setAtcOptions] = useState<AtcGroupOption[]>([])
    const [substanceSuggestions, setSubstanceSuggestions] = useState<SubstanceOption[]>([])
    const [substanceQuery, setSubstanceQuery] = useState("")

    // Fetch ATC groups on load
    useEffect(() => {
        fetch("/api/atc-groups")
            .then(res => res.json())
            .then(data => setAtcOptions(data))
    }, [])

    // Fetch substance suggestions
    useEffect(() => {
        if (substanceQuery.length < 3) {
            setSubstanceSuggestions([])
            return
        }

        const delay = setTimeout(() => {
            fetch(`/api/substances?query=${substanceQuery}`)
                .then(res => res.json())
                .then(data => setSubstanceSuggestions(data))
        }, 300)

        return () => clearTimeout(delay)
    }, [substanceQuery])

    return (
        <div className="filter-panel">
            <div>
                <label>
                    Léčivý přípravek / SÚKL kód / Reg. číslo:
                    <input
                        type="text"
                        value={filters.medicinalProductQuery}
                        onChange={(e) =>
                            onChange({ ...filters, medicinalProductQuery: e.target.value })
                        }
                        placeholder="např. paracetamol, 123456, 54/432/01-C"
                    />
                </label>
                <br/>
                <label>ATC skupina:</label>
                <select
                    value={filters.atcGroupId ?? ""}
                    onChange={e => onChange({ ...filters, atcGroupId: e.target.value ? Number(e.target.value) : null })}
                >
                    <option value="">-- Vyberte ATC skupinu --</option>
                    {atcOptions.map(atc => (
                        <option key={atc.id} value={atc.id}>
                            {atc.name} ({atc.code})
                        </option>
                    ))}
                </select>
            </div>

            <div>
                <label>Látka (substance):</label>
                <input
                    type="text"
                    placeholder="Začněte psát..."
                    onChange={e => {
                        setSubstanceQuery(e.target.value)
                        onChange({ ...filters, substanceId: null })
                    }}
                    list="substance-options"
                />
                <datalist id="substance-options">
                    {substanceSuggestions.map(sub => (
                        <option
                            key={sub.id}
                            value={`${sub.name} (${sub.code})`}
                            onClick={() => onChange({ ...filters, substanceId: sub.id })}
                        />
                    ))}
                </datalist>
            </div>
        </div>
    )
}
