import React, { useEffect, useState } from "react"
import {
    DataGrid,
    GridColDef,
    GridRenderCellParams,
    GridRowSelectionModel
} from "@mui/x-data-grid"
import { MedicinalProductFilterValues } from "../types/MedicinalProductFilterValues"
import { useUnifiedCart } from "./UnifiedCartContext"
import { Button, Box } from "@mui/material"

export type GroupedDrug = {
    registrationNumber: string
    names: string[]
    suklCodes: string[]
    strengths: string[]
    dosageForms: { code: string, name?: string }[]
    administrationRoutes: { code: string, name?: string }[]
    atcGroups: { code: string, name: string }[]
}

type PagedResponse<T> = {
    content: T[]
    totalPages: number
    page: number
    size: number
    totalElements: number
}

type Props = {
    filters: MedicinalProductFilterValues
    triggerSearch: boolean
    onSearchComplete: () => void
    filtersVersion: number
    setTriggerSearch: (val: boolean) => void
    onAddOne: (regNum: string) => void
    onAddSelected?: () => void
    onSelectionUpdate?: (count: number, selectedIds: string[]) => void
}

export const GroupedDrugTableNew: React.FC<Props> = ({
                                                         filters,
                                                         triggerSearch,
                                                         onSearchComplete,
                                                         filtersVersion,
                                                         setTriggerSearch,
                                                         onAddOne,
                                                         onAddSelected,
                                                         onSelectionUpdate
                                                     }) => {
    const [data, setData] = useState<GroupedDrug[]>([])
    const [currentPage, setCurrentPage] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [loading, setLoading] = useState(false)
    const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>([])
    const { addRegistrationNumber } = useUnifiedCart()

    const pageSize = 5

    useEffect(() => {
        setCurrentPage(0)
    }, [filtersVersion])

    useEffect(() => {
        if (!triggerSearch) return

        const fetchData = async () => {
            setLoading(true)
            try {
                const params = new URLSearchParams()
                if (filters.atcGroupId) params.append("atcGroupId", filters.atcGroupId.toString())
                if (filters.substanceId) params.append("substanceId", filters.substanceId.toString())
                if (filters.medicinalProductQuery) params.append("query", filters.medicinalProductQuery)
                if (filters.period) params.append("period", filters.period)
                params.append("page", currentPage.toString())
                params.append("size", pageSize.toString())

                const res = await fetch(`/api/medicinal-products/grouped-by-reg-number?${params.toString()}`)
                const json: PagedResponse<GroupedDrug> = await res.json()

                setData(json.content)
                setTotalElements(json.totalElements)
            } catch (e) {
                console.error("Chyba při načítání:", e)
            } finally {
                setLoading(false)
                onSearchComplete()
            }
        }

        fetchData()
    }, [triggerSearch, filters, currentPage])

    const handleAddSelected = () => {
        const selectedIds = selectionModel as string[]
        selectedIds.forEach(id => addRegistrationNumber(id))
        onAddSelected?.()
    }

    useEffect(() => {
        const selectedIds = selectionModel as string[]
        onSelectionUpdate?.(selectedIds.length, selectedIds)
    }, [selectionModel, onSelectionUpdate])

    const columns: GridColDef[] = [
        { field: "registrationNumber", headerName: "Registrační číslo", flex: 1 },
        {
            field: "names",
            headerName: "Názvy",
            flex: 2,
            valueGetter: (params: { row?: GroupedDrug }) => params.row?.names.join(", ") ?? "-"
        },
        {
            field: "strengths",
            headerName: "Síly",
            flex: 1,
            valueGetter: (params: { row?: GroupedDrug }) => params.row?.strengths.join(", ") ?? "-"
        },
        {
            field: "dosageForms",
            headerName: "Lékové formy",
            flex: 1.5,
            valueGetter: (params: { row?: GroupedDrug }) =>
                params.row?.dosageForms.map(d => d.name || d.code).join(", ") ?? "-"
        },
        {
            field: "administrationRoutes",
            headerName: "Cesty podání",
            flex: 1.5,
            valueGetter: (params: { row?: GroupedDrug }) =>
                params.row?.administrationRoutes.map(r => r.name || r.code).join(", ") ?? "-"
        },
        {
            field: "atcGroups",
            headerName: "ATC skupiny",
            flex: 2,
            valueGetter: (params: { row?: GroupedDrug }) =>
                params.row?.atcGroups.map(a => `${a.name} (${a.code})`).join(", ") ?? "-"
        },
        {
            field: "suklCodes",
            headerName: "Počet kódů",
            flex: 1,
            valueGetter: (params: { row?: GroupedDrug }) => params.row?.suklCodes.length ?? 0
        },
        {
            field: "action",
            headerName: "Akce",
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
            sortable: false,
            filterable: false
        }
    ]

    return (
        <Box>
            <DataGrid<GroupedDrug>
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
                    setCurrentPage(page)
                    setTriggerSearch(true)
                }}
                getRowId={(row) => row.registrationNumber}
                disableColumnMenu
            />
        </Box>
    )
}
