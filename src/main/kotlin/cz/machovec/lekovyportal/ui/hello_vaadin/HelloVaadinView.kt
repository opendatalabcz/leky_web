package cz.machovec.lekovyportal.ui.hello_vaadin

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

@Route("/hello-vaadin")
class HelloVaadinView : VerticalLayout() {
    init {
        val helloText = Span("Hello Vaadin!")

        add(helloText)
    }
}