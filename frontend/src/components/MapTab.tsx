import React, { useEffect, useState } from "react"
import { useCart } from "./CartContext"
import DistrictMap from "./DistrictMap"
import { FeatureCollection } from "geojson"

type FilterType = "prescribed" | "dispensed" | "difference"

export function MapTab() {
    const { cartIds } = useCart()
    const [geojsonData, setGeojsonData] = useState<FeatureCollection | null>(null)
    const [districtValuesByCode, setDistrictValuesByCode] = useState<Record<string, number> | null>(null)
    const [filterType, setFilterType] = useState<FilterType>("prescribed")

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
            filterType
        }

        console.log("➡️ Odesílám payload na BE:", payload)

        try {
            const res = await fetch("/api/district-data", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            })

            const data = await res.json()
            console.log("Data z BE:", data)
            setDistrictValuesByCode(data)
        } catch (err) {
            console.error("Chyba při načítání dat pro mapu:", err)
        }
    }

    return (
        <div className="map-tab">
            <h3>Vizualizace na mapě</h3>

            <div style={{ display: "flex", gap: "1rem", alignItems: "flex-end", marginBottom: "1rem" }}>
                <div>
                    <label>Typ dat:</label>
                    <select
                        value={filterType}
                        onChange={(e) => setFilterType(e.target.value as FilterType)}
                        style={{ padding: "0.4rem", fontSize: "1rem", marginLeft: "0.5rem" }}
                    >
                        <option value="prescribed">Předepsané</option>
                        <option value="dispensed">Vydané</option>
                        <option value="difference">Rozdíl</option>
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

            {geojsonData && districtValuesByCode ? (
                <DistrictMap
                    geojsonData={geojsonData}
                    districtData={districtValuesByCode}
                    filter={filterType}
                />
            ) : (
                <p style={{ color: "#666" }}>Zatím nejsou načtena žádná data.</p>
            )}
        </div>
    )
}
