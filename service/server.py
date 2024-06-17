from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
from fastapi_mqtt.fastmqtt import FastMQTT
from fastapi_mqtt.config import MQTTConfig
from fastapi.staticfiles import StaticFiles
from firenet import FireNet
import uvicorn
import json
import base64
import os
from connectionmanager import ConnectionManager

host = "192.168.2.226"

port_http = 8080

port_mqtt = 1883

app = FastAPI()

print(os.getcwd())

app.mount("/dashboard", StaticFiles(directory="service/dashboard"), name="dashboard")
app.mount("/FireNet", StaticFiles(directory="service/FireNet"), name="FireNet")

mqtt_config = MQTTConfig(host=host, port=port_mqtt)

fast_mqtt = FastMQTT(config=mqtt_config)

fast_mqtt.init_app(app)

manager = ConnectionManager()
firenet = FireNet()

@fast_mqtt.on_connect()
def connect(client, flags, rc, properties):
    print("Connected: ", client, flags, rc, properties)

@fast_mqtt.on_disconnect()
def disconnect(client, packet, exc=None):
    print("Disconnected")

@fast_mqtt.subscribe("room1/cam")
async def cam_handler(client, topic, payload, qos, properties):
    print("Received message on topic: ", topic)
    img = base64.b64decode(json.loads(payload.decode())["measure"].split(",")[1])
    prediction = firenet.predict(img)
    if prediction == 0:
        print("Fire detected!")
    else:
        print("No fire detected")
    await manager.broadcast(payload.decode())

@fast_mqtt.subscribe("room1/roll")
async def measure_handler(client, topic, payload, qos, properties):
    print("Received message on topic: ", topic)
    await manager.broadcast(payload.decode())

@fast_mqtt.subscribe("room1/light")
async def light_handler(client, topic, payload, qos, properties):
    print("Received message on topic: ", topic)
    await manager.broadcast(payload.decode())

with open('service/dashboard/room.html', "r") as html_file:
    html = html_file.read()

@app.get("/")
async def root():
    return HTMLResponse(html)

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            print(f"Message received: {data}")
    except WebSocketDisconnect:
        manager.disconnect(websocket)





if __name__ == "__main__":
    uvicorn.run(app, host=host, port=port_http)
