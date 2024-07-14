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
import firebase_admin
from firebase_admin import db
import threading
import time
from connectionmanager import ConnectionManager

firebase_credentials = {

}

options = {
    "databaseURL" : "https://ufas-f1ebf-default-rtdb.europe-west1.firebasedatabase.app",
    "projectId" : "ufas-f1ebf",
    "storageBucket" : "ufas-f1ebf.appspot.com",
}

firebase = firebase_admin.initialize_app(firebase_admin.credentials.Certificate(firebase_credentials), options)
database = db.reference()

host = "192.168.1.156"
port_http = 8080
port_mqtt = 1883
house = "house1"

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
    data["room"] = room
    if 'room_states' not in globals():
        global room_states
        room_states = {}
    if room not in room_states:
        room_states[room] = {'fire_detected': False, 'co2_level': 0}
    if data["name"] == "cam":
        img = base64.b64decode(data["measure"].split(",")[1])
        prediction = firenet.predict(img)
        room_states[room]['fire_detected'] = (prediction == 0)
    elif data["name"] == "Co2":
        room_states[room]['co2_level'] = float(data["measure"])

    fire_detected = room_states[room]['fire_detected'] and room_states[room]['co2_level'] > 400
    fire_status = "yes" if fire_detected else "no"
    database.child("alarm").push({
        "measure": fire_status,
        "house": house,
    })
    database.child(house).child(room).child(data["name"]).push({
        "measure": data["measure"],
        "timestamp": data["timestamp"]
    })
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
            data = json.loads(data)
            fast_mqtt.publish("house/input/"+data["room"]+"/"+data["name"], data["measure"])
    except WebSocketDisconnect:
        await manager.disconnect(websocket)

def monitor_firebase():
    while True:
        try:
            # Recupera gli ultimi 100 record
            alarm_ref = database.child("alarm").order_by_key().limit_to_last(100).get()
            if alarm_ref is not None:
                for key, val in alarm_ref.items():
                    if val["measure"] == "yes":
                        print("Fumo rilevato! Prendi le necessarie precauzioni!")
                        # Aggiungi qui la logica per notificare l'utente o eseguire altre azioni.
                        break  # Esci dal loop se viene rilevato il fumo
        except Exception as e:
            print(f"Errore durante il monitoraggio di Firebase: {e}")
        time.sleep(10)  # Attende 10 secondi prima di effettuare un'altra query.


                   
if __name__ == "__main__":
    monitor_thread = threading.Thread(target=monitor_firebase, daemon=True)
    monitor_thread.start()
    uvicorn.run(app, host=host, port=port_http)
