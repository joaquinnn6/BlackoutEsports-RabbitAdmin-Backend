# Clúster RabbitMQ de la entrega final

docker-compose.cluster.yml crea rabbit1 y rabbit2 con RabbitMQ 4.4.1, nodos rabbit@rabbit1 y rabbit@rabbit2, cookie compartida, volúmenes independientes y descubrimiento classic_config con ambos nombres. Los dos procesos arrancan en paralelo. El script scripts/check_cluster.py comprueba por la API de administración que ambos aparecen activos en el mismo clúster. El workflow hace esta comprobación sobre infraestructura nueva.

## Configuración

Definir RABBITMQ_USER, RABBITMQ_PASSWORD y RABBITMQ_ERLANG_COOKIE en .env. Usar un secreto aleatorio para la cookie; no subir .env. Las credenciales del servicio administrador y los productores/consumidores deben coincidir. Configurar en Users y Torneos RABBITMQ_ADDRESSES=rabbit1:5672,rabbit2:5672. La red blackout-network permite la comunicación interna. Los puertos publicados son solo loopback: 5672/15672 en rabbit1 y 5673/15673 en rabbit2. No abrirlos a Internet. Para la interfaz usar túnel SSH.

```bash
docker compose -f docker-compose.cluster.yml up -d --build --wait
docker compose -f docker-compose.cluster.yml exec -T rabbit1 rabbitmqctl cluster_status
```

## Datos y migración

El archivo docker-compose.yml anterior permanece disponible para la instalación de un solo nodo. No ejecutar ambos archivos sin resolver los puertos de host coincidentes. El clúster nuevo usa volúmenes nuevos; NO convierte automáticamente las colas y mensajes del servidor anterior. Antes de cambiar producción hay que inventariar las colas, terminar o conservar los mensajes pendientes, guardar respaldo y planificar el cambio de conexiones. No borrar el volumen anterior ni ejecutar down -v con datos reales. La limpieza de volúmenes del workflow se aplica solo a infraestructura desechable de CI.

## Tres colas y DLQ

| Cola funcional | Binding topic | DLQ | Clave direct de fallos |
|---|---|---|---|
| fan.notifications.queue | tournament.# | fan.notifications.dlq | fan.notifications.failed |
| academy.email.queue | academy.application.accepted / academy.application.rejected | academy.email.dlq | academy.email.failed |
| academy.audit.queue | academy.application.* | academy.audit.dlq | academy.audit.failed |

Users declara las colas después de iniciar el clúster. El exchange de negocio es blackout.events.exchange (topic), el de fallos blackout.dead-letter.exchange (direct). La configuración del clúster establece quorum como tipo por defecto para colas nuevas. Se debe comprobar el tipo y los miembros en la interfaz antes de la presentación; el archivo antiguo no cambia su tipo.

## Límite de disponibilidad

Dos nodos cumplen la cantidad pedida en la pauta, pero una cola quorum de dos miembros necesita ambos para tener mayoría. NO se debe afirmar que tolera perder uno de sus nodos. Tres miembros serían necesarios para tolerar uno caído. Además, si ambos contenedores viven en una EC2, la caída de esa máquina afecta al clúster completo. La prueba de fallo funcional se hace con el consumidor/SMTP y las DLQ, no simulando una disponibilidad que esta topología no ofrece.

Referencia: https://www.rabbitmq.com/docs/quorum-queues

## Administrador

El microservicio sigue la estructura Maven src/main/java, src/main/resources, src/test/java y pom.xml. Permite crear/eliminar colas, exchanges y bindings. Exige JWT Entra y rol Admin/Staff. Las demostraciones destructivas deben hacerse con nombres de prueba; no eliminar las colas de negocio para mostrar una operación CRUD.
