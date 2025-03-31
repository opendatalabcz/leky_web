import React, { useState } from "react"
import "./TabSwitcher.css"

export function TabSwitcher() {
    const [activeTab, setActiveTab] = useState<"map" | "sankey">("map")

    return (
        <div className="tab-switcher">
            <div className="tab-buttons">
                <button
                    className={activeTab === "map" ? "tab-button active" : "tab-button"}
                    onClick={() => setActiveTab("map")}
                >
                    Mapa
                </button>
                <button
                    className={activeTab === "sankey" ? "tab-button active" : "tab-button"}
                    onClick={() => setActiveTab("sankey")}
                >
                    Sankey
                </button>
            </div>

            <div className="tab-content">
                {activeTab === "map" ? (
                    <div>🗺️ Zde bude mapa</div>
                ) : (
                    <div>🔀 Zde bude Sankey diagram</div>
                )}
            </div>
        </div>
    )
}
