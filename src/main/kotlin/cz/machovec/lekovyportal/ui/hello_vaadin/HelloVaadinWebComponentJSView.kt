package cz.machovec.lekovyportal.ui.hello_vaadin

import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route

@Route("/hello-vaadin-web-component")
@JsModule("./hello-vaadin.js")
class HelloVaadinWebComponentJSView : Div() {
    init {
        element.setProperty("innerHTML", "<hello-vaadin></hello-vaadin>")
    }
}
