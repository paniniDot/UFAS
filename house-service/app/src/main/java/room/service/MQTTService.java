package room.service;

import java.util.ArrayList;
import java.util.List;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;
import jssc.SerialPortException;

public class MQTTService {

	public MQTTService(int port, String ip, SerialService serial) {
		Vertx vertx = Vertx.vertx();
		final List<MqttEndpoint> endpoints = new ArrayList<>();
		MqttServerOptions options = new MqttServerOptions().setHost(ip).setPort(port);
		MqttServer mqttServer = MqttServer.create(vertx, options);
		mqttServer.endpointHandler(endpoint -> {
			log("client connesso: " + endpoint.clientIdentifier());
			endpoints.add(endpoint);

			endpoint.subscribeHandler(subscribe -> {
				List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
				for (io.vertx.mqtt.MqttTopicSubscription s : subscribe.topicSubscriptions()) {
					log("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
					reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
				}
				endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
			});

			endpoint.unsubscribeHandler(unsubscribe -> {

				for (String t : unsubscribe.topics()) {
					System.out.println("Unsubscription for " + t);
				}
				endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
			});

			endpoint.publishHandler(message -> {
				try {
					serial.sendMsg(message.payload().toString());
				} catch (SerialPortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			endpoint.accept(false);
		}).listen().onComplete(ar -> {
			if (ar.succeeded()) {
				log("MQTT server is listening on port " + ar.result().actualPort());
			} else {
				log("Error on starting the server");
			}
		});
	}

	private void log(final String msg) {
		System.out.println("[MQTT SERVER] " + msg);
	}

}