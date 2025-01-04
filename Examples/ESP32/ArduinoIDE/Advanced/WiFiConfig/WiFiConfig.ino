// SET MEMORY PROFILE WITH 2MB APP

#include <WiFi.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "00100000-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10100000-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SSID_UUID "10100001-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_PASSWORD_UUID "10100002-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_STATUS_UUID "10100003-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_CONNECT_ACTION_UUID "10100004-74ee-43ce-86b2-0dde20dcefd6"

// Default UUID mask for the BLE Manager app is ####face-####-####-####-############
// The segment "face" (case-insensitive) is used by BLE Manager to identify descriptors
#define CUSTOM_DESCRIPTOR_UUID "2000face-74ee-43ce-86b2-0dde20dcefd6"

String ssid = "";
String password = "";
bool connect = false;
BLECharacteristic *pCharacteristicStatus = NULL;

// Custom server callback class to handle connection events
class ServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *pServer) {
    Serial.println("Device connected.");
  }

  void onDisconnect(BLEServer *pServer) {
    Serial.println("Device disconnected. Restarting advertising...");
    // Restart advertising when a device disconnects
    BLEDevice::startAdvertising();
  }
};

class StringCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    BLEUUID receivedUUID = pCharacteristic->getUUID();
    if (receivedUUID.equals(BLEUUID(CHARACTERISTIC_SSID_UUID))) {
      ssid = pCharacteristic->getValue();
    } else if (receivedUUID.equals(BLEUUID(CHARACTERISTIC_PASSWORD_UUID))) {
      password = pCharacteristic->getValue();
    }
  }
};

class ButtonCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    BLEUUID receivedUUID = pCharacteristic->getUUID();
    if (receivedUUID.equals(BLEUUID(CHARACTERISTIC_CONNECT_ACTION_UUID))) {
      connect = true;
    }
  }
};

void setup() {
  Serial.begin(115200);

  // Initialize BLE device with a name
  BLEDevice::init("WiFi Device");

  // Create a BLE server and set its callback class
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  // Create a BLE service with a predefined UUID
  // Service handles by default 15 handles. Each BLE Characteristic takes 2 handles and each BLE Descriptor takes 1 handle.
  BLEService *pService = pServer->createService(BLEUUID(SERVICE_UUID), 20);  //15 handlers
  // If you add or remove characteristics, it may be necessary to forget the device
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.

  // Create a BLE characteristic for service name
  // The value of this characteristic will be displayed as the service name.
  // The "order" value determines the order in which the service appears in the BLE Manager app.
  // Only one "serviceName" characteristic is supported per service.
  // If a service contains multiple "serviceName" characteristics, one may be selected randomly.

  BLECharacteristic *pCharacteristicServiceName = pService->createCharacteristic(
    CHARACTERISTIC_SERVICE_NAME_UUID,
    BLECharacteristic::PROPERTY_READ);

  // Add a custom descriptor used by the BLE Manager app
  // Only one descriptor matching the mask is supported per characteristic.
  // If multiple descriptors match, one may be selected randomly.
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *serviceNameDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);  //200 bytes descriptor length
  // Set control configuration
  // JSON format. Keys are case-sensitive.
  serviceNameDescriptor->setValue(
    R"({"type":"serviceName", "order":1})");

  pCharacteristicServiceName->addDescriptor(serviceNameDescriptor);

  // Set an initial value for the characteristic
  pCharacteristicServiceName->setValue("Settings");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.

  //SSID
  BLECharacteristic *pCharacteristicSSID = pService->createCharacteristic(
    CHARACTERISTIC_SSID_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristicSSID->setCallbacks(new StringCharacteristicCallbacks());

  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *ssidDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);  //200 bytes descriptor length
  ssidDescriptor->setValue(
    R"({"type":"text", "order":1, "disabled":false, "label":"SSID", "maxBytes": 32})");  //maxBytes specifies the maximum number of bytes. It can be set up to 512 bytes. The default value is 512 bytes.

  pCharacteristicSSID->addDescriptor(ssidDescriptor);
  pCharacteristicSSID->setValue("");

  //PASSWORD
  BLECharacteristic *pCharacteristicPassword = pService->createCharacteristic(
    CHARACTERISTIC_PASSWORD_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristicPassword->setCallbacks(new StringCharacteristicCallbacks());

  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *passwordDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);  //200 bytes descriptor length
  passwordDescriptor->setValue(
    R"({"type":"password", "order":2, "disabled":false, "label":"Password", "maxBytes": 64})");  //maxBytes specifies the maximum number of bytes. It can be set up to 512 bytes. The default value is 512 bytes.

  pCharacteristicPassword->addDescriptor(passwordDescriptor);
  pCharacteristicPassword->setValue("");

  //STATUS
  pCharacteristicStatus = pService->createCharacteristic(
    CHARACTERISTIC_STATUS_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_INDICATE);

  pCharacteristicStatus->addDescriptor(new BLE2902());

  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *statusDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);  //200 bytes descriptor length
  statusDescriptor->setValue(
    R"({"type":"textView", "order":3, "disabled":false})");

  pCharacteristicStatus->addDescriptor(statusDescriptor);
  pCharacteristicStatus->setValue("Not Connected");

  //BUTTON
  BLECharacteristic *pCharacteristicConnectButton = pService->createCharacteristic(
    CHARACTERISTIC_CONNECT_ACTION_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristicConnectButton->setCallbacks(new ButtonCharacteristicCallbacks());

  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *connectButtonDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);  //200 bytes descriptor length
  connectButtonDescriptor->setValue(
    R"({"type":"button", "order":4, "label":"Connect", "disabled":false})");

  pCharacteristicConnectButton->addDescriptor(connectButtonDescriptor);

  pService->start();

  // Start BLE advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);  // Advertise the service UUID
  pAdvertising->setScanResponse(true);         // Enable scan response
  BLEDevice::startAdvertising();

  Serial.println("BLE server is running and advertising...");
}

void connectToWiFi() {
  if (connect) {
    connect = false;
    if (!ssid.isEmpty()) {
      if (WiFi.status() != WL_CONNECTED) {
        Serial.print("Connecting: ");
        Serial.println(ssid);

        pCharacteristicStatus->setValue("Connecting...");
        pCharacteristicStatus->indicate();
        WiFi.begin(ssid, password);

        if (WiFi.waitForConnectResult(10000) == WL_CONNECTED) {
          Serial.println("Connected:");
          Serial.print("IP: ");
          Serial.println(WiFi.localIP());
          pCharacteristicStatus->setValue(String("IP: ") + WiFi.localIP().toString());
        } else {
          Serial.println("Connection Failed");
          pCharacteristicStatus->setValue("Connection Failed");
        }

        pCharacteristicStatus->indicate();
      }
    } else {
      pCharacteristicStatus->setValue("Please set the SSID");
      pCharacteristicStatus->indicate();
    }
  }
}


void loop() {
  connectToWiFi();
}
