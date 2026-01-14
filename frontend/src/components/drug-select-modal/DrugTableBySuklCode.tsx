import React, { useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    DataGrid,
    GridColDef,
    GridRenderCellParams,
    GridRowSelectionModel,
} from '@mui/x-data-grid';
import { Box, Button } from '@mui/material';
import { MedicinalProductFilterValues } from '../../types/MedicinalProductFilterValues';

export type Drug = {
    id: number;
    name: string;
    suklCode: string;
    registrationNumber: string | null;
    supplementaryInformation: string | null;
    atcGroup?: { code: string; name: string } | null;
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
    onAddOne: (id: number) => void;
    onSelectionUpdate?: (count: number, selectedIds: number[]) => void;
};

export const DrugTableBySuklCode: React.FC<Props> = ({
                                                         filters,
                                                         triggerSearch,
                                                         onSearchComplete,
                                                         filtersVersion,
                                                         setTriggerSearch,
                                                         onAddOne,
                                                         onSelectionUpdate,
                                                     }) => {
    const pageSize = 5;
    const [currentPage, setCurrentPage] = React.useState(0);

    useEffect(() => setCurrentPage(0), [filtersVersion]);

    const { data, isFetching, isLoading } = useQuery<PagedResponse<Drug>>({
        queryKey: ['medicinal-products-search', filters, currentPage],
        queryFn: async () => {
            const params = new URLSearchParams();
            if (filters.atcGroupId) params.append('atcGroupId', filters.atcGroupId.toString());
            if (filters.substanceId) params.append('substanceId', filters.substanceId.toString());
            if (filters.medicinalProductQuery) params.append('query', filters.medicinalProductQuery);
            if (filters.period) params.append('period', filters.period);
            params.append('searchMode', filters.searchMode);
            params.append('page', currentPage.toString());
            params.append('size', pageSize.toString());

            const res = await fetch(`/api/medicinal-products?${params.toString()}`);
            if (!res.ok) throw new Error("Nepodařilo se načíst data z API");

            const result = await res.json();
            onSearchComplete();
            return result;
        },
        enabled: triggerSearch || currentPage > 0,
        staleTime: 10000,
    });

    const handleSelectionChange = useCallback(
        (model: GridRowSelectionModel) => {
            onSelectionUpdate?.(model.length, model as number[]);
        },
        [onSelectionUpdate]
    );

    const columns: GridColDef<Drug>[] = [
        { field: 'suklCode', headerName: 'SÚKL kód', minWidth: 150 },
        { field: 'name', headerName: 'Název', minWidth: 200 },
        {
            field: 'supplementaryInformation',
            headerName: 'Doplněk',
            minWidth: 200,
            renderCell: (params: GridRenderCellParams<Drug>) =>
                params.row.supplementaryInformation ?? '-',
        },
        {
            field: 'registrationNumber',
            headerName: 'Registrační číslo',
            minWidth: 180,
            renderCell: (params: GridRenderCellParams<Drug>) =>
                params.row.registrationNumber ?? '-',
        },
        {
            field: 'atcGroup',
            headerName: 'ATC skupina',
            minWidth: 200,
            renderCell: (params: GridRenderCellParams<Drug>) => {
                const atc = params.row.atcGroup;
                return atc ? `${atc.name} (${atc.code})` : '-';
            },
        },
        {
            field: 'action',
            headerName: 'Akce',
            minWidth: 120,
            renderCell: (params: GridRenderCellParams<Drug>) => (
                <Button variant="outlined" size="small" onClick={() => onAddOne(params.row.id)}>
                    Přidat
                </Button>
            ),
        },
    ];

    return (
        <Box sx={{ width: '100%' }}>
            <DataGrid
                autoHeight
                rows={data?.content || []}
                columns={columns}
                rowCount={data?.totalElements || 0}
                pageSizeOptions={[pageSize]}
                paginationModel={{ page: currentPage, pageSize }}
                paginationMode="server"
                checkboxSelection
                disableRowSelectionOnClick
                onRowSelectionModelChange={handleSelectionChange}
                loading={isLoading || isFetching}
                onPaginationModelChange={({ page }) => {
                    setCurrentPage(page);
                    setTriggerSearch(true);
                }}
                getRowId={(row) => row.id}
                disableColumnMenu
            />
        </Box>
    );
};
