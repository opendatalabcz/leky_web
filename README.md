# Lekový Portál

Repozitář obsahuje frontend a backend aplikace Lekový Portál, připravené pro běh v Dockeru pomocí `docker compose`.

## Požadavky
- Docker
- Docker Compose plugin (`docker compose`)

---

## Návod k nasazení

### 1) Vytvoř `.env` soubor
V kořenovém adresáři vytvořte soubor .env (podle vzoru .env.example) a vyplňte přístupové údaje.

```env
POSTGRES_DB=lekovy_portal
POSTGRES_USER=lekovy_portal_user
POSTGRES_PASSWORD=zvolte_silne_heslo

RABBITMQ_USER=rabbit_user
RABBITMQ_PASSWORD=zvolte_silne_heslo
```

---

### 2. Konfigurace vnějšího Nginxu a CORS
V `docker-compose.yml` je frontend standardně bindován na `127.0.0.1:8081:80`.

### Nastavení vnějšího Nginxu:
Pro funkční přístup přes doménu vložte do konfigurace vnějšího Nginxu na hostitelském serveru následující blok:

```nginx
  location / {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    proxy_read_timeout 300;
    proxy_connect_timeout 60;
    proxy_send_timeout 300;
    
    client_max_body_size 100M;
}
```

### Kontrola povolených domén (CORS):
Backend je v docker-compose.yml přednastaven pro domény lecivavdatech.cz a lecivavdatech.opendatalab.cz (proměnná ALLOWED_ORIGINS).
Pokud budete aplikaci nasazovat na jinou subdoménu, doplňte ji do tohoto seznamu v docker-compose.yml před spuštěním.

---

### 3) Spuštění aplikace
Pro správnou aplikaci paměťových limitů a načtení konfigurace použijte tento příkaz:

```bash
docker compose --compatibility --env-file .env up -d --build
```

Aplikace je poté dostupná:
* Lokálně (vývoj): http://localhost:8081
* Produkce (přes doménu): http://<server-ip> nebo http://<domena> (Standardní porty 80/443 jsou vnějším Nginxem mapovány na vnitřní port 8081)
  * Např. https://lecivavdatech.opendatalab.cz

---

### 4) Správa kontejnerů

#### Zastavení služeb
```bash
docker compose down
```

#### Aktualizace (přenasazení)
```bash
docker compose --compatibility --env-file .env up -d --build
```

#### Kompletní přenasazení, včetně smazání dat
```bash
docker compose down -v
docker compose --compatibility --env-file .env up -d --build
```

---

### 5) RabbitMQ Management UI (volitelné)

RabbitMQ Management UI je z bezpečnostních důvodů bindnuté pouze na `127.0.0.1:15672`.

Na vzdáleném serveru (VPS) se k němu lze připojit pomocí SSH tunelu:

```bash
ssh -L 15672:127.0.0.1:15672 root@<server-ip>
```

Poté otevři v prohlížeči:
- http://localhost:15672

Přihlašovací údaje jsou stejné jako v `.env` souboru.

----------------------------

## Poznámka k paměťovým limitům
Projekt používá paměťové limity definované v `deploy.resources.limits`.

Aby byly tyto limity aplikovány i při běžném spuštění pomocí Docker Compose, je nutné použít přepínač:

```bash
--compatibility
```

Bez tohoto přepínače Docker Compose paměťové limity ignoruje.