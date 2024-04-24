import threading
import asyncio
import configparser
import paho.mqtt.client as mqtt
from websockets.server import serve

connected = set()

async def ws_handler(websocket, path):
        print("Websocket connection established")
        connected.add(websocket)

async def send_message(message):
    for websocket in connected:
        await websocket.send(message)

async def setupWS():
    print("Setting up websocket server")
    async with serve(ws_handler, "localhost", 8080):
        await asyncio.Future()  # run forever

def on_message(client, userdata, msg):
	print("Received message from MQTT")
	message = msg.payload.decode()
	asyncio.run(send_message(message))

def mqtt_listener():
    try:
        # Connect to MQTT broker
        client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)  # Utilizziamo la versione 3.1.1 del protocollo MQTT
        client.connect("localhost", 1883)
        client.subscribe("room1/cam")

        # Define callback function for received messages
        client.on_message = on_message

        # Start MQTT client loop
        print("Starting MQTT listener")
        client.loop_forever()
    except Exception as e:
        print("Error in MQTT listener:", e)

if __name__ == '__main__':
    mqtt_thread = threading.Thread(target=mqtt_listener)
    mqtt_thread.start()
    asyncio.run(setupWS())
