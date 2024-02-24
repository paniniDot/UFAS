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
                System.out.println("Connected to MQTT server");

                vertx.eventBus().consumer("mqtt.message", message -> {
                    // Forward messages from WebSocket to ESP
                    String payload = (String) message.body();
                    mqttClient.publish("mqtt/topic", Buffer.buffer(payload), MqttQoS.AT_MOST_ONCE, false, false);
                });
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
