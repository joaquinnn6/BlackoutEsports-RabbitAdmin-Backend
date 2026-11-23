"""Lectura del estado del clúster; no modifica colas ni mensajes."""
import base64
import json
import os
import urllib.request
import time

credentials = os.environ.get("RABBITMQ_USER", "blackout") + ":" + os.environ["RABBITMQ_PASSWORD"]
request = urllib.request.Request("http://127.0.0.1:15672/api/nodes")
request.add_header("Authorization", "Basic " + base64.b64encode(credentials.encode()).decode())
for attempt in range(15):
    with urllib.request.urlopen(request, timeout=10) as response:
        nodes = json.load(response)
    if {node["name"] for node in nodes if node["running"]} == {"rabbit@rabbit1", "rabbit@rabbit2"}:
        break
    time.sleep(2)
else:
    raise AssertionError("Los dos nodos no forman un clúster operativo")
print("Clúster verificado: rabbit1 y rabbit2 están agrupados y activos")
