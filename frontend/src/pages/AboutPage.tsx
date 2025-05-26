export function AboutPage() {
    return (
        <div className="p-4">
            <h1 className="text-2xl font-bold mb-4">O projektu</h1>
            <p className="mb-2">
                Tato webová aplikace vychází výhradně z agregovaných otevřených dat, která Státní ústav pro kontrolu léčiv zveřejňuje (Databáze léčivých přípravků, měsíční hlášení o distribuci a souhrny z elektronických receptů). Údaje automaticky stahujeme, propojujeme pomocí kódů SÚKL a vizualizujeme je formou časových řad, map okresů nebo sankey diagramů. Cílem je nabídnout rychlý „makro-pohled“ na to, jak se léčiva v ČR předepisují, vydávají a putují distribučním řetězcem.
            </p>
            <p className="mb-2">
                Je však potřeba počítat s limity těchto veřejných souhrnů. V datech chybí například většina výdejů volně prodejných přípravků (OTC), protože se neevidují v systému eRecept a lékárny je hlásí jen výjimečně. Stejně tak nejsou zachyceny pohyby jednotlivých šarží ani aktuální skladové zásoby. Výsledky proto neodrážejí kompletní a okamžitou dostupnost léků v lékárnách.
            </p>
            <p>
                Podrobné, oficiální informace o konkrétních přípravcích (SPC, příbalové letáky, hlášení o výpadcích) najdete vždy v Přehledu léčiv SÚKL: <a href="https://prehledy.sukl.cz/prehled_leciv.html#/" className="text-blue-500 underline" target="_blank" rel="noopener noreferrer">https://prehledy.sukl.cz/prehled_leciv.html#/</a>.
            </p>
        </div>
    )
}
