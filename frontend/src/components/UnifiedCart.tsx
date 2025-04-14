// UnifiedCart.tsx

import React from "react"
import { useUnifiedCart } from "./UnifiedCartContext"
import "./UnifiedCart.css"

export const UnifiedCart: React.FC = () => {
  const {
    drugs,
    groupedDrugs,
    removeSuklId,
    removeRegistrationNumber,
    clearCart
  } = useUnifiedCart()

  return (
    <div style={{ marginTop: "1rem" }}>
      <h3>Vybrané léčiva</h3>

      {drugs.length === 0 && groupedDrugs.length === 0 && (
        <p>Nemáte vybrané žádné léčiva.</p>
      )}

      {drugs.length > 0 && (
        <>
          <h4>Podle SÚKL kódu</h4>
          <table className="drug-table">
            <thead>
              <tr>
                <th>SÚKL kód</th>
                <th>Název</th>
                <th>ATC</th>
                <th>Akce</th>
              </tr>
            </thead>
            <tbody>
              {drugs.map(drug => (
                <tr key={drug.id}>
                  <td>{drug.suklCode}</td>
                  <td>{drug.name}</td>
                  <td>{drug.atcGroup?.code || "-"}</td>
                  <td>
                    <button onClick={() => removeSuklId(Number(drug.id))}>Odebrat</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      {groupedDrugs.length > 0 && (
        <>
          <h4>Podle Registračního čísla</h4>
          <table className="drug-table">
            <thead>
              <tr>
                <th>Reg. číslo</th>
                <th>Názvy</th>
                <th>SÚKL kódy</th>
                <th>Akce</th>
              </tr>
            </thead>
            <tbody>
              {groupedDrugs.map(group => (
                <tr key={group.registrationNumber}>
                  <td>{group.registrationNumber}</td>
                  <td>{group.names.join(", ")}</td>
                  <td>{group.suklCodes.length}</td>
                  <td>
                    <button onClick={() => removeRegistrationNumber(group.registrationNumber)}>
                      Odebrat
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      {(drugs.length > 0 || groupedDrugs.length > 0) && (
        <div style={{ marginTop: "1rem", textAlign: "right" }}>
          <button onClick={clearCart} className="clear-cart-button">
            Vyprázdnit košík
          </button>
        </div>
      )}
    </div>
  )
}
