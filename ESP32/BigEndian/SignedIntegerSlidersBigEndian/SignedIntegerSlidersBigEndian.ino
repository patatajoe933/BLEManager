#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

/*
ESP32 transmits data in Little-Endian byte order. To emulate Big-Endian byte order, we must reverse the byte order.
*/

#define BYTESWAP16(x) static_cast<int16_t>(((x & 0xFF) << 8) | ((x >> 8) & 0xFF))
#define BYTESWAP32(x) static_cast<int32_t>(((x & 0xFF) << 24) | ((x >> 8) & 0xFF) << 16 | ((x >> 16) & 0xFF) << 8 | ((x >> 24) & 0xFF))
#define BYTESWAP64(x) static_cast<int64_t>(((x & 0xFF) << 56) | ((x >> 8) & 0xFF) << 48 | ((x >> 16) & 0xFF) << 40 | ((x >> 24) & 0xFF) << 32 | ((x >> 32) & 0xFF) << 24 | ((x >> 40) & 0xFF) << 16 | ((x >> 48) & 0xFF) << 8 | ((x >> 56) & 0xFF))

// UUID for the BLE service
#define SERVICE_UUID "00000100-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000100-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT8_UUID "10000101-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SINT16_UUID "10000102-74ee-43ce-86b2-0dde20dcefd6"
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

//Big endian
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
          intValue = static_cast<int16_t>((data[0] << 8) | data[1]);
          break;
        case 4:  // 32-bit integer
          intValue = static_cast<int32_t>((data[0] << 24) | (data[1] << 16) | (data[2] << 8) | data[3]);
          break;
        case 8:  // 64-bit integer
          intValue = static_cast<int64_t>(
            (uint64_t(data[0]) << 56) | (uint64_t(data[1]) << 48) | (uint64_t(data[2]) << 40) | (uint64_t(data[3]) << 32) | (uint64_t(data[4]) << 24) | (uint64_t(data[5]) << 16) | (uint64_t(data[6]) << 8) | uint64_t(data[7]));
          break;
        default:
          Serial.println("Invalid data length!");
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
  pCharacteristicServiceName->setValue("Big Endian Signed Integer Sliders");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

  // sint8sliderbe: editable slider control for signed byte characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  /*While a big-endian variant for a single byte might seem unnecessary,
  it's crucial because the Integer components read an arbitrarily long value and then truncate it to the specified byte size.
  This ensures consistent behavior regardless of the underlying data representation.*/
  BLECharacteristic *pCharacteristicSInt8 = pService->createCharacteristic(
    CHARACTERISTIC_SINT8_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt8->setCallbacks(new IntegerCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt8Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt8Descriptor->setValue(
    R"({"type":"sint8sliderbe", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-70, "maxInt":70, "stepInt":1})"); //Defaults: minInt = 0, maxInt = 100, stepInt: 1

  pCharacteristicSInt8->addDescriptor(sInt8Descriptor);
  uint8_t sint8 = 20;
  pCharacteristicSInt8->setValue(&sint8, sizeof(uint8_t));

  // sint16sliderbe: Editable slider control for signed int16 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicSInt16 = pService->createCharacteristic(
    CHARACTERISTIC_SINT16_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicSInt16->setCallbacks(new IntegerCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *sInt16Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  sInt16Descriptor->setValue(
    R"({"type":"sint16sliderbe", "order":2, "disabled":false, "label":"Signed Int16", "minInt":-20, "maxInt":100, "stepInt":2})"); //Defaults: minInt = 0, maxInt = 100, stepInt: 1
  pCharacteristicSInt16->addDescriptor(sInt16Descriptor);
  int16_t sint16 = 50;
  sint16 = BYTESWAP16(sint16);
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
