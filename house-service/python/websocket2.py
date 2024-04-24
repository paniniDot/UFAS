import paho.mqtt.client as mqtt
import asyncio
import json
import websockets
import threading

queued_messages = asyncio.Queue()  # Usiamo una coda asincrona per gestire i messaggi

async def send_messages(websocket, path):
    while True:
        message = await queued_messages.get()  # Attendiamo un nuovo messaggio dalla coda
        print("Sending message to browser client")
        await websocket.send(message)

def on_message(client, userdata, msg):
    print("Received MQTT message")
    queued_messages.put_nowait(msg.payload.decode())  # Mettiamo il messaggio nella coda

def mqtt_listener():
    try:
        # Connect to MQTT broker
        client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)  # Utilizziamo la versione 3.1.1 del protocollo MQTT
        client.connect("localhost", 1883)
        client.subscribe("room1/cam")

        # Define callback function for received messages
        client.on_message = on_message

        # Start MQTT client loop
        client.loop_forever()
    except Exception as e:
        print("Error in MQTT listener:", e)

async def main():
    # Start WebSocket server
    websocket_server = await websockets.serve(send_messages, "localhost", 8080)

    # Start MQTT listener in a separate thread
    mqtt_thread = threading.Thread(target=mqtt_listener)
    mqtt_thread.start()

    # Wait for the WebSocket server to close
    await websocket_server.wait_closed()

# Run the main event loop indefinitely
asyncio.run(main())
