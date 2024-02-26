package room.verticles;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttClient;
import io.vertx.core.buffer.Buffer;

public class MqttClientVerticle extends AbstractVerticle {

    @Override
    public void start() {
        MqttClient mqttClient = MqttClient.create(vertx);

        mqttClient.connect(1883, "localhost", ar -> {
            if (ar.succeeded()) {
                log("Connected to MQTT server");

                vertx.eventBus().consumer("mqtt.message", message -> {
                    log("Received message from event bus: " + message.body());
                    String payload = (String) message.body();
                    mqttClient.publish("mqtt/topic", Buffer.buffer(payload), MqttQoS.AT_MOST_ONCE, false, false);
                });
            } else {
                log("Unable to connect " + ar.cause().getMessage());
            }
        });
    }

    private void log(String message) {
        System.out.println("[MQTT Client] " + message);
    }
}
