# leky_web

Database:
1. Vytvoření docker kontejneru s postgres
   * `docker run --name lekovy-portal-postgres -e POSTGRES_USER=lekovy_portal_user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=lekovy_portal -p 5433:5432 -d postgres`

RabbitMQ:
1. Vytvoření docker kontejneru s RabbitMQ
   * `docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=lekovy_portal_user -e RABBITMQ_DEFAULT_PASS=password rabbitmq:management`