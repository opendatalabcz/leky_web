import React from "react"
import {Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography} from "@mui/material"
import {DrugSearchSection} from "./DrugSearchSection"
import {UnifiedCart} from "./UnifiedCart"

type Props = {
    open: boolean
    onClose: () => void
}

export const MedicineSelectorModal: React.FC<Props> = ({ open, onClose }) => {
    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="xl">
            <DialogTitle>Výběr léčiv</DialogTitle>

            <DialogContent dividers>
                <Box mb={3}>
                    <DrugSearchSection />
                </Box>

                <Box>
                    <UnifiedCart />
                </Box>
            </DialogContent>

            <DialogActions>
                <Button onClick={onClose} variant="contained" color="primary">
                    Výběr léčiv hotov
                </Button>
            </DialogActions>
        </Dialog>
    )
}
