from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
from fastapi_mqtt.fastmqtt import FastMQTT
from fastapi_mqtt.config import MQTTConfig
import asyncio
import uvicorn
from connectionmanager import ConnectionManager

app = FastAPI()

mqtt_config = MQTTConfig(host="192.168.1.67", port=1883)

fast_mqtt = FastMQTT(config=mqtt_config)

fast_mqtt.init_app(app)

manager = ConnectionManager()

@fast_mqtt.on_connect()
def connect(client, flags, rc, properties):
    print("Connected: ", client, flags, rc, properties)

@fast_mqtt.on_disconnect()
def disconnect(client, packet, exc=None):
    print("Disconnected")

@fast_mqtt.subscribe("room1/cam")
async def cam_handler(client, topic, payload, qos, properties):
    print("Received message on topic: ", topic)
    await manager.broadcast(payload.decode())

@fast_mqtt.subscribe("room1/roll")
async def measure_handler(client, topic, payload, qos, properties):
    print("Received message on topic: ", topic)
    await manager.broadcast(payload.decode())

@fast_mqtt.subscribe("room1/light")
async def light_handler(client, topic, payload, qos, properties):
    print("Received message on topic: ", topic)
    await manager.broadcast(payload.decode())


html = """
<!DOCTYPE html>
<html>
<head> 
    <title>WebSocket Test</title>
</head>
<body>
    <h1>WebSocket Webcam</h1>
    
    <img id="image" src="" alt="Live Webcamp">
    
    <script>
        document.addEventListener("DOMContentLoaded", function(event) {
            var ws = new WebSocket("ws://192.168.1.67:8080/ws");
            ws.onopen = function(event) {
                console.log("WebSocket connection established.");
            };
            ws.onmessage = function(event) {
                console.log("Message received:", event.data);
                // Parse message JSON
                var message = JSON.parse(event.data);
                // Get the base64 encoded image data
                if (message.name === "camera") {
                    var imageData = message.measure;
                    // Update the image src with the new data
                    document.getElementById("image").src = imageData;            
                }
            };
            ws.onerror = function(event) {
                console.error("WebSocket error:", event);
            };
            ws.onclose = function(event) {
                console.log("WebSocket connection closed.");
            };
            setInterval(function() {
                ws.send("Hello, server!");
            }, 1000);
        });
    </script>
</body>
</html>

"""

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
    uvicorn.run(app, host="192.168.1.67", port=8080)
