import { Box, Typography, Link } from "@mui/material"

export function AboutPage() {
    return (
        <Box maxWidth={900}>
            <Typography variant="h5" gutterBottom>
                O projektu
            </Typography>

            <Typography variant="body1" color="text.secondary" mb={3}>
                Tato webová aplikace slouží k analytickému a orientačnímu pohledu na to,
                jak se léčiva v České republice předepisují, vydávají a pohybují
                distribučním řetězcem. Jejím cílem je nabídnout přehledný „makro-pohled“
                na vývoj a regionální rozdíly v čase, nikoli detailní sledování
                jednotlivých balení či aktuálních skladových zásob.
            </Typography>

            <Typography variant="h6" gutterBottom>
                Použitá data a zdroje
            </Typography>

            <Typography variant="body1" color="text.secondary" mb={2}>
                Aplikace vychází výhradně z agregovaných otevřených dat zveřejňovaných
                Státním ústavem pro kontrolu léčiv (SÚKL). Použitá data zahrnují zejména
                souhrnné údaje o předepsaných a vydaných léčivech ze systému eRecept,
                agregovaná hlášení o pohybu léčiv mezi jednotlivými články
                distribučního řetězce a referenční informace o léčivých přípravcích
                (např. kódy SÚKL, registrační čísla nebo ATC klasifikaci).
            </Typography>

            <Typography variant="body1" color="text.secondary" mb={3}>
                Přehled dostupných datových sad je k dispozici v&nbsp;
                <Link
                    href="https://opendata.sukl.cz/?q=katalog-otevrenych-dat"
                    target="_blank"
                    rel="noopener noreferrer"
                    underline="hover"
                >
                    katalogu otevřených dat SÚKL
                </Link>.
            </Typography>

            <Typography variant="h6" gutterBottom>
                Zpracování a interpretace dat
            </Typography>

            <Typography variant="body1" color="text.secondary" mb={2}>
                Data jsou automaticky stahována, vzájemně propojována pomocí
                identifikátorů SÚKL a vizualizována formou časových řad, mapy okresů
                a Sankey diagramu. Vizualizace umožňují sledovat trendy v čase,
                porovnávat regiony a nahlížet na tok léčiv napříč distribučním
                řetězcem.
            </Typography>

            <Typography variant="body1" color="text.secondary" mb={3}>
                Při interpretaci výsledků je nutné počítat s limity veřejně dostupných
                zdrojů. Zobrazená data nezahrnují většinu volně prodejných léčiv (OTC),
                nezachycují pohyby jednotlivých šarží ani aktuální stav skladových
                zásob. Výsledky proto nepředstavují okamžitý přehled dostupnosti
                léčiv v lékárnách, ale slouží především k analytickému a orientačnímu
                pohledu na vývoj v čase.
            </Typography>

            <Typography variant="h6" gutterBottom>
                Oficiální informace o léčivech
            </Typography>

            <Typography variant="body1" color="text.secondary">
                Podrobné a oficiální informace o konkrétních léčivých přípravcích
                (např. SPC, příbalové informace nebo hlášení o výpadcích) jsou dostupné
                v&nbsp;
                <Link
                    href="https://prehledy.sukl.cz/prehled_leciv.html#/"
                    target="_blank"
                    rel="noopener noreferrer"
                    underline="hover"
                >
                    Přehledu léčiv SÚKL
                </Link>.
            </Typography>
        </Box>
    )
}
