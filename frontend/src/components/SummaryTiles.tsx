import React from "react"
import "./SummaryTiles.css"

export const SummaryTiles: React.FC = () => {
    return (
        <div className="summary-tiles-vertical">
            <div className="tile">
                <div className="label">Předepsané</div>
                <div className="value">1 200 000</div>
            </div>
            <div className="tile">
                <div className="label">Vydané</div>
                <div className="value">1 100 000</div>
            </div>
            <div className="tile">
                <div className="label">Rozdíl</div>
                <div className="value">100 000</div>
            </div>
            <div className="tile">
                <div className="label">% Rozdíl</div>
                <div className="value">8.3%</div>
            </div>
        </div>
    )
}
