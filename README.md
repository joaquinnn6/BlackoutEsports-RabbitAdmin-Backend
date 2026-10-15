# Blackout Esports - RabbitMQ Admin

Microservicio REST que administra colas, exchanges y bindings de RabbitMQ. Forma parte de la segunda entrega de Blackout Esports y centraliza las operaciones administrativas del broker sin exponer su consola a Internet.

## Seguridad

- Los endpoints administrativos aceptan solamente access tokens de Microsoft Entra ID.
- El token debe incluir la audiencia y el scope configurados.
- Solo los roles `Admin` y `Staff` pueden utilizar `/api/rabbit/**`.
- Swagger queda disponible para documentar y probar el contrato.
- Los puertos de RabbitMQ, su panel y la API se publican únicamente en `127.0.0.1`.

## API

| Método | Ruta | Acción |
|---|---|---|
| `POST` | `/api/rabbit/queues` | Crear una cola |
| `DELETE` | `/api/rabbit/queues/{name}` | Eliminar una cola |
| `POST` | `/api/rabbit/exchanges` | Crear un exchange `DIRECT`, `TOPIC` o `FANOUT` |
| `DELETE` | `/api/rabbit/exchanges/{name}` | Eliminar un exchange |
| `POST` | `/api/rabbit/bindings` | Crear un binding |
| `DELETE` | `/api/rabbit/bindings` | Eliminar un binding |

Los cuerpos se validan antes de contactar al broker. Los errores de validación responden `400` y los problemas de RabbitMQ responden `502`.

## Topología de notificaciones

La integración inicial usa esta topología:

- Exchange de eventos: `blackout.events.exchange` (`topic`).
- Cola de fans: `fan.notifications.queue`.
- Binding: `tournament.#`.
- Dead-letter exchange: `blackout.dead-letter.exchange`.
- DLQ: `fan.notifications.dlq`.

Torneos publica `tournament.created`; Usuarios consume el evento con ACK manual, hasta tres reintentos y envío a la DLQ si el error persiste.

## Ejecución

1. Copiar `.env.example` a `.env` y definir una contraseña segura.
2. Completar issuer, audience y scope de Entra ID.
3. Ejecutar `docker compose up -d --build`.
4. Abrir `http://localhost:8085/swagger-ui/index.html`.

La red Docker se llama `blackout-network`; los demás microservicios se conectan a RabbitMQ usando el host `blackout-rabbitmq`.

## Pruebas

Ejecutar `mvn test`. Las pruebas verifican la conversión de DTO a recursos AMQP, las operaciones administrativas, las validaciones y la restricción por rol.
