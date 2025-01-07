import React from 'react';
import { MapContainer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const getColor = (value) => {
    return value > 90 ? '#800026' :
        value > 70 ? '#BD0026' :
            value > 50 ? '#E31A1C' :
                value > 30 ? '#FC4E2A' :
                    value > 10 ? '#FFEDA0' :
                        '#D3E5FF';
};

const geoJsonStyle = (feature, districtData) => {
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

const onEachFeature = (feature, layer, districtData) => {
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

const DistrictMap = ({ geojsonData, districtData }) => {
    return (
        <MapContainer
            center={[50.0755, 14.4378]}
            zoom={7}
            style={{ height: '600px', width: '100%' }}
            zoomControl={false}
            doubleClickZoom={false}
            dragging={false}
        >
            <GeoJSON
                data={geojsonData}
                style={(feature) => geoJsonStyle(feature, districtData)}
                onEachFeature={(feature, layer) => onEachFeature(feature, layer, districtData)}
            />
        </MapContainer>
    );
};

export default DistrictMap;
