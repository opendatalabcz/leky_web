import React from "react"
import { useFilters } from "../components/FilterContext"
import { EReceptFiltersPanel } from "../components/EReceptFiltersPanel"

export function EReceptPage() {
    const { common, setCommon, prescriptionDispense, setPrescriptionDispense } = useFilters()

    return (
        <div>
            <h2>Předepisování a výdej</h2>

            <EReceptFiltersPanel
                dateFrom={common.dateFrom}
                dateTo={common.dateTo}
                onChangeDateFrom={(date) => setCommon({ ...common, dateFrom: date })}
                onChangeDateTo={(date) => setCommon({ ...common, dateTo: date })}
                calculationMode={common.calculationMode}
                onChangeCalculationMode={(mode) =>
                    setCommon({ ...common, calculationMode: mode })
                }
                normalisationMode={prescriptionDispense.normalisationMode}
                onChangeNormalisationMode={(nm) =>
                    setPrescriptionDispense({ ...prescriptionDispense, normalisationMode: nm })
                }
                aggregationType={prescriptionDispense.aggregationType}
                onChangeAggregationType={(val) =>
                    setPrescriptionDispense({ ...prescriptionDispense, aggregationType: val })
                }
            />
        </div>
    )
}
