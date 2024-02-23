package room;

import io.vertx.core.Vertx;
import room.verticles.MqttClientVerticle;
import room.verticles.MqttServerVerticle;
import room.verticles.WebSocketServerVerticle;

public class App {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MqttServerVerticle());
        vertx.deployVerticle(new WebSocketServerVerticle());
        vertx.deployVerticle(new MqttClientVerticle());
    }
}
