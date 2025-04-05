package com.safeHome;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscriber {
    private static final String BROKER = "tcp://localhost:1883";
    private static final String CLIENT_ID = "JavaSubscriber";
    private static final String TOPIC = "esp32/sensores";
    private static final String USERNAME = "safehmoe";
    private static final String PASSWORD = "safehome";

    private InfluxDBService influxService;  // Injeção de dependência

    public MqttSubscriber(InfluxDBService influxService) {
        this.influxService = influxService;
    }

    public void start() {
        try {
            MqttClient mqttClient = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("[MQTT] Conexão perdida! Tentando reconectar...");
                    reconnect(mqttClient, options);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    System.out.println("[MQTT] Mensagem recebida: " + payload);
                    influxService.saveData(topic, payload);  // Delega para o InfluxDBClient
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            mqttClient.connect(options);
            mqttClient.subscribe(TOPIC, 1);
            System.out.println("[MQTT] Subscriber rodando. Tópico: " + TOPIC);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void reconnect(MqttClient client, MqttConnectOptions options) {
        while (!client.isConnected()) {
            try {
                client.connect(options);
                client.subscribe(TOPIC, 1);
            } catch (MqttException e) {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }
}