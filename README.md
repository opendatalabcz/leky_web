# Lekovy Portal

Tento repozitář obsahuje frontendovou a backendovou část projektu Lekový Portál, připravenou k běhu v Dockeru pomocí `docker-compose`.

## Jak spustit projekt

### 1. Vytvoř `.env` soubor

V kořenovém adresáři projektu vytvoř soubor `.env` a vlož do něj následující obsah. Přihlašovací údaje můžeš upravit podle potřeby.

```env
# PostgreSQL
POSTGRES_USER=lekovy_portal_user
POSTGRES_PASSWORD=password
POSTGRES_DB=lekovy_portal

# RabbitMQ
RABBITMQ_DEFAULT_USER=lekovy_portal_user
RABBITMQ_DEFAULT_PASS=password

# Backend DB config
DB_URL=jdbc:postgresql://leky-db:5432/lekovy_portal
DB_USER=lekovy_portal_user
DB_PASSWORD=password

# Backend RabbitMQ config
RABBIT_HOST=leky-rabbitmq
RABBIT_PORT=5672
RABBIT_USER=lekovy_portal_user
RABBIT_PASSWORD=password
```

### 2. Postav a spusť kontejnery
```bash
docker compose --env-file .env up --build -d
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
