import { LitElement, html } from 'lit';

class HelloRegionChart extends LitElement {
    firstUpdated() {
        const script = document.createElement('script');
        script.src = 'https://www.gstatic.com/charts/loader.js';
        script.onload = () => {
            google.charts.load('current', { packages: ['geochart'] });
            google.charts.setOnLoadCallback(() => {
                const data = google.visualization.arrayToDataTable([
                    ['Region', 'Počet osob'],
                    ['CZ-PR', 72],
                    ['CZ-JC', 45],
                    ['CZ-MO', 60]
                ]);

                const options = {
                    region: 'CZ',
                    displayMode: 'regions',
                    resolution: 'provinces',
                    colorAxis: { colors: ['#f5f5f5', '#ff0000'] }
                };

                const chart = new google.visualization.GeoChart(this.shadowRoot.getElementById('chart_div'));
                chart.draw(data, options);
            });
        };
        this.shadowRoot.appendChild(script);
    }

    render() {
        return html`<div id="chart_div" style="height: 500px; width: 100%;"></div>`;
    }
}

customElements.define('hello-region-chart', HelloRegionChart);
