import React, { useState } from "react"

export function TabSwitcher() {
    const [activeTab, setActiveTab] = useState<"map" | "sankey">("map")

    return (
        <div>
            <div style={{ display: "flex", gap: "1rem", marginBottom: "1rem" }}>
                <button
                    onClick={() => setActiveTab("map")}
                    style={{
                        fontWeight: activeTab === "map" ? "bold" : "normal",
                        backgroundColor: activeTab === "map" ? "#eee" : "transparent"
                    }}
                >
                    Mapa
                </button>
                <button
                    onClick={() => setActiveTab("sankey")}
                    style={{
                        fontWeight: activeTab === "sankey" ? "bold" : "normal",
                        backgroundColor: activeTab === "sankey" ? "#eee" : "transparent"
                    }}
                >
                    Sankey
                </button>
            </div>

            {activeTab === "map" ? (
                <div>🗺️ Zde bude mapa</div>
            ) : (
                <div>🔀 Zde bude Sankey diagram</div>
            )}
        </div>
    )
}
