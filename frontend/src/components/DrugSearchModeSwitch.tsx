import React from "react"
import "./DrugSearchModeSwitch.css"
import { MedicinalProductSearchMode } from "../types/MedicinalProductSearchMode"

type Props = {
    searchMode: MedicinalProductSearchMode
    onChange: (mode: MedicinalProductSearchMode) => void
}

export const DrugSearchModeSwitch: React.FC<Props> = ({ searchMode, onChange }) => (
    <div className="drug-search-mode-switch">
        <span className="filter-title">Zobrazit výsledky dle:</span>
        <div className="filter-mode-switch">
            <button
                type="button"
                className={`mode-button ${searchMode === MedicinalProductSearchMode.SUKL_CODE ? "active" : ""}`}
                onClick={() => onChange(MedicinalProductSearchMode.SUKL_CODE)}
            >
                Dle kódu SÚKL
            </button>
            <button
                type="button"
                className={`mode-button ${searchMode === MedicinalProductSearchMode.REGISTRATION_NUMBER ? "active" : ""}`}
                onClick={() => onChange(MedicinalProductSearchMode.REGISTRATION_NUMBER)}
            >
                Dle Registračního čísla
            </button>
        </div>
    </div>
)
