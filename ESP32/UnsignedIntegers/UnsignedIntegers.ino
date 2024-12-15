#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "00000030-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000030-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_UINT8_UUID        "10000031-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_UINT16_UUID       "10000032-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_UINT32_UUID       "10000033-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_UINT64_UUID       "10000034-74ee-43ce-86b2-0dde20dcefd6"
// Default UUID mask for the Minglee app is ####face-####-####-####-############
// The segment "face" (case-insensitive) is used by Minglee to identify descriptors
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

class UnsignedIntegerCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    const uint8_t *data = pCharacteristic->getData();
    size_t dataLength = pCharacteristic->getLength();

    Serial.print("Received value: ");

    if (dataLength > 0) {
      switch (dataLength) {
        case sizeof(uint8_t):  // 8-bit unsigned integer
          {
            uint8_t intValue;
            memcpy(&intValue, data, sizeof(uint8_t));
            Serial.println(intValue);
          }
          break;
        case sizeof(uint16_t):  // 16-bit unsigned integer
          {
            uint16_t intValue;
            memcpy(&intValue, data, sizeof(uint16_t));
            Serial.println(intValue);
          }
          break;
        case sizeof(uint32_t):  // 32-bit unsigned integer
          {
            uint32_t intValue;
            memcpy(&intValue, data, sizeof(uint32_t));
            Serial.println(intValue);
          }
          break;
        case sizeof(uint64_t):  // 64-bit unsigned integer
          {
            uint64_t intValue;
            memcpy(&intValue, data, sizeof(uint64_t));
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
  BLEDevice::init("Minglee device");

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
  // The "order" value determines the order in which the service appears in the Minglee app.
  // Only one "serviceName" characteristic is supported per service.
  // If a service contains multiple "serviceName" characteristics, one may be selected randomly.

  BLECharacteristic *pCharacteristicServiceName = pService->createCharacteristic(
    CHARACTERISTIC_SERVICE_NAME_UUID,
    BLECharacteristic::PROPERTY_READ);

  // Add a custom descriptor used by the Minglee app
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
  pCharacteristicServiceName->setValue("Unsigned Integers");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

  // uint8: editable control for unsigned byte characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicUInt8 = pService->createCharacteristic(
    CHARACTERISTIC_UINT8_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicUInt8->setCallbacks(new UnsignedIntegerCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *uInt8Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  uInt8Descriptor->setValue(
    R"({"type":"uint8", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":60, "maxInt":70})");

  pCharacteristicUInt8->addDescriptor(uInt8Descriptor);
  uint8_t uint8 = 69;
  pCharacteristicUInt8->setValue(&uint8, sizeof(uint8_t));

  // uint16: Editable control for unsigned int16 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicUInt16 = pService->createCharacteristic(
    CHARACTERISTIC_UINT16_UUID,  // Correct UUID for uint16
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicUInt16->setCallbacks(new UnsignedIntegerCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *uInt16Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  uInt16Descriptor->setValue(
    R"({"type":"uint16", "order":2, "disabled":false, "label":"Unsigned Int16", "minInt":6060, "maxInt":7070})");
  pCharacteristicUInt16->addDescriptor(uInt16Descriptor);
  uint16_t uint16 = 6969;
  pCharacteristicUInt16->setValue((uint8_t *)&uint16, sizeof(uint16_t));

  // uint32: Editable control for unsigned int32 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicUInt32 = pService->createCharacteristic(
    CHARACTERISTIC_UINT32_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicUInt32->setCallbacks(new UnsignedIntegerCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *uInt32Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  uInt32Descriptor->setValue(
    R"({"type":"uint32", "order":3, "disabled":false, "label":"Unsigned Int32", "minInt":606060, "maxInt":707070})");
  pCharacteristicUInt32->addDescriptor(uInt32Descriptor);
  uint32_t uint32 = 696969;
  pCharacteristicUInt32->setValue((uint8_t *)&uint32, sizeof(uint32_t));

  // uint64: Editable control for unsigned int64 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicUInt64 = pService->createCharacteristic(
    CHARACTERISTIC_UINT64_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicUInt64->setCallbacks(new UnsignedIntegerCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *uInt64Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  uInt64Descriptor->setValue(
    R"({"type":"uint64", "order":4, "disabled":false, "label":"Unsigned Int64", "minInt":6060606060, "maxInt":7070707070})");
  pCharacteristicUInt64->addDescriptor(uInt64Descriptor);
  uint64_t uint64 = 6969696969;
  pCharacteristicUInt64->setValue((uint8_t *)&uint64, sizeof(uint64_t));

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
