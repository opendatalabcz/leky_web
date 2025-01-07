import { LitElement, html, css } from 'lit';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

class HelloDistrictChart extends LitElement {
    static styles = css`
        #map {
            position: relative;
            height: 600px;
            width: 100%;
            border: 1px solid #ccc;
        }

        .leaflet-tooltip {
            position: absolute;
            font-size: 14px;
            font-weight: bold;
            background-color: rgba(255, 255, 255, 0.9);
            border: 1px solid black;
            border-radius: 4px;
            padding: 4px;
        }
    `;

    firstUpdated() {
        const map = L.map(this.shadowRoot.getElementById('map'), {
            center: [50.0755, 14.4378],
            zoom: 7,
            dragging: false,
            scrollWheelZoom: false,
            doubleClickZoom: false,
            touchZoom: false,
        });

        var data = {
            "Kladno": 100,
            "Beroun": 70,
            "Prachatice": 50,
            "Strakonice": 20
        };

        function getColor(value) {
            return value > 80 ? '#800026' :
                value > 60 ? '#BD0026' :
                    value > 40 ? '#E31A1C' :
                        value > 20 ? '#FC4E2A' :
                            '#FFEDA0';
        }

        function style(feature) {
            var okres = feature.name;
            var value = data[okres] || 0;
            return {
                fillColor: getColor(value),
                weight: 2,
                opacity: 1,
                color: 'white',
                dashArray: '3',
                fillOpacity: 0.7
            };
        }

        function onEachFeature(feature, layer) {
            var okres = feature.name;
            var value = data[okres] || 0;

            layer.bindTooltip(`${okres}: ${value}`, {
                permanent: true,
                direction: 'top',
            });

            layer.on('click', () => {
                alert(`Klikl jsi na okres: ${feature.properties.name}`);
            });

            layer.on('mouseover', () => {
                console.log(`Zobrazen tooltip pro okres: ${okres} s hodnotou: ${value}`);
                layer.openTooltip();
            });

            layer.on('mouseout', () => {
                console.log(`Skryt tooltip pro okres: ${okres}`);
                layer.closeTooltip();
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

            console.log(layer);
        }

        fetch('okresy.json')
            .then(response => response.json())
            .then(geojsonData => {
                geojsonData.features.forEach(feature => {
                    console.log(feature.name);
                });
                L.geoJson(geojsonData, {
                    style: style,
                    onEachFeature: onEachFeature
                }).addTo(map);
                map.invalidateSize();
            });

    }

    render() {
        return html`<div id="map"></div>`;
    }
}

customElements.define('hello-district-chart', HelloDistrictChart);
