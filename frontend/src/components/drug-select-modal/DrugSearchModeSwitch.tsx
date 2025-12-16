import React from "react"
import {
    ToggleButton,
    ToggleButtonGroup,
    Typography,
    Box,
    Tooltip
} from "@mui/material"
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined"
import { MedicinalProductSearchMode } from "../../types/MedicinalProductSearchMode"

type Props = {
    searchMode: MedicinalProductSearchMode
    onChange: (mode: MedicinalProductSearchMode) => void
}

export const DrugSearchModeSwitch: React.FC<Props> = ({ searchMode, onChange }) => {
    const handleChange = (
        _: React.MouseEvent<HTMLElement>,
        newMode: MedicinalProductSearchMode | null
    ) => {
        if (newMode !== null) {
            onChange(newMode)
        }
    }

    return (
        <Box sx={{ width: "100%" }}>
            <Box
                display="flex"
                alignItems="center"
                gap={2}
                mb={1}
                flexWrap="wrap"
            >
                <Typography
                    variant="subtitle2"
                    sx={{ fontSize: "1rem", whiteSpace: "nowrap" }}
                >
                    Zobrazit výsledky hledání dle:
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
                            py: 0.5,
                            whiteSpace: "nowrap",
                            height: 36,
                            display: "flex",
                            alignItems: "center",
                            gap: 0.5
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
                    <ToggleButton value={MedicinalProductSearchMode.REGISTRATION_NUMBER}>
                        Registračního čísla
                        <Tooltip
                            arrow
                            placement="bottom"
                            title={
                                <>
                                    <strong>Registrační číslo</strong> označuje léčivý přípravek jako celek
                                    (v jedné síle a lékové formě, např. „Paralen 500 mg tablety“).<br /><br />

                                    Různé varianty balení tohoto léku
                                    (např. odlišné velikosti balení nebo jiné provedení balení)
                                    jsou v tomto režimu <strong>sloučeny dohromady</strong>.<br /><br />

                                    Zvolte tuto možnost, pokud vás zajímá léčivý přípravek obecně
                                    bez rozlišení jednotlivých balení.
                                </>
                            }
                        >
                            <InfoOutlinedIcon
                                sx={{
                                    fontSize: 16,
                                    ml: 0.5,
                                    opacity: 0.8
                                }}
                            />
                        </Tooltip>
                    </ToggleButton>

                    <ToggleButton value={MedicinalProductSearchMode.SUKL_CODE}>
                        Kódu SÚKL
                        <Tooltip
                            arrow
                            placement="bottom"
                            title={
                                <>
                                    <strong>Kód SÚKL</strong> označuje konkrétní variantu balení
                                    léčivého přípravku.<br /><br />

                                    Každá varianta balení má vlastní kód SÚKL
                                    (např. jiná velikost balení nebo jiné provedení balení).<br /><br />

                                    Zvolte tuto možnost, pokud vás zajímají jednotlivá balení
                                    léčivého přípravku samostatně.
                                </>
                            }
                        >
                            <InfoOutlinedIcon
                                sx={{
                                    fontSize: 16,
                                    ml: 0.5,
                                    opacity: 0.8
                                }}
                            />
                        </Tooltip>
                    </ToggleButton>
                </ToggleButtonGroup>
            </Box>
        </Box>
    )
}
