import React from "react"
import { ToggleButton, ToggleButtonGroup, Typography, Box } from "@mui/material"
import { MedicinalProductSearchMode } from "../../types/MedicinalProductSearchMode"

type Props = {
    searchMode: MedicinalProductSearchMode
    onChange: (mode: MedicinalProductSearchMode) => void
}

export const DrugSearchModeSwitch: React.FC<Props> = ({ searchMode, onChange }) => {
    const handleChange = (_: React.MouseEvent<HTMLElement>, newMode: MedicinalProductSearchMode | null) => {
        if (newMode !== null) {
            onChange(newMode)
        }
    }

    return (
        <Box mb={2} display="flex" alignItems="center" gap={2}>
            <Typography variant="subtitle2" sx={{ fontSize: "1rem", whiteSpace: "nowrap" }}>
                Zobrazit výsledky dle:
            </Typography>
            <ToggleButtonGroup
                value={searchMode}
                exclusive
                onChange={handleChange}
                sx={{
                    "& .MuiToggleButton-root": {
                        textTransform: "none",
                        fontWeight: 500,
                        fontSize: "0.95rem",
                        borderColor: "#ccc",
                        px: 2,
                        py: 0.5
                    },
                    "& .MuiToggleButton-root.Mui-selected": {
                        backgroundColor: "#34558a",
                        color: "white",
                        "&:hover": {
                            backgroundColor: "#2c4773"
                        }
                    }
                }}
            >
                <ToggleButton value={MedicinalProductSearchMode.SUKL_CODE}>
                    Dle kódu SÚKL
                </ToggleButton>
                <ToggleButton value={MedicinalProductSearchMode.REGISTRATION_NUMBER}>
                    Dle Registračního čísla
                </ToggleButton>
            </ToggleButtonGroup>
        </Box>
    )
}
