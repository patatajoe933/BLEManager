/*
IT IS STRONGLY RECOMMENDED NOT TO USE BONDING DURING DEVELOPMENT.
ANDROID CACHES BONDED DEVICES, AND IF YOU CHANGE THE STRUCTURE OF SERVICES, CHARACTERISTICS, OR DESCRIPTORS, IT CAN LEAD TO UNPREDICTABLE BEHAVIOR.
YOU CAN THEN TRY CLEARING THE GATT CACHE IN THE APP'S DEVICE MENU OR UNPAIRING THE DEVICE IN THE BLUETOOTH SETTINGS.
*/

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID                       "01000010-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID   "11000010-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TEXT_UUID           "11000011-74ee-43ce-86b2-0dde20dcefd6"

// Default UUID mask for the BLE Manager app is ####face-####-####-####-############
// The segment "face" (case-insensitive) is used by BLE Manager to identify descriptors
#define CUSTOM_DESCRIPTOR_UUID            "2000face-74ee-43ce-86b2-0dde20dcefd6"

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
    String value = pCharacteristic->getValue();
    Serial.print("Received value: ");
    if (!value.isEmpty()) {
      Serial.println(value.c_str());
    }
  }
};

void setup() {
  Serial.begin(115200);

  // Initialize BLE device with a name
  BLEDevice::init("Secured Device");

  // Configure BLE security settings
  // Static PIN bonding
  BLESecurity *pSecurity = new BLESecurity();
  // It's important to call setStaticPIN() before setAuthenticationMode() for bonding to work correctly
  // Reference: https://github.com/espressif/arduino-esp32/blob/98da424de638836e400d4a110b9cb9a101e8cc22/libraries/BLE/src/BLESecurity.cpp#L65
  pSecurity->setStaticPIN(123456);
  pSecurity->setAuthenticationMode(ESP_LE_AUTH_BOND);

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
  
  pCharacteristicServiceName->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);

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
  BLECharacteristic *pCharacteristicText = pService->createCharacteristic(
    CHARACTERISTIC_TEXT_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
  );

  pCharacteristicText->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED | ESP_GATT_PERM_WRITE_ENCRYPTED);

  pCharacteristicText->setCallbacks(new StringCharacteristicCallbacks());

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
}
