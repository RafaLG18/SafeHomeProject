package com.safeHome;

public class Main {
    public static void main(String[] args) {
        // Configurações do InfluxDB
        String influxUrl = "http://localhost:8086";
        String influxToken = "token-safeHome#";
        String influxOrg = "safehome";
        String influxBucket = "safehome-bucket";

        // Inicializa o cliente InfluxDB
        InfluxDBService influxClient = new InfluxDBService(influxUrl, influxToken, influxOrg, influxBucket);

        // Inicializa o Subscriber MQTT (injeta o InfluxDBClient)
        MqttSubscriber subscriber = new MqttSubscriber(influxClient);
        subscriber.start();

        // Adicione um shutdown hook para fechar o cliente InfluxDB
        Runtime.getRuntime().addShutdownHook(new Thread(influxClient::close));
    }
}