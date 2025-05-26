import React, { useEffect, useState, useCallback } from 'react';
import {
    DataGrid,
    GridColDef,
    GridRenderCellParams,
    GridRowSelectionModel,
} from '@mui/x-data-grid';
import { Box, Button } from '@mui/material';
import { MedicinalProductFilterValues } from '../../types/MedicinalProductFilterValues';
import { useDrugCart } from './DrugCartContext';

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

    const [data, setData] = useState<Drug[]>([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [loading, setLoading] = useState(false);
    const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>([]);

    const { addSuklId } = useDrugCart();

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
                params.append('searchMode', filters.searchMode);
                params.append('page', currentPage.toString());
                params.append('size', pageSize.toString());

                const res = await fetch(`/api/medicinal-products?${params.toString()}`);
                const json: PagedResponse<Drug> = await res.json();

                setData(json.content);
                setTotalElements(json.totalElements);
            } catch (e) {
                console.error('Chyba při načítání dat:', e);
            } finally {
                setLoading(false);
                onSearchComplete();
            }
        };

        fetchData();
    }, [triggerSearch, filters, currentPage, onSearchComplete]);

    const handleSelectionChange = useCallback(
        (model: GridRowSelectionModel) => {
            setSelectionModel(model);
            onSelectionUpdate?.(model.length, model as number[]);
        },
        [onSelectionUpdate]
    );

    const columns: GridColDef<Drug>[] = [
        { field: 'suklCode', headerName: 'SÚKL kód', minWidth: 150,},
        { field: 'name', headerName: 'Název', minWidth: 200,},
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
            onRowSelectionModelChange={handleSelectionChange}
            loading={loading}
            onPaginationModelChange={({ page }) => {
                setCurrentPage(page);
                setTriggerSearch(true);
            }}
            getRowId={(row) => row.id}
            disableColumnMenu
        />
    );
};
