package cz.machovec.lekovyportal.ui.hello_maps

import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route

@Route("/hello-region-chart")
@JsModule("./hello-region-chart.js")
class HelloRegionChartView : Div() {
    init {
        element.setProperty("innerHTML", "<hello-region-chart></hello-region-chart>")
    }
}
