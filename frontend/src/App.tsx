import React, { useEffect, useState } from 'react';
import DistrictMap from './DistrictMap';

function App() {
    const [geojsonData, setGeojsonData] = useState(null);

    useEffect(() => {
        fetch('/okresy.json')
            .then((response) => response.json())
            .then((data) => setGeojsonData(data));
    }, []);

    if (!geojsonData) {
        return <div>Načítám data...</div>;
    }

    return (
        <div>
            <h1>Mapa okresů</h1>
            <DistrictMap geojsonData={geojsonData} />
        </div>
    );
}

export default App;
