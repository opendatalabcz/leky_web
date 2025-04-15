import React, {useCallback, useEffect, useState} from "react"
import {format} from "date-fns"
import {FeatureCollection} from "geojson"
import {useUnifiedCart} from "./UnifiedCartContext"
import DistrictMap from "./DistrictMap"
import {MedicineSelectorModal} from "./MedicineSelectorModal"
import {SelectedMedicinalProductSummary} from "./SelectedMedicinalProductSummary"
import {EReceptFiltersPanel} from "./EReceptFiltersPanel"
import {Button} from "@mui/material"
import {MedicinalUnitMode} from "../types/MedicinalUnitMode"
import {EReceptDataTypeAggregation} from "../types/EReceptDataTypeAggregation"
import {PopulationNormalisationMode} from "../types/PopulationNormalisationMode"
import "./MapTab.css"
import {SummaryTiles} from "./SummaryTiles";

type MedicineProductInfo = {
    id: number
    suklCode: string
}

type EReceptDistrictDataResponse = {
    aggregationType: EReceptDataTypeAggregation
    calculationMode: MedicinalUnitMode
    normalisationMode: PopulationNormalisationMode
    dateFrom: string | null
    dateTo: string | null
    districtValues: Record<string, number>
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
}

export function MapTab() {
    const { suklIds } = useUnifiedCart()

    const [geojsonData, setGeojsonData] = useState<FeatureCollection | null>(null)
    const [districtValuesByCode, setDistrictValuesByCode] = useState<Record<string, number> | null>(null)
    const [ignoredProducts, setIgnoredProducts] = useState<MedicineProductInfo[]>([])
    const [isModalOpen, setIsModalOpen] = useState(false)
    const [activeTab, setActiveTab] = useState<"map" | "sankey">("map")

    const [aggregationType, setAggregationType] = useState<EReceptDataTypeAggregation>(EReceptDataTypeAggregation.PRESCRIBED)
    const [calculationMode, setCalculationMode] = useState<MedicinalUnitMode>(MedicinalUnitMode.PACKAGES)
    const [normalisationMode, setNormalisationMode] = useState<PopulationNormalisationMode>(PopulationNormalisationMode.ABSOLUTE)

    const [dateFrom, setDateFrom] = useState<Date | null>(null)
    const [dateTo, setDateTo] = useState<Date | null>(null)

    useEffect(() => {
        fetch("/okresy.json")
            .then(res => res.json())
            .then(setGeojsonData)
    }, [])

    const handleFetchData = useCallback(async () => {
        if (suklIds.length === 0) {
            setDistrictValuesByCode(null)
            setIgnoredProducts([])
            return
        }

        const payload = {
            medicinalProductIds: suklIds,
            aggregationType,
            calculationMode,
            normalisationMode,
            dateFrom: dateFrom ? format(dateFrom, "yyyy-MM") : null,
            dateTo: dateTo ? format(dateTo, "yyyy-MM") : null
        }

        try {
            const res = await fetch("/api/district-data", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            })

            const response: EReceptDistrictDataResponse = await res.json()
            setDistrictValuesByCode(response.districtValues)
            setIgnoredProducts(response.ignoredMedicineProducts)
        } catch (err) {
            console.error("❌ Chyba při načítání dat pro mapu:", err)
        }
    }, [suklIds, aggregationType, calculationMode, normalisationMode, dateFrom, dateTo])

    useEffect(() => {
        handleFetchData()
    }, [handleFetchData])

    return (
        <div className="map-tab">
            <div className="map-header-row">
                <EReceptFiltersPanel
                    dateFrom={dateFrom}
                    dateTo={dateTo}
                    onChangeDateFrom={setDateFrom}
                    onChangeDateTo={setDateTo}
                    calculationMode={calculationMode}
                    onChangeCalculationMode={setCalculationMode}
                    normalisationMode={normalisationMode}
                    onChangeNormalisationMode={setNormalisationMode}
                    aggregationType={aggregationType}
                    onChangeAggregationType={setAggregationType}
                />
            </div>

            <MedicineSelectorModal
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />

            <div className="map-content">
                <div className="map-sidebar">
                    <Button
                        variant="outlined"
                        onClick={() => setIsModalOpen(true)}
                        style={{ marginBottom: "1rem" }}
                    >
                        Vybrat léčiva
                    </Button>

                    <SelectedMedicinalProductSummary />
                </div>

                <div className="map-area">
                    {ignoredProducts.length > 0 && (
                        <p className="ignored-products">
                            Některé léčivé přípravky nebyly zahrnuty do výpočtu (např. chybí DDD):{" "}
                            {ignoredProducts.map(p => p.suklCode).join(", ")}
                        </p>
                    )}

                    {activeTab === "map" && geojsonData ? (
                        <DistrictMap
                            geojsonData={geojsonData}
                            districtData={districtValuesByCode ?? {}}
                            filter={aggregationType}
                        />
                    ) : activeTab === "sankey" ? (
                        <p className="map-loading">Sankey diagram se připravuje...</p>
                    ) : (
                        <p className="map-loading">Načítám podkladovou mapu...</p>
                    )}
                </div>

                {activeTab === "map" && (
                    <div className="map-summary-sidebar">
                        <SummaryTiles />
                    </div>
                )}
            </div>
        </div>
    )
}
