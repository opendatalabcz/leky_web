package cz.machovec.lekovyportal.crawler

import org.jsoup.Jsoup
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CrawlerService {
    @Scheduled(cron = "0 * * * * ?")
    fun crawlPage() {
        println("Spouštím crawler pro test...")

        // 1) Stáhnout HTML
        val url = "https://opendata.sukl.cz/?q=katalog/predepsane-vydane-lecive-pripravky-ze-systemu-erecept"
        val doc = Jsoup.connect(url).get()

        // 2) Najít všechny odkazy
        val links = doc.select("a[href]")

        // 3) Vypsat je
        for ((index, link) in links.withIndex()) {
            val href = link.attr("abs:href")
            println("#$index => $href")
        }
    }
}