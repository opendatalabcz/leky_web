import React, { createContext, useContext, useState } from "react"
import { MedicinalUnitMode } from "../types/MedicinalUnitMode"
import { PopulationNormalisationMode } from "../types/PopulationNormalisationMode"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"

interface CommonFilters {
    dateFrom: Date | null
    dateTo: Date | null
    medicinalUnitMode: MedicinalUnitMode
}

interface PrescriptionDispenseFilters {
    aggregationType: EReceptDataTypeAggregation
    normalisationMode: PopulationNormalisationMode
}

interface DistributionFlowFilters {
    movementType: string
}

interface FilterContextValue {
    common: CommonFilters
    setCommon: React.Dispatch<React.SetStateAction<CommonFilters>>

    prescriptionDispense: PrescriptionDispenseFilters
    setPrescriptionDispense: React.Dispatch<React.SetStateAction<PrescriptionDispenseFilters>>

    distributionFlow: DistributionFlowFilters
    setDistributionFlow: React.Dispatch<React.SetStateAction<DistributionFlowFilters>>
}

const FilterContext = createContext<FilterContextValue | undefined>(undefined)

const today = new Date()
const defaultDateTo = new Date(today.getFullYear(), today.getMonth() - 1, 1)
const defaultDateFrom = new Date(today.getFullYear() - 1, today.getMonth() - 1, 1)

const defaultCommon: CommonFilters = {
    dateFrom: defaultDateFrom,
    dateTo: defaultDateTo,
    medicinalUnitMode: MedicinalUnitMode.PACKAGES,
}

const defaultPrescriptionDispense: PrescriptionDispenseFilters = {
    aggregationType: EReceptDataTypeAggregation.DIFFERENCE,
    normalisationMode: PopulationNormalisationMode.PER_100000_CAPITA,
}

const defaultDistributionFlow: DistributionFlowFilters = {
    movementType: "DELIVERY",
}

// Provider
export const FilterProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [common, setCommon] = useState<CommonFilters>(defaultCommon)
    const [prescriptionDispense, setPrescriptionDispense] = useState<PrescriptionDispenseFilters>(defaultPrescriptionDispense)
    const [distributionFlow, setDistributionFlow] = useState<DistributionFlowFilters>(defaultDistributionFlow)

    const value: FilterContextValue = {
        common,
        setCommon,
        prescriptionDispense,
        setPrescriptionDispense,
        distributionFlow,
        setDistributionFlow
    }

    return (
        <FilterContext.Provider value={value}>
            {children}
        </FilterContext.Provider>
    )
}

export function useFilters() {
    const ctx = useContext(FilterContext)
    if (!ctx) {
        throw new Error("useFilters must be used inside FilterProvider.")
    }
    return ctx
}
