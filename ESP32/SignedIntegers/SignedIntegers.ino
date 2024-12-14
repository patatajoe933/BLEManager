#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "00000020-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000020-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT8_UUID "10000021-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT16_UUID "10000022-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT32_UUID "10000023-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT64_UUID "10000024-74ee-43ce-86b2-0dde20dcefd6"
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

class IntegerCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    const uint8_t *data = pCharacteristic->getData();
    size_t dataLength = pCharacteristic->getLength();

    Serial.print("Received value: ");

    if (dataLength > 0) {
      int64_t intValue = 0;

      switch (dataLength) {
        case 1:  // 8-bit integer
          intValue = static_cast<int8_t>(data[0]);
          break;
        case 2:  // 16-bit integer
          intValue = static_cast<int16_t>(data[0] | (data[1] << 8));
          break;
        case 4:  // 32-bit integer
          intValue = static_cast<int32_t>(data[0] | (data[1] << 8) | (data[2] << 16) | (data[3] << 24));
          break;
        case 8:  // 64-bit integer
          intValue = static_cast<int64_t>(data[0] | (uint64_t(data[1]) << 8) | (uint64_t(data[2]) << 16) | (uint64_t(data[3]) << 24) | (uint64_t(data[4]) << 32) | (uint64_t(data[5]) << 40) | (uint64_t(data[6]) << 48) | (uint64_t(data[7]) << 56));
          break;
        default:
          Serial.println("Invalid data length!");
          return;
      }

      Serial.println(intValue);
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
  pCharacteristicServiceName->setValue("Signed Integers");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

  // sint8: editable control for signed byte characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicSInt8 = pService->createCharacteristic(
    CHARACTERISTIC_SINT8_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt8->setCallbacks(new IntegerCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt8Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt8Descriptor->setValue(
    R"({"type":"sint8", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-70, "maxInt":70})");

  pCharacteristicSInt8->addDescriptor(sInt8Descriptor);
  uint8_t sint8 = -69;
  pCharacteristicSInt8->setValue(&sint8, sizeof(uint8_t));

  // sint16: Editable control for signed int16 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicSInt16 = pService->createCharacteristic(
    CHARACTERISTIC_SINT16_UUID,  // Correct UUID for sint16
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt16->setCallbacks(new IntegerCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt16Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt16Descriptor->setValue(
    R"({"type":"sint16", "order":2, "disabled":false, "label":"Signed Int16", "minInt":-7070, "maxInt":7070})");
  pCharacteristicSInt16->addDescriptor(sInt16Descriptor);
  int16_t sint16 = -6969;
  pCharacteristicSInt16->setValue((uint8_t *)&sint16, sizeof(int16_t));

  // sint32: Editable control for signed int32 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicSInt32 = pService->createCharacteristic(
    CHARACTERISTIC_SINT32_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt32->setCallbacks(new IntegerCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt32Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt32Descriptor->setValue(
    R"({"type":"sint32", "order":3, "disabled":false, "label":"Signed Int32", "minInt":-707070, "maxInt":707070})");
  pCharacteristicSInt32->addDescriptor(sInt32Descriptor);
  int32_t sint32 = -696969;
  pCharacteristicSInt32->setValue((uint8_t *)&sint32, sizeof(int32_t));

  // sint64: Editable control for signed int64 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicSInt64 = pService->createCharacteristic(
    CHARACTERISTIC_SINT64_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt64->setCallbacks(new IntegerCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt64Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt64Descriptor->setValue(
    R"({"type":"sint64", "order":4, "disabled":false, "label":"Signed Int64", "minInt":-7070707070, "maxInt":7070707070})");
  pCharacteristicSInt64->addDescriptor(sInt64Descriptor);
  int64_t sint64 = -6969696969;
  pCharacteristicSInt64->setValue((uint8_t *)&sint64, sizeof(int64_t));

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
