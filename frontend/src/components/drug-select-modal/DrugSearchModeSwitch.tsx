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
        <Box
            mb={2}
            display="flex"
            flexDirection={{ xs: "column", sm: "row" }}
            alignItems={{ xs: "stretch", sm: "center" }}
            gap={2}
            sx={{ width: '100%' }}
        >
            <Typography
                variant="subtitle2"
                sx={{
                    fontSize: "1rem",
                    whiteSpace: "nowrap"
                }}
            >
                Zobrazit výsledky dle:
            </Typography>

            <ToggleButtonGroup
                value={searchMode}
                exclusive
                onChange={handleChange}
                sx={{
                    width: { xs: "100%", sm: "auto" },
                    "& .MuiToggleButton-root": {
                        textTransform: "none",
                        fontWeight: 500,
                        fontSize: "0.95rem",
                        borderColor: "#ccc",
                        px: 2,
                        py: 0.5,
                        flex: { xs: 1, sm: "unset" },
                        whiteSpace: "nowrap",
                        height: 36,
                        lineHeight: 1
                    },
                    "& .MuiToggleButton-root.Mui-selected": {
                        backgroundColor: "#34558a",
                        color: "white",
                        "&:hover": {
                            backgroundColor: "#2c4773"
                        }
                    }
                }}
                fullWidth // celé tlačítko fullWidth na xs
            >
                <ToggleButton value={MedicinalProductSearchMode.REGISTRATION_NUMBER}>
                    Registračního čísla
                </ToggleButton>
                <ToggleButton value={MedicinalProductSearchMode.SUKL_CODE}>
                    Kódu SÚKL
                </ToggleButton>
            </ToggleButtonGroup>
        </Box>
    )
}
