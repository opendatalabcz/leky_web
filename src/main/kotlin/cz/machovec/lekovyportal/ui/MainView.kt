package cz.machovec.lekovyportal.ui

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import org.springframework.stereotype.Component

@Component
@Route("/hello")
class MainView : VerticalLayout() {
    init {
        val helloText = Span("Hello World from Vaadin!")

        add(helloText)
    }
}