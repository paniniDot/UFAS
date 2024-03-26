import asyncio
import websockets

async def handle_incoming_web_client_message(websocket, path):
    async for message in websocket:
        mqtt_message = message
        await webserver_to_mqttserver_queue.put(mqtt_message)

async def handle_incoming_mqtt_message(websocket, path):
    while True:
        mqtt_message = await mqttserver_to_webserver_queue.get()
        await websocket.send(mqtt_message)

async def handle_connection(websocket, path):
    if path == '/webclient':
        await handle_incoming_web_client_message(websocket, path)
    elif path == '/mqttserver':
        await handle_incoming_mqtt_message(websocket, path)

async def main():
    async with websockets.serve(handle_connection, "localhost", 8080):
        print("Server started on port 8080")
        await asyncio.Future()  # Serve forever

if __name__ == "__main__":
    asyncio.run(main())
