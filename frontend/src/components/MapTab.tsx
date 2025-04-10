import React, { useEffect, useState } from "react"
import { useCart } from "./CartContext"
import DistrictMap from "./DistrictMap"
import { CalculationMode } from "../types/CalculationMode"
import { FeatureCollection } from "geojson"
import { VisualizationSettings } from "./VisualizationSettings"
import { format } from "date-fns"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"
import { NormalisationMode } from "../types/NormalisationMode"

type MedicineProductInfo = {
    id: number
    suklCode: string
}

type EReceptDistrictDataResponse = {
    aggregationType: EReceptDataTypeAggregation
    calculationMode: CalculationMode
    normalisationMode: NormalisationMode
    dateFrom: string | null
    dateTo: string | null
    districtValues: Record<string, number>
    includedMedicineProducts: MedicineProductInfo[]
    ignoredMedicineProducts: MedicineProductInfo[]
}

export function MapTab() {
    const { cartIds } = useCart()
    const [geojsonData, setGeojsonData] = useState<FeatureCollection | null>(null)
    const [districtValuesByCode, setDistrictValuesByCode] = useState<Record<string, number> | null>(null)
    const [ignoredProducts, setIgnoredProducts] = useState<MedicineProductInfo[]>([])

    const [aggregationType, setAggregationType] = useState<EReceptDataTypeAggregation>(EReceptDataTypeAggregation.PRESCRIBED)
    const [calculationMode, setCalculationMode] = useState<CalculationMode>(CalculationMode.UNITS)
    const [normalisationMode, setNormalisationMode] = useState<NormalisationMode>(NormalisationMode.ABSOLUTE)

    const [dateFrom, setDateFrom] = useState<Date | null>(null)
    const [dateTo, setDateTo] = useState<Date | null>(null)

    useEffect(() => {
        fetch("/okresy.json")
            .then(res => res.json())
            .then(setGeojsonData)
    }, [])

    const handleFetchData = async () => {
        if (cartIds.length === 0) {
            alert("Košík je prázdný.")
            return
        }

        const payload = {
            medicinalProductIds: cartIds,
            aggregationType,
            calculationMode,
            normalisationMode,
            dateFrom: dateFrom ? format(dateFrom, "yyyy-MM") : null,
            dateTo: dateTo ? format(dateTo, "yyyy-MM") : null
        }

        console.log("➡️ Odesílám payload na BE:", payload)

        try {
            const res = await fetch("/api/district-data", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            })

            const response: EReceptDistrictDataResponse = await res.json()
            console.log("✅ Data z BE:", response)

            setDistrictValuesByCode(response.districtValues)
            setIgnoredProducts(response.ignoredMedicineProducts)
        } catch (err) {
            console.error("❌ Chyba při načítání dat pro mapu:", err)
        }
    }

    return (
        <div className="map-tab">
            <h3>Vizualizace na mapě</h3>

            <VisualizationSettings
                dateFrom={dateFrom}
                dateTo={dateTo}
                onChangeDateFrom={setDateFrom}
                onChangeDateTo={setDateTo}
                calculationMode={calculationMode}
                onChangeCalculationMode={setCalculationMode}
                normalisationMode={normalisationMode}
                onChangeNormalisationMode={setNormalisationMode}
            />

            <div style={{ display: "flex", gap: "1rem", alignItems: "flex-end", marginBottom: "1rem" }}>
                <div>
                    <label>Typ dat:</label>
                    <select
                        value={aggregationType}
                        onChange={(e) => setAggregationType(e.target.value as EReceptDataTypeAggregation)}
                        style={{ padding: "0.4rem", fontSize: "1rem", marginLeft: "0.5rem" }}
                    >
                        <option value={EReceptDataTypeAggregation.PRESCRIBED}>Předepsané</option>
                        <option value={EReceptDataTypeAggregation.DISPENSED}>Vydané</option>
                        <option value={EReceptDataTypeAggregation.DIFFERENCE}>Rozdíl</option>
                    </select>
                </div>

                <button
                    onClick={handleFetchData}
                    style={{
                        padding: "0.5rem 1.2rem",
                        backgroundColor: "#007bff",
                        color: "white",
                        border: "none",
                        borderRadius: "4px",
                        cursor: "pointer",
                        fontWeight: 500
                    }}
                >
                    Zobrazit
                </button>
            </div>

            {ignoredProducts.length > 0 && (
                <p style={{ color: "#b00" }}>
                    Některé léčivé přípravky nebyly zahrnuty do výpočtu (např. chybí DDD):{" "}
                    {ignoredProducts.map(p => p.suklCode).join(", ")}
                </p>
            )}

            {geojsonData && districtValuesByCode ? (
                <DistrictMap
                    geojsonData={geojsonData}
                    districtData={districtValuesByCode}
                    filter={aggregationType}
                />
            ) : (
                <p style={{ color: "#666" }}>Zatím nejsou načtena žádná data.</p>
            )}
        </div>
    )
}
