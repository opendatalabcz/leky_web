import React from "react"
import {DistributionFiltersPanel} from "../components/DistributionFiltersPanel";
import {useFilters} from "../components/FilterContext";

export function DistributionPage() {
    const { common, setCommon } = useFilters()

    return (
        <div>
            <h2>Distribuční tok léčiv</h2>

            <DistributionFiltersPanel
                dateFrom={common.dateFrom}
                dateTo={common.dateTo}
                onChangeDateFrom={(val) => setCommon(prev => ({ ...prev, dateFrom: val }))}
                onChangeDateTo={(val) => setCommon(prev => ({ ...prev, dateTo: val }))}
                calculationMode={common.calculationMode}
                onChangeCalculationMode={(val) => setCommon(prev => ({ ...prev, calculationMode: val }))}
            />
        </div>
    )
}
