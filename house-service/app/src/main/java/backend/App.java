package backend;

import io.vertx.core.Vertx;
import backend.verticles.MqttClientVerticle;

import backend.verticles.MqttServerVerticle;
import backend.verticles.WebSocketServerVerticle;

public class App {

    public static void main(String[] args) throws Exception {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MqttServerVerticle());
        vertx.deployVerticle(new WebSocketServerVerticle());
        //Thread.sleep(3000);
        //vertx.deployVerticle(new MqttClientVerticle());
    }
}
