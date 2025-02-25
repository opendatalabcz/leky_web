import React, { useEffect, useState } from 'react';
import DistrictMap from './DistrictMap';

function App() {
    const [geojsonData, setGeojsonData] = useState(null);
    const [districtData, setDistrictData] = useState(null);
    const [filter, setFilter] = useState('prescribed');

    useEffect(() => {
        fetch('/okresy.json')
            .then((response) => response.json())
            .then((data) => setGeojsonData(data));

        fetchDistrictData(filter);
    }, []);

    const fetchDistrictData = (selectedFilter: string) => {
        fetch(`/api/district-data?filter=${selectedFilter}`)
            .then((response) => response.json())
            .then((data) => setDistrictData(data));
    };

    const handleFilterChange = (event : React.ChangeEvent<HTMLSelectElement>) => {
        const selectedFilter = event.target.value;
        setFilter(selectedFilter);
        fetchDistrictData(selectedFilter);
    };

    if (!geojsonData || !districtData) {
        return <div>Načítám data...</div>;
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

            <DistrictMap geojsonData={geojsonData} districtData={districtData} filter={filter}/>
        </div>
    );
}

export default App;
