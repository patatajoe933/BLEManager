#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "00000110-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000110-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_UINT32_UUID "10000113-74ee-43ce-86b2-0dde20dcefd6"
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

class ColorCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    const uint8_t *data = pCharacteristic->getData();
    size_t dataLength = pCharacteristic->getLength();

    Serial.print("Received value: ");

    if (dataLength > 0) {
      if (dataLength == sizeof(uint32_t)) {
        uint32_t intValue;
        memcpy(&intValue, data, sizeof(uint32_t));

        uint8_t r = intValue & 0xFF;
        uint8_t g = (intValue >> 8) & 0xFF;
        uint8_t b = (intValue >> 16) & 0xFF;
        uint8_t a = (intValue >> 24) & 0xFF;

        Serial.print("R: ");
        Serial.print(r);
        Serial.print(", G: ");
        Serial.print(g);
        Serial.print(", B: ");
        Serial.print(b);
        Serial.print(", A: ");
        Serial.println(a);
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
  pCharacteristicServiceName->setValue("Color");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

  // Color: An editable control for unsigned 32-bit integer color characteristics. The red component (R) is stored in the first byte.
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicUInt32 = pService->createCharacteristic(
    CHARACTERISTIC_UINT32_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicUInt32->setCallbacks(new ColorCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *uInt32Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  uInt32Descriptor->setValue(
    R"({"type":"color", "order":1, "disabled":false, "label":"Color", "alphaSlider":true})");
  pCharacteristicUInt32->addDescriptor(uInt32Descriptor);
  uint32_t uint32 = 0xFF05E6F2; //R:0xF2, G:0xE6, B:0x05, A:0xFF
  pCharacteristicUInt32->setValue((uint8_t *)&uint32, sizeof(uint32_t));

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
