package backend.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

public class MqttServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
        MqttServerOptions mqttOptions = new MqttServerOptions()
                .setHost("192.168.1.52")
                .setPort(1883);

        MqttServer mqttServer = MqttServer.create(vertx, mqttOptions);

        vertx.eventBus().consumer("webserver.to.mqttserver", message -> {
            String payload = message.body().toString();
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
        vertx.eventBus().publish("mqttserver.to.mqttclient", payload); // Changed the address here
    }
    

    private void handleEndpoint(MqttEndpoint endpoint) {
        log("connected client " + endpoint.clientIdentifier());
        endpoint.publishHandler(message -> {
            System.out.println("Just received message on [" + message.topicName() + "] payload [" + message.payload() + "] with QoS [" + message.qosLevel() + "]");
            String mqttPayload = message.payload().toString();
            vertx.eventBus().publish("mqttserver.to.webserver", mqttPayload);
        });
        endpoint.accept(false);
    }

    private void log(String message) {
        System.out.println("[MQTT Server] " + message);
    }
}

