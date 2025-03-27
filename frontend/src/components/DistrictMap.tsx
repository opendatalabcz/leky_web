import React from "react"
import {GeoJSON, GeoJSONProps, MapContainer,} from "react-leaflet"
import "leaflet/dist/leaflet.css"
import {Feature, FeatureCollection, Geometry} from "geojson"

type FilterType = "prescribed" | "dispensed" | "difference"

interface Props {
    geojsonData: FeatureCollection
    districtData: Record<string, number>
    filter: FilterType
}

const getColor = (value: number, filter: FilterType): string => {
    const prescribedColors = ["#D3E5FF", "#A4C8FF", "#76ABFF", "#478EFF", "#176AFF", "#0044CC"]
    const dispensedColors = ["#FFD3D3", "#FFA4A4", "#FF7676", "#FF4747", "#FF1717", "#CC0000"]

    let colorScale: string[]
    if (filter === "prescribed") {
        colorScale = prescribedColors
    } else if (filter === "dispensed") {
        colorScale = dispensedColors
    } else if (filter === "difference") {
        colorScale = value > 0 ? prescribedColors : dispensedColors
    } else {
        return "#FFFFFF"
    }

    const absValue = Math.abs(value)

    return absValue > 1000 ? colorScale[5]
        : absValue > 100 ? colorScale[4]
            : absValue > 50 ? colorScale[3]
                : absValue > 25 ? colorScale[2]
                    : absValue > 10 ? colorScale[1]
                        : absValue > 0 ? colorScale[0]
                            : "#FFFFFF"
}

const geoJsonStyle: GeoJSONProps["style"] = (feature) => {
    if (!feature || !feature.properties || !feature.properties.name) {
        return {
            fillColor: "#FFFFFF",
            weight: 2,
            opacity: 1,
            color: "white",
            dashArray: "3",
            fillOpacity: 0.7,
        }
    }

    const district = feature.properties.name
    const value = districtData[district] ?? 0

    return {
        fillColor: getColor(value, currentFilter),
        weight: 2,
        opacity: 1,
        color: "white",
        dashArray: "3",
        fillOpacity: 0.7,
    }
}

let districtData: Record<string, number> = {}
let currentFilter: FilterType = "prescribed"

const onEachFeature = (
    feature: Feature<Geometry, any>,
    layer: L.Layer
) => {
    const district = feature.properties?.name
    const value = district ? districtData[district] ?? 0 : 0

    if ((layer as any).bindTooltip) {
        (layer as any).bindTooltip(`${district}: ${value}`, {
            direction: "top",
            permanent: false,
        })

        layer.on("mouseover", () => {
            (layer as any).setStyle({
                fillOpacity: 0.9,
                color: "yellow",
            })
        })

        layer.on("mouseout", () => {
            (layer as any).setStyle({
                fillOpacity: 0.7,
                color: "white",
            })
        })
    }
}

export default function DistrictMap({ geojsonData, districtData: data, filter }: Props) {
    districtData = data
    currentFilter = filter

    return (
        <MapContainer
            center={[50.0755, 14.4378]}
            zoom={7}
            style={{ height: "600px", width: "100%" }}
            zoomControl={false}
            doubleClickZoom={false}
            dragging={false}
        >
            <GeoJSON
                key={JSON.stringify(data)}
                data={geojsonData}
                style={geoJsonStyle}
                onEachFeature={(feature, layer) => onEachFeature(feature, layer)}
            />
        </MapContainer>
    )
}
