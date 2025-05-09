import React from "react"
import { GeoJSON, GeoJSONProps, MapContainer } from "react-leaflet"
import "leaflet/dist/leaflet.css"
import { Feature, FeatureCollection, Geometry } from "geojson"
import { EReceptDataTypeAggregation } from "../types/EReceptDataTypeAggregation"
import { Box, Typography } from "@mui/material"
import { MedicinalUnitMode } from "../types/MedicinalUnitMode"

interface Props {
    geojsonData: FeatureCollection
    districtData: Record<string, number>
    filter: EReceptDataTypeAggregation
    medicinalUnitMode: MedicinalUnitMode
}

export default function DistrictMap({ geojsonData, districtData, filter, medicinalUnitMode }: Props) {
    const positiveColors = ["#D3E5FF", "#A4C8FF", "#76ABFF", "#478EFF", "#176AFF", "#0044CC"]
    const negativeColors = ["#FFD3D3", "#FFA4A4", "#FF7676", "#FF4747", "#FF1717", "#CC0000"]

    const positiveValues = Object.values(districtData).filter(v => v > 0)
    const negativeValues = Object.values(districtData).filter(v => v < 0).map(Math.abs)

    let maxReference = 0

    if (filter === EReceptDataTypeAggregation.PRESCRIBED) {
        maxReference = positiveValues.length ? Math.max(...positiveValues) : 0
    } else if (filter === EReceptDataTypeAggregation.DISPENSED) {
        maxReference = positiveValues.length ? Math.max(...positiveValues) : 0 // vydané jsou vždy kladné
    } else if (filter === EReceptDataTypeAggregation.DIFFERENCE) {
        const maxPositive = positiveValues.length ? Math.max(...positiveValues) : 0
        const maxNegative = negativeValues.length ? Math.max(...negativeValues) : 0
        maxReference = Math.max(maxPositive, maxNegative)
    }

    const numSteps = 6
    const stepSize = maxReference / numSteps
    const grades = Array.from({ length: numSteps }, (_, i) => Math.round(i * stepSize))

    const geoJsonStyle: GeoJSONProps["style"] = (feature) => {
        const code = (feature as any).nationalCode
        const value = code ? districtData[code] ?? 0 : 0
        const absValue = Math.abs(value)

        let fillColor = "#FFFFFF"

        if (filter === EReceptDataTypeAggregation.PRESCRIBED || (filter === EReceptDataTypeAggregation.DIFFERENCE && value > 0)) {
            for (let i = grades.length - 1; i >= 0; i--) {
                if (absValue >= grades[i]) {
                    fillColor = positiveColors[i]
                    break
                }
            }
        } else if (filter === EReceptDataTypeAggregation.DISPENSED || (filter === EReceptDataTypeAggregation.DIFFERENCE && value < 0)) {
            for (let i = grades.length - 1; i >= 0; i--) {
                if (absValue >= grades[i]) {
                    fillColor = negativeColors[i]
                    break
                }
            }
        }

        return {
            fillColor,
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
            <Box sx={{ minWidth: '800px', height: '420px', position: 'relative' }}>
                <MapContainer
                    center={[49.75, 16]}
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

                <Box
                    sx={{
                        position: 'absolute',
                        bottom: 16,
                        right: 16,
                        background: 'white',
                        padding: '10px',
                        borderRadius: '4px',
                        boxShadow: 3,
                        fontSize: '12px',
                        zIndex: 1000,
                    }}
                >
                    <Typography variant="body2" fontWeight={600} gutterBottom>
                        Legenda ({medicinalUnitMode === MedicinalUnitMode.PACKAGES ? 'balení' : 'DDD'})
                    </Typography>
                    {(filter === EReceptDataTypeAggregation.PRESCRIBED || filter === EReceptDataTypeAggregation.DIFFERENCE) && (
                        <Box>
                            <Typography variant="caption" fontWeight={600}>Předepsáno (modře)</Typography>
                            {grades.map((grade, i) => (
                                <Box key={`pos-${i}`} sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                                    <Box sx={{ width: 18, height: 18, backgroundColor: positiveColors[i], border: '1px solid #999', mr: 1 }} />
                                    {grade}{grades[i + 1] ? `–${grades[i + 1]}` : '+'}
                                </Box>
                            ))}
                        </Box>
                    )}
                    {(filter === EReceptDataTypeAggregation.DISPENSED || filter === EReceptDataTypeAggregation.DIFFERENCE) && (
                        <Box mt={1}>
                            <Typography variant="caption" fontWeight={600}>Vydáno (červeně)</Typography>
                            {grades.map((grade, i) => (
                                <Box key={`neg-${i}`} sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                                    <Box sx={{ width: 18, height: 18, backgroundColor: negativeColors[i], border: '1px solid #999', mr: 1 }} />
                                    {grade}{grades[i + 1] ? `–${grades[i + 1]}` : '+'}
                                </Box>
                            ))}
                        </Box>
                    )}
                </Box>
            </Box>
        </Box>
    )
}