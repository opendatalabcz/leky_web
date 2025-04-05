import React, { useState } from "react"
import { MapTab } from "./MapTab"
import "./TabSwitcher.css"

export function TabSwitcher() {
    const [activeTab, setActiveTab] = useState<"map" | "sankey">("map")

    return (
        <div>
            <div className="tab-buttons">
                <button
                    className={`tab-button ${activeTab === "map" ? "active" : ""}`}
                    onClick={() => setActiveTab("map")}
                >
                    Mapa
                </button>
                <button
                    className={`tab-button ${activeTab === "sankey" ? "active" : ""}`}
                    onClick={() => setActiveTab("sankey")}
                >
                    Sankey
                </button>
            </div>

            <div style={{ marginTop: "1rem" }}>
                {activeTab === "map" ? <MapTab /> : <div>Sankey bude tady</div>}
            </div>
        </div>
    )
}
