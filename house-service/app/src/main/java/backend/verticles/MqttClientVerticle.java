package backend.verticles;

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

                vertx.eventBus().consumer("mqttserver.to.mqttclient", message -> {
                    String payload = message.body().toString();
                    mqttClient.publish("room1/light", Buffer.buffer(payload), MqttQoS.AT_MOST_ONCE, false, false);
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
