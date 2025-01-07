package cz.machovec.lekovyportal.ui.hello_maps

import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route

@Route("/hello-district-chart")
@JsModule("./hello-district-chart.js")
class LeafletMapView : Div() {
    init {
        element.setProperty("innerHTML", "<hello-district-chart></hello-district-chart>")
    }
}