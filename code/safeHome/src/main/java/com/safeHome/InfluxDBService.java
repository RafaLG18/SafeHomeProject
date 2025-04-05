package com.safeHome;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient; // <-- Nova importação
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;

public class InfluxDBService {
    private final String url;
    private final String token;
    private final String org;
    private final String bucket;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private InfluxDBClient influxClient;

    public InfluxDBService(String url, String token, String org, String bucket) {
        this.url = url;
        this.token = token;
        this.org = org;
        this.bucket = bucket;
        this.influxClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }

    public void saveData(String topic, String payload) {
        try {
            JsonNode jsonNode = jsonMapper.readTree(payload);
            String sensorId = jsonNode.get("sensorId").asText();
            double value = jsonNode.get("value").asDouble();

            Point point = Point.measurement("sensor_data")
                .addTag("topic", topic)
                .addTag("sensor_id", sensorId)
                .addField("value", value)
                .addField("status", "received");

            // Substituição do getWriteApi() obsoleto:
            WriteApiBlocking writeApi = influxClient.getWriteApiBlocking(); // <-- Nova abordagem
            writeApi.writePoint(point);
            
            System.out.println("[InfluxDB] Dados salvos: " + point);

        } catch (Exception e) {
            System.err.println("[InfluxDB] Erro ao salvar dados: " + e.getMessage());
        }
    }

    public void close() {
        if (influxClient != null) {
            influxClient.close();
        }
    }
}