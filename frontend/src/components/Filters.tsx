import React, { useEffect, useState } from "react"
import "./Filters.css"
import { SubstanceSelect } from "./SubstanceSelect"

export enum SearchMode {
    SUKL_CODE = "SUKL_CODE",
    REGISTRATION_NUMBER = "REGISTRATION_NUMBER"
}

export interface FilterValues {
    searchMode: SearchMode
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
        <form
            className="filters-container"
            onSubmit={(e) => {
                e.preventDefault()
                onSearchClick()
            }}
        >
            <div className="filter-header">
                <span className="filter-title">Vyhledávání léčiva:</span>

                <div className="filter-mode-switch">
                    <button
                        type="button"
                        className={`mode-button ${filters.searchMode === SearchMode.SUKL_CODE ? "active" : ""}`}
                        onClick={() => onChange({ ...filters, searchMode: SearchMode.SUKL_CODE })}
                    >
                        Dle kódu SÚKL
                    </button>
                    <button
                        type="button"
                        className={`mode-button ${filters.searchMode === SearchMode.REGISTRATION_NUMBER ? "active" : ""}`}
                        onClick={() => onChange({ ...filters, searchMode: SearchMode.REGISTRATION_NUMBER })}
                    >
                        Dle Registračního čísla
                    </button>
                </div>

                <button type="submit" className="search-button">
                    Vyhledat
                </button>
            </div>

            <div className="filter-row row-cols">
                <label className="full-col">
                    Název / SÚKL kód / Registrační číslo / Název + síla:
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
            </div>
        </form>
    )
}
