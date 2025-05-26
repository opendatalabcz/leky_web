import {
    Box,
    TextField,
    Button,
} from "@mui/material";
import { MedicinalProductFilterValues } from "../../types/MedicinalProductFilterValues";
import { SubstanceSelect } from "./SubstanceSelect";
import { AtcGroupSelect } from "./AtcGroupSelect";
import React from "react";

type Props = {
    filters: MedicinalProductFilterValues;
    onChange: (updated: MedicinalProductFilterValues) => void;
    onSearchClick: () => void;
};

export const DrugFilters: React.FC<Props> = ({
                                                 filters,
                                                 onChange,
                                                 onSearchClick
                                             }) => {
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
        {/* --- full-text --- */}
            <Box flex={{ xs: "1 1 100%", sm: "1 1 300px" }} minWidth={200} maxWidth={400}>
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

            {/* --- ATC Group --- */}
            <Box flex={{ xs: "1 1 100%", sm: "1 1 200px" }} minWidth={200} maxWidth={300}>
                <AtcGroupSelect
                    selectedAtcGroupId={filters.atcGroupId}
                    onChange={(id) => onChange({ ...filters, atcGroupId: id })}
                />
            </Box>

            {/* --- Substance --- */}
            <Box flex={{ xs: "1 1 100%", sm: "1 1 200px" }} minWidth={200} maxWidth={300}>
                <SubstanceSelect
                    selectedSubstanceId={filters.substanceId}
                    onChange={(id) => onChange({ ...filters, substanceId: id })}
                />
            </Box>

            {/* --- Search button --- */}
            <Box
                display="flex"
                alignItems="center"
                flex={{ xs: "1 1 100%", sm: "0 0 auto" }}
                minWidth={150}
            >
                <Button
                    variant="contained"
                    color="primary"
                    type="submit"
                    sx={{
                        whiteSpace: "nowrap",
                        width: { xs: "100%", sm: "auto" }
                    }}
                >
                    Vyhledat
                </Button>
            </Box>
        </Box>
    );
};
