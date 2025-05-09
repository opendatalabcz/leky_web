# Lekovy Portal

Tento repozitář obsahuje frontendovou a backendovou část projektu Lekový Portal, připravenou k běhu v Dockeru.

## Jak spustit projekt

### 1. Změň konfigurace, pokud je potřeba
V souboru `docker-compose.yml` jsou přednastavené hodnoty pro:
- přístup k databázi (Postgres),
- přístup k RabbitMQ,
- porty aplikací.

Pokud potřebuješ jiné uživatelské jméno/heslo, uprav tyto části:

```yaml
environment:
  DB_USER: lekovy_portal_user
  DB_PASSWORD: password
  RABBIT_USER: lekovy_portal_user
  RABBIT_PASSWORD: password
```

### 2. Postav a spusť kontejnery
```bash
docker-compose up --build -d
```

### 3. Zkontroluj běžící služby
```bash
docker ps
```

- Frontend: http://localhost


### Zastavení všech služeb
```bash
docker-compose down
```
