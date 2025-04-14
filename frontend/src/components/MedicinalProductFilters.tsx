import React, {useEffect, useState} from "react"
import {MedicinalProductFilterValues} from "../types/MedicinalProductFilterValues"
import {SubstanceSelect} from "./SubstanceSelect"
import "./MedicinalProductFilters.css"

type Props = {
    filters: MedicinalProductFilterValues
    onChange: (updated: MedicinalProductFilterValues) => void
    onSearchClick: () => void
}

type AtcGroupOption = {
    id: number
    name: string
    code: string
}

export const MedicinalProductFilters: React.FC<Props> = ({ filters, onChange, onSearchClick }) => {
    const [atcOptions, setAtcOptions] = useState<AtcGroupOption[]>([])

    useEffect(() => {
        fetch("/api/atc-groups")
            .then(res => res.json())
            .then(data => setAtcOptions(data))
    }, [])

    return (
        <form
            className="medicinal-product-filters"
            onSubmit={(e) => {
                e.preventDefault()
                onSearchClick()
            }}
        >
            <div className="filter-row row-cols">
                <label>
                    Název / SÚKL / Registrační číslo:
                    <input
                        type="text"
                        value={filters.medicinalProductQuery}
                        onChange={(e) =>
                            onChange({ ...filters, medicinalProductQuery: e.target.value })
                        }
                        placeholder="např. Paralen / 272209 / 54/432/01-C / Paralen 500"
                    />
                </label>

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

                <div className="filter-actions">
                    <button type="submit">Vyhledat</button>
                </div>
            </div>
        </form>
    )
}
