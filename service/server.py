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

app.mount("/dashboard", StaticFiles(directory="service/dashboard"), name="dashboard")
app.mount("/FireNet", StaticFiles(directory="service/FireNet"), name="FireNet")

mqtt_config = MQTTConfig(host=host, port=port_mqtt)
fast_mqtt = FastMQTT(config=mqtt_config)
fast_mqtt.init_app(app)

manager = ConnectionManager()
firenet = FireNet()

devices = {}

def create_handler(room, device_type):
    async def handler(client, topic, payload, qos, properties):
        print(f"Received message on topic: {topic}")
        data = payload.decode()
        
        if device_type == "cam":
            img = base64.b64decode(json.loads(data)["measure"].split(",")[1])
            prediction = firenet.predict(img)
            if prediction == 0:
                print("Fire detected!")
            else:
                print("No fire detected")
        
        await manager.broadcast(data)

    return handler

@fast_mqtt.on_connect()
def connect(client, flags, rc, properties):
    print("Connected: ", client, flags, rc, properties)

@fast_mqtt.on_disconnect()
def disconnect(client, packet, exc=None):
    print("Disconnected")

@fast_mqtt.on_message()
async def message_handler(client, topic, payload, qos, properties):
    print(f"Received message on topic: {topic}")

# funzione che consente di fare hand-shaking fra gli esp e il server  
@fast_mqtt.subscribe("room_config")
async def room_config_handler(client, topic, payload, qos, properties):
    topic_str = payload.decode()
    if topic_str.startswith("room"):
        room, device_type = topic_str.split("/")
        if room not in devices:
            devices[room] = {}

        if device_type not in devices[room]:
            devices[room][device_type] = True
            handler = create_handler(room, device_type)
            await fast_mqtt.subscribe(topic_str)(handler)

with open('service/dashboard/house.html', "r") as html_file:
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
        await manager.disconnect(websocket)

if __name__ == "__main__":
    uvicorn.run(app, host=host, port=port_http)
