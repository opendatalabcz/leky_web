// UnifiedCartContext.tsx

import React, { createContext, useContext, useEffect, useState } from "react"
import { Drug } from "./DrugTableNew"
import { GroupedDrug } from "./GroupedDrugTableNew"

type UnifiedCartContextType = {
  suklIds: number[]
  registrationNumbers: string[]
  drugs: Drug[]
  groupedDrugs: GroupedDrug[]
  addSuklId: (id: number) => void
  removeSuklId: (id: number) => void
  addRegistrationNumber: (regNumber: string) => void
  removeRegistrationNumber: (regNumber: string) => void
  clearCart: () => void
}

const UnifiedCartContext = createContext<UnifiedCartContextType | undefined>(undefined)

export const UnifiedCartProvider = ({ children }: { children: React.ReactNode }) => {
  const [suklIds, setSuklIds] = useState<number[]>([])
  const [registrationNumbers, setRegistrationNumbers] = useState<string[]>([])

  const [drugs, setDrugs] = useState<Drug[]>([])
  const [groupedDrugs, setGroupedDrugs] = useState<GroupedDrug[]>([])

  // sync from localStorage on mount
  useEffect(() => {
    const stored = localStorage.getItem("unified-medicine-cart")
    if (stored) {
      const parsed = JSON.parse(stored)
      setSuklIds(parsed.suklIds || [])
      setRegistrationNumbers(parsed.registrationNumbers || [])
    }
  }, [])

  // persist to localStorage
  useEffect(() => {
    localStorage.setItem("unified-medicine-cart", JSON.stringify({ suklIds, registrationNumbers }))
  }, [suklIds, registrationNumbers])

  // fetch detailed drugs info
  useEffect(() => {
    if (suklIds.length === 0) {
      setDrugs([])
      return
    }

    fetch(`/api/medicinal-products/by-ids?${suklIds.map(id => `ids=${id}`).join("&")}`)
      .then(res => res.json())
      .then(setDrugs)
      .catch(console.error)
  }, [suklIds])

  useEffect(() => {
    if (registrationNumbers.length === 0) {
      setGroupedDrugs([])
      return
    }

    const params = new URLSearchParams()
    registrationNumbers.forEach(reg => params.append("regNumbers", reg))

    fetch(`/api/medicinal-products/grouped-by-reg-numbers?${params.toString()}`)
      .then(res => res.json())
      .then(setGroupedDrugs)
      .catch(console.error)
  }, [registrationNumbers])

  const addSuklId = (id: number) => {
    setSuklIds(prev => (prev.includes(id) ? prev : [...prev, id]))
  }

  const removeSuklId = (id: number) => {
    setSuklIds(prev => prev.filter(i => i !== id))
  }

  const addRegistrationNumber = (reg: string) => {
    setRegistrationNumbers(prev => (prev.includes(reg) ? prev : [...prev, reg]))
  }

  const removeRegistrationNumber = (reg: string) => {
    setRegistrationNumbers(prev => prev.filter(r => r !== reg))
  }

  const clearCart = () => {
    setSuklIds([])
    setRegistrationNumbers([])
  }

  return (
    <UnifiedCartContext.Provider
      value={{
        suklIds,
        registrationNumbers,
        drugs,
        groupedDrugs,
        addSuklId,
        removeSuklId,
        addRegistrationNumber,
        removeRegistrationNumber,
        clearCart
      }}
    >
      {children}
    </UnifiedCartContext.Provider>
  )
}

export const useUnifiedCart = () => {
  const context = useContext(UnifiedCartContext)
  if (!context) throw new Error("useUnifiedCart must be used within UnifiedCartProvider")
  return context
}
