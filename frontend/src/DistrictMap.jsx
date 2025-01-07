import React from 'react';
import { MapContainer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const districtData = {
    "Kladno": 100,
    "Beroun": 70,
    "Prachatice": 50,
    "Strakonice": 20,
    "Benešov": 85,
    "Příbram": 65,
    "Tábor": 45,
    "České Budějovice": 90,
    "Jindřichův Hradec": 60,
    "Třebíč": 75,
    "Znojmo": 55,
    "Hodonín": 35,
    "Břeclav": 95,
    "Opava": 40,
    "Frýdek-Místek": 80,
    "Olomouc": 70,
    "Pardubice": 50,
    "Hradec Králové": 90,
    "Liberec": 65,
    "Ústí nad Labem": 75,
    "Chomutov": 60,
    "Most": 45,
    "Karlovy Vary": 85,
    "Plzeň-město": 95,
    "Plzeň-sever": 55,
}

const getColor = (value) => {
    return value > 80 ? '#800026' :
        value > 60 ? '#BD0026' :
            value > 40 ? '#E31A1C' :
                value > 20 ? '#FC4E2A' :
                    '#FFEDA0';
};

const geoJsonStyle = (feature) => {
    const district = feature.name;
    const value = districtData[district] || 0;
    return {
        fillColor: getColor(value),
        weight: 2,
        opacity: 1,
        color: 'white',
        dashArray: '3',
        fillOpacity: 0.7
    };
};

const onEachFeature = (feature, layer) => {
    const district = feature.name;
    const value = districtData[district] || 0;

    layer.bindTooltip(`${district}: ${value}`, {
        direction: 'top',
        permanent: false
    });

    layer.on('mouseover', () => {
        layer.setStyle({
            fillOpacity: 0.9,
            color: 'yellow'
        });
    });

    layer.on('mouseout', () => {
        layer.setStyle({
            fillOpacity: 0.7,
            color: 'white'
        });
    });
};

const HelloDistrictChart = ({ geojsonData }) => {
    return (
        <MapContainer
            center={[50.0755, 14.4378]}
            zoom={7}
            style={{ height: '600px', width: '100%' }}
            zoomControl={false}
            doubleClickZoom={false}
            dragging={false}
        >
            <GeoJSON data={geojsonData} style={geoJsonStyle} onEachFeature={onEachFeature} />
        </MapContainer>
    );
};

export default HelloDistrictChart;
