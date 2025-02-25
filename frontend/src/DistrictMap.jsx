import React from 'react';
import { MapContainer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const getColor = (value, filter) => {
    const prescribedColors = ["#D3E5FF", "#A4C8FF", "#76ABFF", "#478EFF", "#176AFF", "#0044CC"]; // Modrá škála
    const dispensedColors = ["#FFD3D3", "#FFA4A4", "#FF7676", "#FF4747", "#FF1717", "#CC0000"]; // Červená škála

    let colorScale;
    if (filter === "prescribed") {
        colorScale = prescribedColors;
    } else if (filter === "dispensed") {
        colorScale = dispensedColors;
    } else if (filter === "difference") {
        colorScale = value > 0 ? prescribedColors : dispensedColors;
    } else {
        return "#FFFFFF"; // Neznámý filter, fallback na bílou
    }

    const absValue = Math.abs(value);

    return absValue > 1000 ? colorScale[5] :
        absValue > 100 ? colorScale[4] :
            absValue > 50 ? colorScale[3] :
                absValue > 25 ? colorScale[2] :
                    absValue > 10 ? colorScale[1] :
                        absValue > 0 ? colorScale[0] :
                            "#FFFFFF"; // 0 = bez barvy
};

const geoJsonStyle = (feature, districtData, filter) => {
    const district = feature.name;
    const value = districtData[district] || 0;
    return {
        fillColor: getColor(value, filter),
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

const DistrictMap = ({ geojsonData, districtData, filter }) => {
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
                key={JSON.stringify(districtData)}
                data={geojsonData}
                style={(feature) => geoJsonStyle(feature, districtData, filter)}
                onEachFeature={(feature, layer) => onEachFeature(feature, layer, districtData)}
            />
        </MapContainer>
    );
};

export default DistrictMap;
