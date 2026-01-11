# Lekový Portál

Repozitář obsahuje frontend a backend aplikace Lekový Portál, připravené pro běh v Dockeru pomocí `docker compose`.

## Požadavky
- Docker
- Docker Compose plugin (`docker compose`)

---

## Spuštění projektu

### 1) Vytvoř `.env` soubor
V kořenovém adresáři projektu vytvoř soubor `.env` s následujícím obsahem:

```env
# PostgreSQL
POSTGRES_DB=lekovy_portal
POSTGRES_USER=lekovy_portal_postgres_user
POSTGRES_PASSWORD=superSilneHesloPostgres!!!

# RabbitMQ
RABBITMQ_USER=lekovy_portal_rabbit_user
RABBITMQ_PASSWORD=superSilneHesloRabbit!!!
```

Konkrétní hodnoty hesel je samozřejmě potřeba přepsat.

---

### 2) Postav a spusť kontejnery
```bash
docker compose --compatibility --env-file .env up -d --build
```

---

### 3) Zkontroluj běžící služby
```bash
docker compose ps
```

- Frontend (lokálně): http://localhost
- Frontend (na serveru): http://<server-ip> nebo http://<domena>

---

## Zastavení služeb
```bash
docker compose down
```

---

## Práce s databází (zachování vs. smazání dat)

### Běžný deploy / update aplikace (data zůstanou zachována)
```bash
docker compose --compatibility --env-file .env up -d --build
```

### Kompletní reset databáze (smazání všech dat)
```bash
docker compose down -v
docker compose --compatibility --env-file .env up -d --build
```

> Volba `-v` smaže Docker volumes (včetně dat PostgreSQL).

---

## RabbitMQ Management UI (volitelné)

RabbitMQ Management UI je z bezpečnostních důvodů bindnuté pouze na `127.0.0.1:15672`.

Na vzdáleném serveru (VPS) se k němu lze připojit pomocí SSH tunelu:

```bash
ssh -L 15672:127.0.0.1:15672 root@<server-ip>
```

Poté otevři v prohlížeči:
- http://localhost:15672

Přihlašovací údaje jsou stejné jako v `.env` souboru.

---

## Poznámka k paměťovým limitům
Projekt používá paměťové limity definované v `deploy.resources.limits`.

Aby byly tyto limity aplikovány i při běžném spuštění pomocí Docker Compose, je nutné použít přepínač:

```bash
--compatibility
```

Bez tohoto přepínače Docker Compose paměťové limity ignoruje.