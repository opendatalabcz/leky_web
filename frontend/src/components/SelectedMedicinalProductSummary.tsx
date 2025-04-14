import React from "react"
import { useUnifiedCart } from "./UnifiedCartContext"
import "./SelectedMedicinalProductSummary.css"

export const SelectedMedicinalProductSummary: React.FC = () => {
    const { drugs, groupedDrugs } = useUnifiedCart()

    const isEmpty = drugs.length === 0 && groupedDrugs.length === 0

    if (isEmpty) {
        return <p>Žádná léčiva nejsou vybrána.</p>
    }

    return (
        <div className="summary-container">
            <h4>Vybraná léčiva:</h4>

            {drugs.length > 0 && (
                <div className="summary-section">
                    <strong>Podle SÚKL kódu:</strong>
                    <ul>
                        {drugs.map((d) => (
                            <li key={d.id}>
                                {d.name} ({d.suklCode})
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {groupedDrugs.length > 0 && (
                <div className="summary-section">
                    <strong>Podle registračního čísla:</strong>
                    <ul>
                        {groupedDrugs.map((g) => (
                            <li key={g.registrationNumber}>
                                {g.names.join(", ")} ({g.registrationNumber})
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    )
}
