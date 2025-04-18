import React, { useEffect, useState } from "react"
import { MedicinalProductFilterValues } from "../types/MedicinalProductFilterValues"
import { SubstanceSelect } from "./SubstanceSelect"
import { Box, FormControl, InputLabel, MenuItem, Select, TextField, Button } from "@mui/material"

type Props = {
    filters: MedicinalProductFilterValues
    onChange: (updated: MedicinalProductFilterValues) => void
    onSearchClick: () => void
}

type AtcGroupOption = {
    id: number
    name: string
    code: string
}

export const MedicinalProductFilters: React.FC<Props> = ({ filters, onChange, onSearchClick }) => {
    const [atcOptions, setAtcOptions] = useState<AtcGroupOption[]>([])

    useEffect(() => {
        fetch("/api/atc-groups")
            .then(res => res.json())
            .then(data => setAtcOptions(data))
    }, [])

    return (
        <Box
            component="form"
            onSubmit={(e) => {
                e.preventDefault()
                onSearchClick()
            }}
            display="flex"
            flexWrap="wrap"
            gap={2}
            mt={2}
            mb={3}
        >
            <Box flex={1} minWidth={200}>
                <TextField
                    label="Název / SÚKL / Registrační číslo"
                    value={filters.medicinalProductQuery}
                    onChange={(e) =>
                        onChange({ ...filters, medicinalProductQuery: e.target.value })
                    }
                    fullWidth
                    size="small"
                    placeholder="např. Paralen / 272209 / 54/432/01-C"
                />
            </Box>

            <Box flex={1} minWidth={200}>
                <FormControl fullWidth size="small">
                    <InputLabel id="atc-group-label">ATC skupina</InputLabel>
                    <Select
                        labelId="atc-group-label"
                        value={filters.atcGroupId ?? ""}
                        label="ATC skupina"
                        onChange={(e) =>
                            onChange({
                                ...filters,
                                atcGroupId: e.target.value ? Number(e.target.value) : null
                            })
                        }
                    >
                        <MenuItem value="">-- Vyberte ATC skupinu --</MenuItem>
                        {atcOptions.map((atc) => (
                            <MenuItem key={atc.id} value={atc.id}>
                                {atc.name} ({atc.code})
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </Box>

            <Box flex={1} minWidth={200}>
                <SubstanceSelect
                    selectedSubstanceId={filters.substanceId}
                    onChange={(id) => onChange({ ...filters, substanceId: id })}
                />
            </Box>

            <Box display="flex" alignItems="center" minWidth={150}>
                <Button
                    variant="contained"
                    color="primary"
                    type="submit"
                    sx={{ whiteSpace: "nowrap" }}
                >
                    Vyhledat
                </Button>
            </Box>
        </Box>
    )
}
