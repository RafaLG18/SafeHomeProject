#include "WiFi.h"
#include <PubSubClient.h>
#include <ArduinoJson.h>

const char* ssid = "DTEL-RAFAEL";
const char* password = "25200616";

const char* mqtt_server = "192.168.1.14";
const int mqtt_port = 1883;
const char* mqtt_user = "safehmoe";
const char* mqtt_pass = "safehome";

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  
  randomSeed(analogRead(0));
  
  connectToWifi();
  
  // Configura o servidor MQTT antes de tentar conectar
  client.setServer(mqtt_server, mqtt_port);
  
  connectToMqtt();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi desconectado! Reconectando...");
    connectToWifi();
  }
  if (!client.connected()) {
    connectToMqtt();
  }
  client.loop(); // Mantém a conexão MQTT ativa

  publishTemperature();
  publishHumidity();
  publishPPM();
  delay(5000);

  
}

void connectToWifi() {
  Serial.print("Conectando a ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("\nWiFi conectado!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());
}

void connectToMqtt() {
  Serial.println("Conectando ao MQTT...");
  
  // Usando MAC address como clientId (único e mais confiável)
  String clientId = "ESP32-" + WiFi.macAddress();
  clientId.replace(":", ""); // Remove os dois pontos
  
  if (client.connect(clientId.c_str(), mqtt_user, mqtt_pass)) {
    Serial.println("Conectado ao MQTT!");
    Serial.print("ClientID: ");
    Serial.println(clientId);
  } else {
    Serial.print("Falha na conexão MQTT, rc=");
    Serial.print(client.state());
    Serial.println(" Tentando novamente em 5 segundos...");
    delay(5000);
  }
}


int generateNumbers(int number){
 return random(number);
}

int generateTempData(){
  return generateNumbers(33);
}

int generateHumidityData(){
  return generateNumbers(100);
}

int generatePpmData(){
  return generateNumbers(1000);
}

void publishTemperature(){
  StaticJsonDocument<200> temp;
  
  temp["sensorId"]="temperatura";
  temp["value"]=generateTempData();

  char buffer[256];
  
  serializeJson(temp,buffer);

  client.publish("esp32/sensores",buffer);
}

void publishHumidity(){
  StaticJsonDocument<200> hum;
  
  hum["sensorId"]="umidade";
  hum["value"]=generateHumidityData();

  char buffer[256];
  
  serializeJson(hum,buffer);

  client.publish("esp32/sensores",buffer);
}

void publishPPM(){
  StaticJsonDocument<200> ppm;
  
  ppm["sensorId"]="concentracao";
  ppm["value"]=generatePpmData();

  char buffer[256];
  
  serializeJson(ppm,buffer);

  client.publish("esp32/sensores",buffer);
}
