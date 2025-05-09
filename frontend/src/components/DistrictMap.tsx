import React from "react"
import { GeoJSON, GeoJSONProps, MapContainer } from "react-leaflet"
import "leaflet/dist/leaflet.css"
import { Feature, FeatureCollection, Geometry } from "geojson"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"
import { Box } from "@mui/material"

interface Props {
    geojsonData: FeatureCollection
    districtData: Record<string, number>
    filter: EReceptDataTypeAggregation
}

export default function DistrictMap({ geojsonData, districtData, filter }: Props) {
    const getColor = (value: number, filter: EReceptDataTypeAggregation): string => {
        const prescribedColors = ["#D3E5FF", "#A4C8FF", "#76ABFF", "#478EFF", "#176AFF", "#0044CC"]
        const dispensedColors = ["#FFD3D3", "#FFA4A4", "#FF7676", "#FF4747", "#FF1717", "#CC0000"]

        const colorScale =
            filter === EReceptDataTypeAggregation.PRESCRIBED ? prescribedColors :
                filter === EReceptDataTypeAggregation.DISPENSED ? dispensedColors :
                    value > 0 ? prescribedColors : dispensedColors

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
        const code = (feature as any).nationalCode
        const value = code ? districtData[code] ?? 0 : 0

        return {
            fillColor: getColor(value, filter),
            weight: 2,
            opacity: 1,
            color: "white",
            dashArray: "3",
            fillOpacity: 0.7,
        }
    }

    const onEachFeature = (feature: Feature<Geometry, any>, layer: L.Layer) => {
        const code = (feature as any).nationalCode
        const name = (feature as any).name || "Neznámý"
        const value = code ? districtData[code] ?? 0 : 0

        if ((layer as any).bindTooltip) {
            (layer as any).bindTooltip(`${name}: ${value}`, {
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

    return (
        <Box sx={{ width: '100%', overflowX: 'auto' }}>
            <Box sx={{ minWidth: '600px', height: '420px' }}>
                <MapContainer
                    center={[49.75, 15]}
                    zoom={7}
                    style={{ width: '100%', height: '100%' }}
                    zoomControl={false}
                    scrollWheelZoom={false}
                    doubleClickZoom={false}
                    dragging={false}
                >
                    <GeoJSON
                        key={JSON.stringify({ districtData, filter })}
                        data={geojsonData}
                        style={geoJsonStyle}
                        onEachFeature={onEachFeature}
                    />
                </MapContainer>
            </Box>
        </Box>
    )
}
