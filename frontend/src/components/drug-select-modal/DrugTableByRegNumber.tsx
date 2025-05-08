import React, { useEffect, useState } from 'react';
import {
    DataGrid,
    GridColDef,
    GridRenderCellParams,
    GridRowSelectionModel,
} from '@mui/x-data-grid';
import { Box, Button } from '@mui/material';
import { MedicinalProductFilterValues } from '../../types/MedicinalProductFilterValues';
import { useDrugCart } from './DrugCartContext';

export type GroupedDrug = {
    registrationNumber: string;
    names: string[];
    suklCodes: string[];
    strengths: string[];
    dosageForms: { code: string; name?: string }[];
    administrationRoutes: { code: string; name?: string }[];
    atcGroups: { code: string; name: string }[];
};

type PagedResponse<T> = {
    content: T[];
    totalPages: number;
    page: number;
    size: number;
    totalElements: number;
};

type Props = {
    filters: MedicinalProductFilterValues;
    triggerSearch: boolean;
    onSearchComplete: () => void;
    filtersVersion: number;
    setTriggerSearch: (val: boolean) => void;
    onAddOne: (regNum: string) => void;
    onAddSelected?: () => void;
    onSelectionUpdate?: (count: number, selectedIds: string[]) => void;
};

export const DrugTableByRegNumber: React.FC<Props> = ({
                                                          filters,
                                                          triggerSearch,
                                                          onSearchComplete,
                                                          filtersVersion,
                                                          setTriggerSearch,
                                                          onAddOne,
                                                          onAddSelected,
                                                          onSelectionUpdate,
                                                      }) => {
    const pageSize = 5;

    const [data, setData] = useState<GroupedDrug[]>([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [loading, setLoading] = useState(false);
    const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>([]);

    const { addRegistrationNumber } = useDrugCart();

    useEffect(() => setCurrentPage(0), [filtersVersion]);

    useEffect(() => {
        if (!triggerSearch) return;

        const fetchData = async () => {
            setLoading(true);
            try {
                const params = new URLSearchParams();
                if (filters.atcGroupId) params.append('atcGroupId', filters.atcGroupId.toString());
                if (filters.substanceId) params.append('substanceId', filters.substanceId.toString());
                if (filters.medicinalProductQuery) params.append('query', filters.medicinalProductQuery);
                if (filters.period) params.append('period', filters.period);
                params.append('page', currentPage.toString());
                params.append('size', pageSize.toString());

                const res = await fetch(`/api/medicinal-products/grouped-by-reg-number?${params.toString()}`);
                const json: PagedResponse<GroupedDrug> = await res.json();

                setData(json.content);
                setTotalElements(json.totalElements);
            } catch (e) {
                console.error('Chyba při načítání:', e);
            } finally {
                setLoading(false);
                onSearchComplete();
            }
        };

        fetchData();
    }, [triggerSearch, filters, currentPage]);

    const handleAddSelected = () => {
        const ids = selectionModel as string[];
        ids.forEach(addRegistrationNumber);
        onAddSelected?.();
    };

    useEffect(() => {
        const ids = selectionModel as string[];
        onSelectionUpdate?.(ids.length, ids);
    }, [selectionModel, onSelectionUpdate]);

    const columns: GridColDef<GroupedDrug>[] = [
        { field: 'registrationNumber', headerName: 'Registrační číslo', flex: 1 },
        {
            field: 'names',
            headerName: 'Název',
            flex: 2,
            renderCell: (params: GridRenderCellParams<GroupedDrug>) =>
                params.row.names.length > 0 ? params.row.names.join(', ') : '-',
        },
        {
            field: 'strengths',
            headerName: 'Síla',
            flex: 1,
            renderCell: (params: GridRenderCellParams<GroupedDrug>) =>
                params.row.strengths.length > 0 ? params.row.strengths.join(', ') : '-',
        },
        {
            field: 'dosageForms',
            headerName: 'Léková forma',
            flex: 1.5,
            renderCell: (params: GridRenderCellParams<GroupedDrug>) =>
                params.row.dosageForms.length > 0
                    ? params.row.dosageForms.map((d) => d.name ?? d.code).join(', ')
                    : '-',
        },
        {
            field: 'administrationRoutes',
            headerName: 'Cesta podání',
            flex: 1.5,
            renderCell: (params: GridRenderCellParams<GroupedDrug>) =>
                params.row.administrationRoutes.length > 0
                    ? params.row.administrationRoutes.map((r) => r.name ?? r.code).join(', ')
                    : '-',
        },
        {
            field: 'atcGroups',
            headerName: 'ATC skupina',
            flex: 2,
            renderCell: (params: GridRenderCellParams<GroupedDrug>) =>
                params.row.atcGroups.length > 0
                    ? params.row.atcGroups.map((a) => `${a.name} (${a.code})`).join(', ')
                    : '-',
        },
        {
            field: 'suklCodes',
            headerName: 'Počet kódů',
            flex: 1,
            renderCell: (params: GridRenderCellParams<GroupedDrug>) => params.row.suklCodes.length,
        },
        {
            field: 'action',
            headerName: 'Akce',
            flex: 1,
            renderCell: (params: GridRenderCellParams<GroupedDrug>) => (
                <Button
                    variant="outlined"
                    size="small"
                    onClick={() => onAddOne(params.row.registrationNumber)}
                >
                    Přidat
                </Button>
            ),
        },
    ];

    return (
        <Box>
            <DataGrid
                autoHeight
                rows={data}
                columns={columns}
                rowCount={totalElements}
                pageSizeOptions={[pageSize]}
                paginationModel={{ page: currentPage, pageSize }}
                paginationMode="server"
                checkboxSelection
                disableRowSelectionOnClick
                onRowSelectionModelChange={setSelectionModel}
                loading={loading}
                onPaginationModelChange={({ page }) => {
                    setCurrentPage(page);
                    setTriggerSearch(true);
                }}
                getRowId={(row) => row.registrationNumber}
                disableColumnMenu
            />

            {selectionModel.length > 0 && (
                <Box mt={1}>
                    <Button variant="contained" size="small" onClick={handleAddSelected}>
                        Přidat vybrané ({selectionModel.length})
                    </Button>
                </Box>
            )}
        </Box>
    );
};
