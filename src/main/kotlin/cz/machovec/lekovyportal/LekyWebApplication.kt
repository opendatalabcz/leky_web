package cz.machovec.lekovyportal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LekyWebApplication

fun main(args: Array<String>) {
	runApplication<LekyWebApplication>(*args)
}
