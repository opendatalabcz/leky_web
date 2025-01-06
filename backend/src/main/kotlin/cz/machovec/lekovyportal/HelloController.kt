package cz.machovec.lekovyportal

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HelloController {
    @GetMapping("/hello")
    fun sayHello(): String {
        return "Hello from Kotlin Spring Boot!"
    }
}
