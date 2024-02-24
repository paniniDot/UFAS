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

        mqttServer.endpointHandler(endpoint -> {
            endpoint.publishHandler(message -> {
                String payload = message.payload().toString();
                vertx.eventBus().publish("mqtt.message", payload);
            });
        });

        mqttServer.listen(ar -> {
            if (ar.succeeded()) {
                System.out.println("MQTT server started on port " + ar.result().actualPort());
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
