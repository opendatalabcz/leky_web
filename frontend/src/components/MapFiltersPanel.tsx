import React from "react"
import { YearMonthPicker } from "./YearMonthPicker"
import { CalculationMode, CalculationModeLabels } from "../types/CalculationMode"
import { NormalisationMode } from "../types/NormalisationMode"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"
import "./MapFiltersPanel.css"

type Props = {
    dateFrom: Date | null
    dateTo: Date | null
    onChangeDateFrom: (date: Date | null) => void
    onChangeDateTo: (date: Date | null) => void

    calculationMode: CalculationMode
    onChangeCalculationMode: (mode: CalculationMode) => void

    normalisationMode: NormalisationMode
    onChangeNormalisationMode: (mode: NormalisationMode) => void

    aggregationType: EReceptDataTypeAggregation
    onChangeAggregationType: (val: EReceptDataTypeAggregation) => void
}

export const MapFiltersPanel: React.FC<Props> = ({
                                                     dateFrom,
                                                     dateTo,
                                                     onChangeDateFrom,
                                                     onChangeDateTo,
                                                     calculationMode,
                                                     onChangeCalculationMode,
                                                     normalisationMode,
                                                     onChangeNormalisationMode,
                                                     aggregationType,
                                                     onChangeAggregationType
                                                 }) => {
    return (
        <div className="map-filters-panel">
            <div className="top-row">
                <select value={aggregationType} onChange={(e) => onChangeAggregationType(e.target.value as EReceptDataTypeAggregation)}>
                    <option value={EReceptDataTypeAggregation.PRESCRIBED}>Předepsané</option>
                    <option value={EReceptDataTypeAggregation.DISPENSED}>Vydané</option>
                    <option value={EReceptDataTypeAggregation.DIFFERENCE}>Rozdíl</option>
                </select>

                <YearMonthPicker
                    label="Období od"
                    value={dateFrom}
                    onChange={onChangeDateFrom}
                    maxDate={dateTo ?? undefined}
                />
                <YearMonthPicker
                    label="Období do"
                    value={dateTo}
                    onChange={onChangeDateTo}
                    minDate={dateFrom ?? undefined}
                />

                <select value={calculationMode} onChange={(e) => onChangeCalculationMode(e.target.value as CalculationMode)}>
                    {Object.values(CalculationMode).map(mode => (
                        <option key={mode} value={mode}>
                            {CalculationModeLabels[mode]}
                        </option>
                    ))}
                </select>

                <select value={normalisationMode} onChange={(e) => onChangeNormalisationMode(e.target.value as NormalisationMode)}>
                    <option value={NormalisationMode.ABSOLUTE}>Absolutně</option>
                    <option value={NormalisationMode.PER_1000}>Na 1000 obyvatel</option>
                </select>
            </div>
        </div>
    )
}
