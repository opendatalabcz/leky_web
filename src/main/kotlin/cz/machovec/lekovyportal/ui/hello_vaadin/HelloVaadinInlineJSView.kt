package cz.machovec.lekovyportal.ui.hello_vaadin

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route

@Route("/hello-vaadin-inline")
class HelloVaadinInlineJSView : Div() {
    init {
        element.setProperty("innerHTML", "<h1 id='message'>Loading...</h1>")
        element.executeJs("document.getElementById('message').innerText = 'Hello Vaadin from Inline JavaScript!';")
    }
}