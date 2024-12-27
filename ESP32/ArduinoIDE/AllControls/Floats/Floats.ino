#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "00000060-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000060-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_HALF_UUID "10000061-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_FLOAT_UUID "10000062-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_DOUBLE_UUID "10000063-74ee-43ce-86b2-0dde20dcefd6"
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

class FloatCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    const uint8_t *data = pCharacteristic->getData();
    size_t dataLength = pCharacteristic->getLength();

    Serial.print("Received value: ");

    if (dataLength > 0) {
      if (dataLength == 2) {  // 16-bit halfFloat
        uint16_t halfValue = data[0] | (data[1] << 8);
        float floatValue = halfToFloat(halfValue);  //halfFloat -> float
        Serial.println(floatValue);
      } else if (dataLength == 4) {  // 32-bit Float
        float floatValue;
        memcpy(&floatValue, data, sizeof(float));
        Serial.println(floatValue);
      } else if (dataLength == 8) {  // 64-bit Double
        double doubleValue;
        memcpy(&doubleValue, data, sizeof(double));
        Serial.println(doubleValue);
      } else {
        Serial.println("Invalid data length for floating-point value!");
      }
    } else {
      Serial.println("Empty value received!");
    }
  }

  float halfToFloat(uint16_t half) {
    uint16_t sign = (half & 0x8000) >> 15;
    uint16_t exponent = (half & 0x7C00) >> 10;
    uint16_t mantissa = half & 0x03FF;

    if (exponent == 0) {  // Subnormal number or zero
      if (mantissa == 0) {
        return sign ? -0.0f : 0.0f;
      } else {
        return (sign ? -1.0f : 1.0f) * (mantissa / 1024.0f) * pow(2, -14);
      }
    } else if (exponent == 0x1F) {  // Infinity or NaN
      return mantissa ? NAN : (sign ? -INFINITY : INFINITY);
    } else {  // Normal number
      return (sign ? -1.0f : 1.0f) * (1.0f + (mantissa / 1024.0f)) * pow(2, exponent - 15);
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
  BLEService *pService = pServer->createService(BLEUUID(SERVICE_UUID), 15);  //15 Handles
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
  pCharacteristicServiceName->setValue("Floats");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

  // Half: editable control for float16 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicHalf = pService->createCharacteristic(
    CHARACTERISTIC_HALF_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicHalf->setCallbacks(new FloatCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *halfDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  halfDescriptor->setValue(
    R"({"type":"half", "order":1, "disabled":false, "label":"Float 16", "minFloat": -10, "maxFloat": 10})");

  pCharacteristicHalf->addDescriptor(halfDescriptor);
  uint16_t h = 0x46E6;
  pCharacteristicHalf->setValue((uint8_t *)&h, sizeof(uint16_t));

  // Float: editable control for float32 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicFloat = pService->createCharacteristic(
    CHARACTERISTIC_FLOAT_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristicFloat->setCallbacks(new FloatCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *floatDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  floatDescriptor->setValue(
    R"({"type":"float", "order":2, "disabled":false, label:"Float 32", "minFloat": -20, "maxFloat": 20})");

  pCharacteristicFloat->addDescriptor(floatDescriptor);
  
  float f = 6.9;
  pCharacteristicFloat->setValue(f);

  // Double: editable control for float64 characteristics
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicDouble = pService->createCharacteristic(
    CHARACTERISTIC_DOUBLE_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

  pCharacteristicDouble->setCallbacks(new FloatCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *doubleDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  doubleDescriptor->setValue(
    R"({"type":"double", "order":3, "disabled":false, label:"Float 64", "minFloat": -30, "maxFloat": 30})");

  pCharacteristicDouble->addDescriptor(doubleDescriptor);
  
  double d = 6.9;
  pCharacteristicDouble->setValue(d);

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
