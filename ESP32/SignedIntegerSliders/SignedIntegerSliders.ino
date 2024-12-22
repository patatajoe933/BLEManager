#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "000000D0-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "100000D0-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT8_UUID "100000D1-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT16_UUID "100000D2-74ee-43ce-86b2-0dde20dcefd6"
// Default UUID mask for the BLE Manager app is ####face-####-####-####-############
// The segment "face" (case-insensitive) is used by BLE Manager to identify descriptors
#define CUSTOM_DESCRIPTOR_UUID "2000face-74ee-43ce-86b2-0dde20dcefd6"

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

class IntegerCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    const uint8_t *data = pCharacteristic->getData();
    size_t dataLength = pCharacteristic->getLength();

    Serial.print("Received value: ");

    if (dataLength > 0) {
      switch (dataLength) {
        case sizeof(int8_t):  // 8-bit integer
          {
            int8_t intValue;
            memcpy(&intValue, data, sizeof(int8_t));
            Serial.println(intValue);
          }
          break;
        case sizeof(int16_t):  // 16-bit integer
          {
            int16_t intValue;
            memcpy(&intValue, data, sizeof(int16_t));
            Serial.println(intValue);
          }
          break;
        case sizeof(int32_t):  // 32-bit integer
          {
            int32_t intValue;
            memcpy(&intValue, data, sizeof(int32_t));
            Serial.println(intValue);
          }
          break;
        case sizeof(int64_t):  // 64-bit integer
          {
            int64_t intValue;
            memcpy(&intValue, data, sizeof(int64_t));
            Serial.println(intValue);
          }
          break;
        default:
          Serial.println("Invalid data length!");
      }
    } else {
      Serial.println("Empty value received!");
    }
  }
};

void setup() {
  Serial.begin(115200);

  // Initialize BLE device with a name
  BLEDevice::init("BLE Device");

  // Create a BLE server and set its callback class
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  // Create a BLE service with a predefined UUID
  // Service handles by default 15 handles. Each BLE Characteristic takes 2 handles and each BLE Descriptor takes 1 handle.
  BLEService *pService = pServer->createService(BLEUUID(SERVICE_UUID), 20);  //20 Handles
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

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
  BLEDescriptor *serviceNameDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  // Set control configuration
  // JSON format. Keys are case-sensitive.
  serviceNameDescriptor->setValue(
    R"({"type":"serviceName", "order":1})");

  pCharacteristicServiceName->addDescriptor(serviceNameDescriptor);

  // Set an initial value for the characteristic
  pCharacteristicServiceName->setValue("Signed Integer Sliders");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

  // sint8slider: editable slider control for signed byte characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicSInt8 = pService->createCharacteristic(
    CHARACTERISTIC_SINT8_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt8->setCallbacks(new IntegerCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt8Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt8Descriptor->setValue(
    R"({"type":"sint8slider", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-50, "maxInt":50, "stepInt":1})"); //Defaults: minInt = 0, maxInt = 100, stepInt: 1

  pCharacteristicSInt8->addDescriptor(sInt8Descriptor);
  uint8_t sint8 = -10;
  pCharacteristicSInt8->setValue(&sint8, sizeof(uint8_t));

  // sint16slider: Editable slider control for signed int16 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicSInt16 = pService->createCharacteristic(
    CHARACTERISTIC_SINT16_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt16->setCallbacks(new IntegerCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt16Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt16Descriptor->setValue(
    R"({"type":"sint16slider", "order":2, "disabled":false, "label":"Signed Int16", "minInt":0, "maxInt":100, "stepInt":2})"); //Defaults: minInt = 0, maxInt = 100, stepInt: 1
  pCharacteristicSInt16->addDescriptor(sInt16Descriptor);
  int16_t sint16 = 50;
  pCharacteristicSInt16->setValue((uint8_t *)&sint16, sizeof(int16_t));

  // Start the BLE service
  pService->start();

  // Start BLE advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);  // Advertise the service UUID
  pAdvertising->setScanResponse(true);         // Enable scan response
  BLEDevice::startAdvertising();

  Serial.println("BLE server is running and advertising...");
}

void loop() {
}
