package room.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

public class MqttServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
        MqttServerOptions mqttOptions = new MqttServerOptions()
                .setHost("localhost")
                .setPort(1883);

        MqttServer mqttServer = MqttServer.create(vertx, mqttOptions);

        vertx.eventBus().consumer("web.to.mqtt", message -> {
            String payload = (String) message.body();
            log("Received message on web.to.mqtt: " + payload);
        });

        mqttServer.endpointHandler(endpoint -> {
            log("connected client " + endpoint.clientIdentifier());
            endpoint.publishHandler(message -> {
                log("Just received message on [" + message.topicName() + "] payload [" + message.payload().toString() + "] with QoS [" + message.qosLevel() + "]");
                String payload = message.payload().toString();
                vertx.eventBus().publish("mqtt.to.web", payload);
            });
            endpoint.accept(false);
        });

        mqttServer.listen(ar -> {
            if (ar.succeeded()) {
                log("MQTT server started on port " + ar.result().actualPort());
            } else {
                log("MQTT server error on start" + ar.cause().getMessage());
            }
        });
    }

    private void log(String message) {
        System.out.println("[MQTT Server] " + message);
    }
}
