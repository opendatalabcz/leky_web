import React from "react"
import { FilterValues } from "./Filters"

type DrugTableProps = {
    filters: FilterValues
    onSelectDrug: (drugId: string) => void
}

export function DrugTable({ filters, onSelectDrug }: DrugTableProps) {
    const dummyData = [
        { id: "1", name: "Paralen 500 mg" },
        { id: "2", name: "Ibalgin 400 mg" }
    ]

    return (
        <div>
            <h3>Výsledky vyhledávání</h3>
            <table>
                <thead>
                <tr>
                    <th>Název léčiva</th>
                    <th>Akce</th>
                </tr>
                </thead>
                <tbody>
                {dummyData.map((drug) => (
                    <tr key={drug.id}>
                        <td>{drug.name}</td>
                        <td>
                            <button onClick={() => onSelectDrug(drug.id)}>Přidat do košíku</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    )
}
