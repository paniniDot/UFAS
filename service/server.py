from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse, FileResponse
from fastapi_mqtt.fastmqtt import FastMQTT
from fastapi_mqtt.config import MQTTConfig
from fastapi.staticfiles import StaticFiles
from firenet import FireNet
import uvicorn
import json
import base64
import os
from connectionmanager import ConnectionManager

host = "192.168.1.47"
port_http = 8080
port_mqtt = 1883

app = FastAPI()

# Serve the dashboard and FireNet directories
app.mount("/dashboard", StaticFiles(directory="service/dashboard"), name="dashboard")
app.mount("/FireNet", StaticFiles(directory="service/FireNet"), name="FireNet")

mqtt_config = MQTTConfig(host=host, port=port_mqtt)
fast_mqtt = FastMQTT(config=mqtt_config)
fast_mqtt.init_app(app)

manager = ConnectionManager()
firenet = FireNet()

devices = {}

@fast_mqtt.on_connect()
def connect(client, flags, rc, properties):
    print("Connected: ", client, flags, rc, properties)

@fast_mqtt.on_disconnect()
def disconnect(client, packet, exc=None):
    print("Disconnected")

@fast_mqtt.on_message()
async def message_handler(client, topic, payload, qos, properties):
    print(f"Received message on topic: {topic}")

@fast_mqtt.subscribe("house/output/#")
async def message_handler(client, topic, payload, qos, properties):
    data = json.loads(payload.decode())
    _, _, room = topic.split("/")
    if data["name"] == "cam":
        img = base64.b64decode(data["measure"].split(",")[1])
        prediction = firenet.predict(img)
        if prediction == 0:
            print("Fire detected!")
        else:
            print("No fire detected")
    data["room"] = room
    updated_data = json.dumps(data)
    await manager.broadcast(updated_data)

@app.get("/")
async def root():
    return FileResponse('service/dashboard/index.html')

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            print(f"Message received: {data}")
            # {name: house/input/roomX/light_roll measure: on_off}
            data = json.loads(data)
            fast_mqtt.publish("house/input/"+data["room"], data["measure"])
    except WebSocketDisconnect:
        await manager.disconnect(websocket)

if __name__ == "__main__":
    uvicorn.run(app, host=host, port=port_http)
