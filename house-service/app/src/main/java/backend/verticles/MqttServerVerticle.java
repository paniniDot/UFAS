package backend.verticles;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import java.util.ArrayList;
import java.util.List;

public class MqttServerVerticle extends AbstractVerticle {

    private List<MqttEndpoint> endpoints = new ArrayList<>();

    @Override
    public void start() {
        MqttServerOptions mqttOptions = new MqttServerOptions()
                .setHost("192.168.1.52")
                .setPort(1883)
                .setMaxMessageSize(Integer.MAX_VALUE)
                .setReceiveBufferSize(Integer.MAX_VALUE);

        MqttServer mqttServer = MqttServer.create(vertx, mqttOptions);

        vertx.eventBus().consumer("webserver.to.mqttserver", message -> {
            String payload = message.body().toString();
            log("Received message from web server: " + payload);
            handleWebToMqttMessage(payload);
        });

        mqttServer.endpointHandler(this::handleEndpoint).listen(ar -> {
            if (ar.succeeded()) {
                log("MQTT server started on port " + ar.result().actualPort());
            } else {
                log("MQTT server error " + ar.cause().getMessage());
                ar.cause().printStackTrace(); 
            }
        });
    }

    private void handleWebToMqttMessage(String payload) {
        if (!endpoints.isEmpty()) {
            for (MqttEndpoint endpoint : endpoints) {
                endpoint.publish("mqttserver.to.mqttclient", Buffer.buffer(payload.getBytes()), MqttQoS.AT_LEAST_ONCE, false, false);
            }
        }
    }
    
    private void handleEndpoint(MqttEndpoint endpoint) {
        log("connected client " + endpoint.clientIdentifier());
        endpoint.publishHandler(message -> {
            System.out.println("Just received message on [" + message.topicName() + "] payload [" + message.payload() + "] with QoS [" + message.qosLevel() + "]");
            String mqttPayload = message.payload().toString();
            vertx.eventBus().publish("mqttserver.to.webserver", mqttPayload);
        });
        endpoint.accept(false);
        endpoints.add(endpoint);
    }

    private void log(String message) {
        System.out.println("[MQTT Server] " + message);
    }
}

