import React from 'react';
import { MapContainer, TileLayer, GeoJSON, Tooltip } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const data = {
    "Kladno": 100,
    "Beroun": 70,
    "Prachatice": 50,
    "Strakonice": 20
};

const getColor = (value) => {
    return value > 80 ? '#800026' :
        value > 60 ? '#BD0026' :
            value > 40 ? '#E31A1C' :
                value > 20 ? '#FC4E2A' :
                    '#FFEDA0';
};

const geoJsonStyle = (feature) => {
    const okres = feature.properties.name;
    const value = data[okres] || 0;
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
    const okres = feature.properties.name;
    const value = data[okres] || 0;

    layer.bindTooltip(`${okres}: ${value}`, {
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
        <MapContainer center={[50.0755, 14.4378]} zoom={7} style={{ height: '600px', width: '100%' }}>
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            <GeoJSON data={geojsonData} style={geoJsonStyle} onEachFeature={onEachFeature} />
        </MapContainer>
    );
};

export default HelloDistrictChart;
