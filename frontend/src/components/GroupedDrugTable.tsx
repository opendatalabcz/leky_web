import React from "react"
import "./DrugTable.css"

type GroupedDrug = {
    registrationNumber: string
    suklCodes: string[]
    names: string[]
    strengths: string[]
    dosageForms: { id: number; code: string; name?: string }[]
    administrationRoutes: { id: number; code: string; name?: string }[]
    atcGroups: { id: number; code: string; name: string }[]
}

type Props = {
    data: GroupedDrug[]
    loading: boolean
}

export function GroupedDrugTable({ data, loading }: Props) {
    return (
        <div className="drug-table-container">
            <h3>Výsledky vyhledávání (podle registračního čísla)</h3>
            {loading && <p>Načítání dat...</p>}
            {!loading && data.length === 0 && <p>Žádné výsledky</p>}

            {!loading && data.length > 0 && (
                <table className="drug-table">
                    <thead>
                    <tr>
                        <th>Registrační číslo</th>
                        <th>Název</th>
                        <th>Síla</th>
                        <th>Léková forma</th>
                        <th>Cesta podání</th>
                        <th>ATC skupina</th>
                        <th>Počet SÚKL kódů</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data.map((drug) => (
                        <tr key={drug.registrationNumber}>
                            <td>{drug.registrationNumber}</td>
                            <td>{renderList(drug.names)}</td>
                            <td>{renderList(drug.strengths)}</td>
                            <td>{renderObjectList(drug.dosageForms)}</td>
                            <td>{renderObjectList(drug.administrationRoutes)}</td>
                            <td>{renderObjectList(drug.atcGroups)}</td>
                            <td>{drug.suklCodes.length}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}

function renderList(items: string[]) {
    if (items.length === 1) return items[0]
    return (
        <ul style={{ margin: 0, paddingLeft: "1.2rem" }}>
            {items.map((item, idx) => (
                <li key={idx}>{item}</li>
            ))}
        </ul>
    )
}

function renderObjectList(items: { code: string; name?: string }[]) {
    if (items.length === 1) {
        const { code, name } = items[0]
        return name ? `${name} (${code})` : code
    }

    return (
        <ul style={{ margin: 0, paddingLeft: "1.2rem" }}>
            {items.map((item, idx) => (
                <li key={idx}>
                    {item.name ? `${item.name} (${item.code})` : item.code}
                </li>
            ))}
        </ul>
    )
}
