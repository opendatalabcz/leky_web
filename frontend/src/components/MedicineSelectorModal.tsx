import React, { useState } from "react"
import {
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Button
} from "@mui/material"
import { DrugSearchSection } from "./DrugSearchSection"

interface Props {
    open: boolean
    onClose: () => void
}

export const MedicineSelectorModal: React.FC<Props> = ({ open, onClose }) => {
    const [refreshToken, setRefreshToken] = useState(0)
    const [selectedCount, setSelectedCount] = useState(0)
    const [addSelectedHandler, setAddSelectedHandler] = useState<() => void>(() => {})

    const handleDrugsAdded = () => {
        // Trigger refresh and close modal after adding
        setRefreshToken(prev => prev + 1)
        onClose()
    }

    const handleSelectionUpdate = (count: number, handler: () => void) => {
        setSelectedCount(count)
        setAddSelectedHandler(() => handler)
    }

    const handleAddSelected = () => {
        if (addSelectedHandler) {
            addSelectedHandler()
        }
    }

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="xl">
            <DialogTitle>Výběr léčiv</DialogTitle>

            <DialogContent dividers>
                <DrugSearchSection
                    onAddSelected={handleDrugsAdded}
                    refreshToken={refreshToken}
                    onCloseModal={onClose}
                    onSelectionUpdate={handleSelectionUpdate}
                />
            </DialogContent>

            {selectedCount > 0 && (
                <DialogActions>
                    <Button onClick={handleAddSelected} variant="contained" color="primary">
                        Přidat vybrané ({selectedCount})
                    </Button>
                </DialogActions>
            )}
        </Dialog>
    )
}
