### author: Roberto Vezzani
import configparser

import paho.mqtt.client as mqtt

class Bridge():

	def __init__(self):
		self.config = configparser.ConfigParser()
		self.config.read('config.ini')

		self.pubtopic = self.config.get("MQTT","PubTopic", fallback= "room1/cam")
		self.setupMQTT()

	def setupMQTT(self):
		self.clientMQTT = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
		self.clientMQTT.on_connect = self.on_connect
		self.clientMQTT.on_message = self.on_message
		print("Connecting to MQTT broker...")
		self.clientMQTT.connect(
			self.config.get("MQTT","Server", fallback= "192.168.1.66"),
			self.config.getint("MQTT","Port", fallback= 1883),
			60)

		self.clientMQTT.loop_start()

	def on_connect(self,client, userdata, flags, reason_code, properties):
		print("Connected with result code " + str(reason_code))
		
		# Subscribing in on_connect() means that if we lose the connection and
		# reconnect then subscriptions will be renewed.
		t = self.config.get("MQTT","SubTopic", fallback= "room1/cam")
		
		self.clientMQTT.subscribe(t)
		print("Subscribed to " + t)

	# The callback for when a PUBLISH message is received from the server.
	def on_message(self, client, userdata, msg):
		print("Received message from MQTT")
		print(msg.topic + " " + str(msg.payload))


if __name__ == '__main__':
	br=Bridge()
	br.clientMQTT.loop_forever()
	

