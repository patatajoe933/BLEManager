#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID "00000001-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000000-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TITLE_UUID "10000001-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TEXTVIEW_UUID "10000002-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TEXTFIELD_UUID "10000003-74ee-43ce-86b2-0dde20dcefd6"
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

class CharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    String value = pCharacteristic->getValue();

    if (!value.isEmpty()) {
      Serial.print("Received value: ");
      Serial.println(value.c_str());
    }
  }
};

BLECharacteristic *testCharacteristic = NULL;
void setup() {
  Serial.begin(115200);

  // Initialize BLE device with a name
  BLEDevice::init("Minglee device");

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
  BLEService *pService = pServer->createService(BLEUUID(SERVICE_UUID), 32);  //32 handlers
  // If you add or remove characteristics, it may be necessary to forget the device
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.

  // Create a BLE characteristic for service name
  // The value of this characteristic will be displayed as the service name.
  // The "order" value determines the order in which the service appears in the Minglee app.
  // Only one "serviceName" characteristic is supported per service.
  // If a service contains multiple "serviceName" characteristics, one may be selected randomly.

  BLECharacteristic *pCharacteristicServiceName = pService->createCharacteristic(
    CHARACTERISTIC_SERVICE_NAME_UUID,
    BLECharacteristic::PROPERTY_READ);
  pCharacteristicServiceName->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);

  // Add a custom descriptor used by the Minglee app
  // Only one descriptor matching the mask is supported per characteristic.
  // If multiple descriptors match, one may be selected randomly.
  BLEDescriptor *serviceNameDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
  // Set control configuration
  // JSON format. Keys are case-sensitive.
  serviceNameDescriptor->setValue(
    R"({"type":"serviceName", "order":1})");

  pCharacteristicServiceName->addDescriptor(serviceNameDescriptor);

  // Set an initial value for the characteristic
  pCharacteristicServiceName->setValue("Service 1");

  //// CONTROLS ////
  // If you add or remove characteristics, it may be necessary to forget the device
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.

  // Title: read-only large text
  BLECharacteristic *pCharacteristicTitle = pService->createCharacteristic(
    CHARACTERISTIC_TITLE_UUID,
    BLECharacteristic::PROPERTY_READ
      | BLECharacteristic::PROPERTY_INDICATE);
  pCharacteristicTitle->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);
  BLEDescriptor *titleDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
  titleDescriptor->setValue(
    R"({"type":"title", "order":1, "disabled":false})"  // Control is always read-only. "Disabled" has only a visual effect.
  );

  pCharacteristicTitle->addDescriptor(titleDescriptor);
  pCharacteristicTitle->setValue("This is large text");

  // TextView: read-only regular-sized text
  BLECharacteristic *pCharacteristicTextView = pService->createCharacteristic(
    CHARACTERISTIC_TEXTVIEW_UUID,
    BLECharacteristic::PROPERTY_READ
    //| BLECharacteristic::PROPERTY_INDICATE
  );
  pCharacteristicTextView->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);
  BLEDescriptor *textViewDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
  textViewDescriptor->setValue(
    R"({"type":"textView", "order":2, "disabled":false})"  // Control is always read-only. "Disabled" has only a visual effect.
  );

  pCharacteristicTextView->addDescriptor(textViewDescriptor);
  pCharacteristicTextView->setValue("This is read-only text");

  // TextField: editable control for string characteristics
  // Supports UTF-8 encoding
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicTextField = pService->createCharacteristic(
    CHARACTERISTIC_TEXTFIELD_UUID,
    // Read-only characteristic
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE  //otestovat zápis bez oprávnění
    //| BLECharacteristic::PROPERTY_INDICATE
  );
  pCharacteristicTextField->setCallbacks(new CharacteristicCallbacks());
  pCharacteristicTextField->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED | ESP_GATT_PERM_WRITE_ENCRYPTED);
  BLEDescriptor *textFieldDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
  textFieldDescriptor->setValue(
    R"({"type":"textField", "order":3, "disabled":false})");

  pCharacteristicTextField->addDescriptor(textFieldDescriptor);
  pCharacteristicTextField->setValue("This is a TextField");

  ////NOTIFY TEST////
  testCharacteristic = pCharacteristicTitle;
  testCharacteristic->addDescriptor(new BLE2902());
  /////
  // Start the BLE service
  pService->start();

  // Start BLE advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);  // Advertise the service UUID
  pAdvertising->setScanResponse(true);         // Enable scan response
  BLEDevice::startAdvertising();

  Serial.println("BLE server is running and advertising...");
}

int counter = 0;
void loop() {
  // Empty loop since BLE server runs in the background
  //NOTIFY TEST
  delay(5000);
  testCharacteristic->setValue(String(counter++));
  testCharacteristic->indicate();
  ///////////
}
