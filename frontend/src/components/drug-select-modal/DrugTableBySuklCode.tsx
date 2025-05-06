import React, { useEffect, useState } from "react"
import {
    DataGrid,
    GridColDef,
    GridRenderCellParams,
    GridRowSelectionModel
} from "@mui/x-data-grid"
import { MedicinalProductFilterValues } from "../../types/MedicinalProductFilterValues"
import { useDrugCart } from "./DrugCartContext"
import { Button, Box } from "@mui/material"

export type Drug = {
    id: number
    name: string
    suklCode: string
    registrationNumber: string | null
    supplementaryInformation: string | null
    atcGroup?: { code: string, name: string } | null
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
    onAddOne: (id: number) => void
    onAddSelected?: () => void
    onSelectionUpdate?: (count: number, selectedIds: number[]) => void
}

export const DrugTableBySuklCode: React.FC<Props> = ({
                                                  filters,
                                                  triggerSearch,
                                                  onSearchComplete,
                                                  filtersVersion,
                                                  setTriggerSearch,
                                                  onAddOne,
                                                  onAddSelected,
                                                  onSelectionUpdate
                                              }) => {
    const [data, setData] = useState<Drug[]>([])
    const [currentPage, setCurrentPage] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [loading, setLoading] = useState(false)
    const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>([])
    const { addSuklId } = useDrugCart()

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
                params.append("searchMode", filters.searchMode)
                params.append("page", currentPage.toString())
                params.append("size", pageSize.toString())

                const res = await fetch(`/api/medicinal-products?${params.toString()}`)
                const json: PagedResponse<Drug> = await res.json()

                setData(json.content)
                setTotalElements(json.totalElements)
            } catch (e) {
                console.error("Chyba při načítání dat:", e)
            } finally {
                setLoading(false)
                onSearchComplete()
            }
        }

        fetchData()
    }, [triggerSearch, filters, currentPage])

    // Akce pro hromadné přidání vybraných
    const handleAddSelected = () => {
        const selectedIds = selectionModel as number[]
        selectedIds.forEach(id => addSuklId(id))
        onAddSelected?.()
    }

    useEffect(() => {
        const selectedIds = selectionModel as number[]
        onSelectionUpdate?.(selectedIds.length, selectedIds)
    }, [selectionModel, onSelectionUpdate])

    const columns: GridColDef[] = [
        { field: "suklCode", headerName: "SÚKL kód", flex: 1 },
        { field: "name", headerName: "Název", flex: 2 },
        {
            field: "supplementaryInformation",
            headerName: "Doplněk",
            flex: 2,
            valueGetter: (params: { row?: Drug }) => params.row?.supplementaryInformation ?? "-"
        },
        {
            field: "registrationNumber",
            headerName: "Registrační číslo",
            flex: 1,
            valueGetter: (params: { row?: Drug }) => params.row?.registrationNumber ?? "-"
        },
        {
            field: "atcGroup",
            headerName: "ATC skupina",
            flex: 2,
            valueGetter: (params: { row?: Drug }) => {
                const atc = params.row?.atcGroup
                return atc ? `${atc.name} (${atc.code})` : "-"
            }
        },
        {
            field: "action",
            headerName: "Akce",
            flex: 1,
            renderCell: (params: GridRenderCellParams<Drug>) => (
                <Button
                    variant="outlined"
                    size="small"
                    onClick={() => onAddOne(params.row.id)}
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
            <DataGrid<Drug>
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
                getRowId={(row) => row.id}
                disableColumnMenu
            />
        </Box>
    )
}
