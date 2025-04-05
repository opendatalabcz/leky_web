import React, {useEffect, useState} from "react"
import "./Filters.css"
import {SubstanceSelect} from "./SubstanceSelect";

export interface FilterValues {
    atcGroupId: number | null
    substanceId: number | null
    medicinalProductQuery: string
    period: string
}

interface Props {
    filters: FilterValues
    onChange: (updated: FilterValues) => void
    onSearchClick: () => void
}

interface AtcGroupOption {
    id: number
    name: string
    code: string
}

export function Filters({ filters, onChange, onSearchClick }: Props) {
    const [atcOptions, setAtcOptions] = useState<AtcGroupOption[]>([])

    useEffect(() => {
        fetch("/api/atc-groups")
            .then(res => res.json())
            .then(data => setAtcOptions(data))
    }, [])

    return (
        <div className="filters-container">
            <div className="filter-row">
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
            </div>

            <div className="filter-row row-cols">
                <label>
                    ATC skupina:
                    <select
                        value={filters.atcGroupId ?? ""}
                        onChange={e =>
                            onChange({ ...filters, atcGroupId: e.target.value ? Number(e.target.value) : null })
                        }
                    >
                        <option value="">-- Vyberte ATC skupinu --</option>
                        {atcOptions.map(atc => (
                            <option key={atc.id} value={atc.id}>
                                {atc.name} ({atc.code})
                            </option>
                        ))}
                    </select>
                </label>

                <SubstanceSelect
                    selectedSubstanceId={filters.substanceId}
                    onChange={(id) => onChange({ ...filters, substanceId: id })}
                />
            </div>

            <div className="filter-actions">
                <button type="button" onClick={onSearchClick}>
                    Vyhledat
                </button>
            </div>
        </div>
    )
}
