import React from "react"
import {
    Box,
    Typography,
    Stack,
    Paper,
    IconButton,
    Divider,
    Tooltip
} from "@mui/material"
import CloseIcon from "@mui/icons-material/Close"
import { useDrugCart } from "./drug-select-modal/DrugCartContext"

export const SelectedMedicinalProductSummary: React.FC = () => {
    const {
        drugs,
        groupedDrugs,
        removeSuklId,
        removeRegistrationNumber
    } = useDrugCart()

    const hasAny = drugs.length > 0 || groupedDrugs.length > 0

    return (
        <Paper variant="outlined" sx={{ overflow: "hidden", borderRadius: 2 }}>
            <Box
                sx={{
                    backgroundColor: "#e9eff6",
                    color: "#1f2b3d",
                    px: 2,
                    py: 1.5,
                    fontWeight: "bold",
                    fontSize: "1rem",
                    borderBottom: "1px solid #d0d7e2"
                }}
            >
                Vybrané léčiva:
            </Box>

            <Box sx={{ p: 2 }}>
                {!hasAny ? (
                    <Typography variant="body2" color="text.secondary">
                        Žádná léčiva nejsou vybrána.
                    </Typography>
                ) : (
                    <Stack spacing={3}>
                        <Box>
                            <Typography variant="subtitle2" gutterBottom>
                                Podle registračního čísla:
                            </Typography>
                            <Stack spacing={1}>
                                {groupedDrugs.length === 0 ? (
                                    <Typography variant="body2" color="text.secondary">
                                        -
                                    </Typography>
                                ) : (
                                    groupedDrugs.map((g) => (
                                        <Box
                                            key={g.registrationNumber}
                                            sx={{
                                                display: "flex",
                                                justifyContent: "space-between",
                                                alignItems: "center",
                                                flexWrap: "nowrap",
                                                px: 1.5,
                                                py: 1,
                                                borderRadius: 1,
                                                backgroundColor: "#f0f6fa",
                                                transition: "background-color 0.2s",
                                                overflow: "hidden",
                                                "&:hover": {
                                                    backgroundColor: "#d8e9f7"
                                                }
                                            }}
                                        >
                                            <Tooltip title={`${g.names.join(", ")} (${g.registrationNumber})`}>
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        whiteSpace: "nowrap",
                                                        overflow: "hidden",
                                                        textOverflow: "ellipsis",
                                                        mr: 1,
                                                        flexGrow: 1,
                                                        minWidth: 0
                                                    }}
                                                >
                                                    {g.names.join(", ")} ({g.registrationNumber})
                                                </Typography>
                                            </Tooltip>
                                            <IconButton
                                                size="small"
                                                color="error"
                                                onClick={() => removeRegistrationNumber(g.registrationNumber)}
                                            >
                                                <CloseIcon fontSize="small" />
                                            </IconButton>
                                        </Box>
                                    ))
                                )}
                            </Stack>
                        </Box>

                        <Divider />

                        <Box>
                            <Typography variant="subtitle2" gutterBottom>
                                Podle SÚKL kódu:
                            </Typography>
                            <Stack spacing={1}>
                                {drugs.length === 0 ? (
                                    <Typography variant="body2" color="text.secondary">
                                        -
                                    </Typography>
                                ) : (
                                    drugs.map((d) => (
                                        <Box
                                            key={d.id}
                                            sx={{
                                                display: "flex",
                                                justifyContent: "space-between",
                                                alignItems: "center",
                                                flexWrap: "nowrap",
                                                px: 1.5,
                                                py: 1,
                                                borderRadius: 1,
                                                backgroundColor: "#f0f6fa",
                                                transition: "background-color 0.2s",
                                                overflow: "hidden",
                                                "&:hover": {
                                                    backgroundColor: "#d8e9f7"
                                                }
                                            }}
                                        >
                                            <Tooltip title={`${d.name} (${d.suklCode})`}>
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        whiteSpace: "nowrap",
                                                        overflow: "hidden",
                                                        textOverflow: "ellipsis",
                                                        mr: 1,
                                                        flexGrow: 1,
                                                        minWidth: 0
                                                    }}
                                                >
                                                    {d.name} ({d.suklCode})
                                                </Typography>
                                            </Tooltip>
                                            <IconButton
                                                size="small"
                                                color="error"
                                                onClick={() => removeSuklId(Number(d.id))}
                                            >
                                                <CloseIcon fontSize="small" />
                                            </IconButton>
                                        </Box>
                                    ))
                                )}
                            </Stack>
                        </Box>
                    </Stack>
                )}
            </Box>
        </Paper>
    )
}