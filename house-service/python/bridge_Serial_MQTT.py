import configparser
import paho.mqtt.client as mqtt
import asyncio
import websockets
from websockets.server import serve

class Bridge():

    def __init__(self):
        self.config = configparser.ConfigParser()
        self.config.read('config.ini')

        self.pubtopic = self.config.get("MQTT","PubTopic", fallback= "room1/cam")
        self.connected = set()

    async def setupWS(self):
        self.serverWS = await serve(self.ws_handler, "localhost", 8080)
        print("Websocket server started on localhost:8080")

    async def ws_handler(self, websocket):
        self.connected.add(websocket)

    def setupMQTT(self):
        self.clientMQTT = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
        self.clientMQTT.on_connect = self.on_connect
        self.clientMQTT.on_message = self.on_message
        print("Connecting to MQTT broker...")
        self.clientMQTT.connect(
            self.config.get("MQTT","Server", fallback= "192.168.1.51"),
            self.config.getint("MQTT","Port", fallback= 1883),
            60)

        self.clientMQTT.loop_start()

    def on_connect(self, client, userdata, flags, reason_code, properties):
        print("Connected with result code " + str(reason_code))
        t = self.config.get("MQTT","SubTopic", fallback= "room1/cam")
        self.clientMQTT.subscribe(t)
        print("Subscribed to " + t)

    def on_message(self, client, userdata, msg):
        print("Received message from MQTT")
        print(self.connected)
        for ws in self.connected:
            ws.send(msg.payload.decode())

async def main():
    br = Bridge()
    await br.setupWS()
    br.setupMQTT()
    while True:
        await asyncio.sleep(1)

if __name__ == '__main__':
    asyncio.run(main())
