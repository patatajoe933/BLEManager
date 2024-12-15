#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "00000090-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000090-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TIME32_UUID       "10000091-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_DATE32_UUID       "10000092-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_DATE64_UUID       "10000093-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_DATETIME32_UUID   "10000094-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_DATETIME64_UUID   "10000095-74ee-43ce-86b2-0dde20dcefd6"
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

class DateTimeCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    const uint8_t *data = pCharacteristic->getData();
    size_t dataLength = pCharacteristic->getLength();

    const size_t timeSize = sizeof(time_t);
    time_t receivedTime = 0;

    for (size_t i = 0; i < timeSize; i++) {
      if (i < dataLength) {
        receivedTime |= ((uint64_t)(data[i] & 0xFF) << (8 * i));
      }
    }

    char buffer[64];
    struct tm *timeinfo = localtime(&receivedTime);
    if (timeinfo) {
      strftime(buffer, sizeof(buffer), "%Y-%m-%d %H:%M:%S", timeinfo);
      Serial.print("Received time: ");
      Serial.println(buffer);
    } else {
      Serial.println("Invalid time value (out of range)!");
    }
  }
};

void setup() {
  Serial.begin(115200);

  struct tm dtm = {};
  dtm.tm_year = 2025 - 1900;
  dtm.tm_mon = 0;
  dtm.tm_mday = 1;
  dtm.tm_hour = 11;
  dtm.tm_min = 59;
  dtm.tm_sec = 0;

  struct tm dt = {};
  dt.tm_year = 2025 - 1900;
  dt.tm_mon = 0;
  dt.tm_mday = 1;
  dt.tm_hour = 0;
  dt.tm_min = 0;
  dt.tm_sec = 0;

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
  pCharacteristicServiceName->setValue("Date And Time");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device (if paired)
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.
  // Alternatively you can try Clear GATT cahce from device menu un MingleApp

  // time: editable control for time (uint16_t) characteristics
  // The value represents the number of seconds since midnight.
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicTime = pService->createCharacteristic(
    CHARACTERISTIC_TIME32_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicTime->setCallbacks(new DateTimeCharacteristicCallbacks());
  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *timeDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  timeDescriptor->setValue(
    R"({"type":"time", "order":1, "disabled":false, "label":"Time"})");

  pCharacteristicTime->addDescriptor(timeDescriptor);
  uint32_t time = 6 * 60 * 60 + 9 * 60;
  pCharacteristicTime->setValue(time);

  // date32: Editable control for date (uint32_t) characteristics
  // The value represents the number of seconds since the epoch.
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicDate32 = pService->createCharacteristic(
    CHARACTERISTIC_DATE32_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicDate32->setCallbacks(new DateTimeCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *date32Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  date32Descriptor->setValue(
    R"({"type":"date32", "order":2, "disabled":false, "label":"Date 32"})");
  pCharacteristicDate32->addDescriptor(date32Descriptor);
  uint32_t date32 = mktime(&dt);
  pCharacteristicDate32->setValue(date32);

  // date64: Editable control for date (uint64_t) characteristics
  // The value represents the number of seconds since the epoch.
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicDate64 = pService->createCharacteristic(
    CHARACTERISTIC_DATE64_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicDate64->setCallbacks(new DateTimeCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *date64Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  date64Descriptor->setValue(
    R"({"type":"date64", "order":3, "disabled":false, "label":"Date 64"})");
  pCharacteristicDate64->addDescriptor(date64Descriptor);
  uint64_t date64 = mktime(&dt);
  pCharacteristicDate64->setValue((uint8_t *)&date64, sizeof(uint64_t));

  // datetime32: Editable control for datetime (uint32_t) characteristics
  // The value represents the number of seconds since the epoch.
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicDateTime32 = pService->createCharacteristic(
    CHARACTERISTIC_DATETIME32_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicDateTime32->setCallbacks(new DateTimeCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *dateTime32Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  dateTime32Descriptor->setValue(
    R"({"type":"datetime32", "order":4, "disabled":false, "label":"DateTime 32"})");
  pCharacteristicDateTime32->addDescriptor(dateTime32Descriptor);
  uint32_t dateTime32 = mktime(&dtm);
  pCharacteristicDateTime32->setValue(dateTime32);

  // datetime64: Editable control for datetime (uint64_t) characteristics
  // The value represents the number of seconds since the epoch.
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicDateTime64 = pService->createCharacteristic(
    CHARACTERISTIC_DATETIME64_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristicDateTime64->setCallbacks(new DateTimeCharacteristicCallbacks());
   //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *dateTime64Descriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  dateTime64Descriptor->setValue(
    R"({"type":"datetime64", "order":5, "disabled":false, "label":"DateTime 64"})");
  pCharacteristicDateTime64->addDescriptor(dateTime64Descriptor);
  uint64_t dateTime64 = mktime(&dtm);
  //1735732740;
  pCharacteristicDateTime64->setValue((uint8_t *)&dateTime64, sizeof(uint64_t));

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
