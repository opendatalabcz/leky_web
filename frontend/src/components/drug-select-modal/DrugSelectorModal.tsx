import React, { useState } from "react"
import {
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Button
} from "@mui/material"
import { DrugSearchSection } from "./DrugSearchSection"
import { useMediaQuery, useTheme } from "@mui/material";

interface Props {
    open: boolean
    onClose: () => void
}

export const DrugSelectorModal: React.FC<Props> = ({ open, onClose }) => {
    const theme = useTheme();
    const fullScreen = useMediaQuery(theme.breakpoints.down("sm"));    const [refreshToken, setRefreshToken] = useState(0)
    const [selectedCount, setSelectedCount] = useState(0)
    const [addSelectedHandler, setAddSelectedHandler] = useState<() => void>(() => {})

    const handleDrugsAdded = () => {
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
        <Dialog
            open={open}
            onClose={onClose}
            fullScreen={fullScreen}
            fullWidth
            maxWidth={fullScreen ? "xs" : "lg"}
        >
            <DialogTitle>Výběr léčiv</DialogTitle>

            <DialogContent
                dividers
                sx={{
                    width: '100%',
                    maxWidth: '1200px',
                    mx: 'auto',
                    boxSizing: 'border-box',
                    p: { xs: 1, sm: 2 },
                    overflowX: 'auto'
                }}
            >
                <DrugSearchSection
                    onAddSelected={handleDrugsAdded}
                    refreshToken={refreshToken}
                    onCloseModal={onClose}
                    onSelectionUpdate={handleSelectionUpdate}
                />
            </DialogContent>

            {selectedCount > 0 && (
                <DialogActions
                    sx={{
                        justifyContent: 'flex-end',
                        pr: 4
                    }}
                >
                    <Button
                        onClick={handleAddSelected}
                        variant="contained"
                        color="primary"
                    >
                        Přidat vybrané ({selectedCount})
                    </Button>
                </DialogActions>

            )}
        </Dialog>
    )
}
