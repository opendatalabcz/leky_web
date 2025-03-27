import React, {useEffect, useState} from "react"
import DistrictMap from "../components/DistrictMap"
import {FeatureCollection} from "geojson"

type FilterType = "prescribed" | "dispensed" | "difference"

function MapOverviewPage() {
    const [geojsonData, setGeojsonData] = useState<FeatureCollection | null>(null)
    const [districtData, setDistrictData] = useState<Record<string, number> | null>(null)
    const [filter, setFilter] = useState<FilterType>("prescribed")

    useEffect(() => {
        fetch("/okresy.json")
            .then((response) => response.json())
            .then((data) => setGeojsonData(data))

        fetchDistrictData(filter)
    }, [])

    const fetchDistrictData = (selectedFilter: FilterType) => {
        fetch(`/api/district-data?filter=${selectedFilter}`)
            .then((response) => response.json())
            .then((data) => setDistrictData(data))
    }

    const handleFilterChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const selectedFilter = event.target.value as FilterType
        setFilter(selectedFilter)
        fetchDistrictData(selectedFilter)
    }

    if (!geojsonData || !districtData) {
        return <div>Načítám data...</div>
    }

    return (
        <div>
            <h1>Mapa okresů</h1>
            <div>
                <label htmlFor="filter">Vyber typ dat:</label>
                <select id="filter" value={filter} onChange={handleFilterChange}>
                    <option value="prescribed">Předepsané</option>
                    <option value="dispensed">Vydané</option>
                    <option value="difference">Rozdíl</option>
                </select>
            </div>

            <DistrictMap geojsonData={geojsonData} districtData={districtData} filter={filter} />
        </div>
    )
}

export default MapOverviewPage
