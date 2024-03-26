import asyncio
import websockets

async def handle_incoming_web_client_message(websocket, path):
    async for message in websocket:
        mqtt_websocket = await websockets.connect('ws://localhost:1883')
        await mqtt_websocket.send(message)
        await mqtt_websocket.close()

async def handle_incoming_mqtt_message(websocket, path):
    mqtt_websocket = await websockets.connect('ws://localhost:1884')
    async for mqtt_message in mqtt_websocket:
        await websocket.send(mqtt_message)
    await mqtt_websocket.close()

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
