import React, { useEffect, useState } from "react"
import { useCart } from "./CartContext"
import { Drug } from "./DrugTable"
import "./DrugTable.css"

export function Cart() {
    const { cartIds, removeFromCart, clearCart } = useCart()
    const [drugs, setDrugs] = useState<Drug[]>([])

    useEffect(() => {
        if (cartIds.length === 0) {
            setDrugs([])
            return
        }

        fetch(`/api/medicinal-products/by-ids?${cartIds.map(id => `ids=${id}`).join("&")}`)
            .then(res => res.json())
            .then(data => setDrugs(data))
    }, [cartIds])

    return (
        <div className="drug-table-container">
            <h3>Košík</h3>
            {drugs.length === 0 ? (
                <p>Košík je prázdný.</p>
            ) : (
                <>
                    <table className="drug-table">
                        <thead>
                        <tr>
                            <th>Název</th>
                            <th>Doplněk názvu</th>
                            <th>SÚKL kód</th>
                            <th>Registrační číslo</th>
                            <th>ATC skupina</th>
                            <th>Akce</th>
                        </tr>
                        </thead>
                        <tbody>
                        {drugs.map((drug) => (
                            <tr key={drug.id}>
                                <td>{drug.name}</td>
                                <td>{drug.supplementaryInformation || "-"}</td>
                                <td>{drug.suklCode}</td>
                                <td>{drug.registrationNumber || "-"}</td>
                                <td>{drug.atcGroup ? `${drug.atcGroup.name} (${drug.atcGroup.code})` : "-"}</td>
                                <td>
                                    <button onClick={() => removeFromCart(Number(drug.id))}>
                                        Odebrat
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <div style={{ marginTop: "1rem", textAlign: "right" }}>
                        <button onClick={clearCart} style={{ backgroundColor: "#dc3545", color: "white", border: "none", borderRadius: "4px", padding: "0.5rem 1rem" }}>
                            Vyprázdnit košík
                        </button>
                    </div>
                </>
            )}
        </div>
    )
}
