import React from "react"

type Props = {
    activeTab: "map" | "sankey"
    onChangeTab: (tab: "map" | "sankey") => void
}

export const TabSwitcher: React.FC<Props> = ({ activeTab, onChangeTab }) => {
    return (
        <div className="tab-switcher">
            <button
                className={activeTab === "map" ? "active" : ""}
                onClick={() => onChangeTab("map")}
            >
                Mapa
            </button>
            <button
                className={activeTab === "sankey" ? "active" : ""}
                onClick={() => onChangeTab("sankey")}
            >
                Sankey
            </button>
        </div>
    )
}
