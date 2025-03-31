import React, { createContext, useContext, useEffect, useState } from "react"

type CartContextType = {
    cartIds: number[]
    addToCart: (id: number) => void
    removeFromCart: (id: number) => void
    clearCart: () => void
}

const CartContext = createContext<CartContextType | undefined>(undefined)

export const CartProvider = ({ children }: { children: React.ReactNode }) => {
    const [cartIds, setCartIds] = useState<number[]>([])

    useEffect(() => {
        const stored = localStorage.getItem("medicinal-product-cart")
        if (stored) {
            setCartIds(JSON.parse(stored))
        }
    }, [])

    useEffect(() => {
        localStorage.setItem("medicinal-product-cart", JSON.stringify(cartIds))
    }, [cartIds])

    const addToCart = (id: number) => {
        if (!cartIds.includes(id)) {
            setCartIds([...cartIds, id])
        }
    }

    const removeFromCart = (id: number) => {
        setCartIds(cartIds.filter((item) => item !== id))
    }

    const clearCart = () => {
        setCartIds([])
    }

    return (
        <CartContext.Provider value={{ cartIds, addToCart, removeFromCart, clearCart }}>
            {children}
        </CartContext.Provider>
    )
}

export const useCart = () => {
    const context = useContext(CartContext)
    if (!context) throw new Error("useCart must be used within CartProvider")
    return context
}
