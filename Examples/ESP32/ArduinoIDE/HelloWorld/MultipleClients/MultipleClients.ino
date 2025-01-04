/*
This example demonstrates the ability to connect multiple clients at the same time.
It is only necessary to restart the transmission of advertising packets after connection.

void onConnect(BLEServer *pServer) {
    Serial.println("Device connected.");
    BLEDevice::startAdvertising();
}
*/

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID                       "01000020-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID   "11000020-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TEXT_UUID           "11000021-74ee-43ce-86b2-0dde20dcefd6"

// Default UUID mask for the BLE Manager app is ####face-####-####-####-############
// The segment "face" (case-insensitive) is used by BLE Manager to identify descriptors
#define CUSTOM_DESCRIPTOR_UUID            "2000face-74ee-43ce-86b2-0dde20dcefd6"

// Custom server callback class to handle connection events
class ServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *pServer) {
    Serial.println("Device connected.");
    BLEDevice::startAdvertising();
  }

  void onDisconnect(BLEServer *pServer) {
    Serial.println("Device disconnected. Restarting advertising...");
    // Restart advertising when a device disconnects
    BLEDevice::startAdvertising();
  }
};

class StringCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    String value = pCharacteristic->getValue();
    Serial.print("Received value: ");
    if (!value.isEmpty()) {
      Serial.println(value.c_str());
    }
  }
};

BLECharacteristic *pCharacteristicText = NULL;

void setup() {
  Serial.begin(115200);

  // Initialize BLE device with a name
  BLEDevice::init("Interactive Device");

  // Create a BLE server and set its callback class
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  // Create a BLE service with a predefined UUID
  // Service handles by default 15 handles. Each BLE Characteristic takes 2 handles and each BLE Descriptor takes 1 handle.
  BLEService *pService = pServer->createService(BLEUUID(SERVICE_UUID), 15);  //15 handlers
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
  BLEDescriptor *serviceNameDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200); //200 bytes descriptor length
  // Set control configuration
  // JSON format. Keys are case-sensitive.
  serviceNameDescriptor->setValue(
    R"({"type":"serviceName", "order":1})");

  pCharacteristicServiceName->addDescriptor(serviceNameDescriptor);

  // Set an initial value for the characteristic
  pCharacteristicServiceName->setValue("My Service Name");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.

  // Text field: editable control for string characteristics
  // Supports UTF-8 encoding
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  pCharacteristicText = pService->createCharacteristic(
    CHARACTERISTIC_TEXT_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_INDICATE
  );

  pCharacteristicText->setCallbacks(new StringCharacteristicCallbacks());

  pCharacteristicText->addDescriptor(new BLE2902());

  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *textDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200); //200 bytes descriptor length
  textDescriptor->setValue(
    R"({"type":"text", "order":1, "disabled":false, "label":"My Text Field Label", "maxBytes": 80})"); //maxBytes specifies the maximum number of bytes. It can be set up to 512 bytes. The default value is 512 bytes.

  pCharacteristicText->addDescriptor(textDescriptor);
  pCharacteristicText->setValue("Hello, World!");
  pService->start();

  // Start BLE advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);  // Advertise the service UUID
  pAdvertising->setScanResponse(true);         // Enable scan response
  BLEDevice::startAdvertising();

  Serial.println("BLE server is running and advertising...");
}

void loop() {
  delay(1000);
  pCharacteristicText->setValue("Hello, World!");
  pCharacteristicText->indicate();
  delay(1000);
  /*
  Indications send data only up to the size of MTU - 3.
  The default MTU is usually 20 bytes.
  If you require a larger range, enable maximum MTU negotiation in the app's device settings.
  */
  pCharacteristicText->setValue("Hello, My Friend!   This will likely be truncated.");
  pCharacteristicText->indicate();
}
