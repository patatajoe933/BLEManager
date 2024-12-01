#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE services
#define SERVICE1_UUID        "00000001-74ee-43ce-86b2-0dde20dcefd6"
#define SERVICE2_UUID        "00000002-74ee-43ce-86b2-0dde20dcefd6"
// UUID for the BLE characteristic
#define CHARACTERISTIC_UUID "10000001-74ee-43ce-86b2-0dde20dcefd6"
//#define CHARACTERISTIC2_UUID "10000002-74ee-43ce-86b2-0dde20dcefd6"
// Default UUID mask for the Minglee app is ####face-####-####-####-############
// The segment "face" (case-insensitive) is used by Minglee to identify descriptors
#define CUSTOM_DESCRIPTOR_UUID  "2000face-74ee-43ce-86b2-0dde20dcefd6"

// Custom server callback class to handle connection events
class ServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        Serial.println("Device connected.");
    }

    void onDisconnect(BLEServer* pServer) {
        Serial.println("Device disconnected. Restarting advertising...");
        // Restart advertising when a device disconnects
        BLEDevice::startAdvertising();
    }
};

void setup() {
    Serial.begin(115200);

    // Initialize BLE device with a name
    BLEDevice::init("Minglee device");

    // Configure BLE security settings
    // Static PIN bonding
    BLESecurity *pSecurity = new BLESecurity();
    // It is important to call setStaticPIN() before setAuthenticationMode() for bonding to work correctly
    // https://github.com/espressif/arduino-esp32/blob/98da424de638836e400d4a110b9cb9a101e8cc22/libraries/BLE/src/BLESecurity.cpp#L65
    pSecurity->setStaticPIN(123456);
    pSecurity->setAuthenticationMode(ESP_LE_AUTH_BOND);
   
    // Create a BLE server and set its callback class
    BLEServer *pServer = BLEDevice::createServer();
    pServer->setCallbacks(new ServerCallbacks());

    //SERVICE 1
    // Create a BLE service with a predefined UUID
    BLEService *pService1 = pServer->createService(SERVICE1_UUID);

    // Create a BLE characteristic with read and notify properties
    BLECharacteristic *pCharacteristic1 = pService1->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_NOTIFY
                                       );

    pCharacteristic1->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);

    // Add a custom descriptor used by the Minglee app
    // Only one descriptor matching the mask is supported for each characteristic.
    // If a characteristic contains multiple descriptors matching the mask, a descriptor may be selected randomly.
    BLEDescriptor *customDescriptor1 = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
    //Set control configuration
    //JSON format. Keys are case-sensitive.
    customDescriptor1->setValue(
      // The value of this characteristic will be displayed as the service name.
      // The order value determines the order in which the service will be displayed in the Minglee app.
      // Only one serviceName characteristic is supported for each service.
      // If a service contains multiple serviceName characteristic, a serviceName characteristic may be selected randomly.
      R"({"type":"serviceName", "order":1})"
    );
  
    pCharacteristic1->addDescriptor(customDescriptor1);

    // Set an initial value for the characteristic
    pCharacteristic1->setValue("Service 1");

    ///SERVICE 2
    BLEService *pService2 = pServer->createService(SERVICE2_UUID);
    BLECharacteristic *pCharacteristic2 = pService2->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_NOTIFY
                                       );

    pCharacteristic2->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);
    BLEDescriptor *customDescriptor2 = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
    customDescriptor2->setValue(
      R"({"type":"serviceName", "order":2})"
    );
  
    pCharacteristic2->addDescriptor(customDescriptor2);
    pCharacteristic2->setValue("Service 2");

    // Start the BLE services
    pService1->start();
    pService2->start();

    // Start BLE advertising
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE1_UUID); // Advertise the service UUID
    pAdvertising->addServiceUUID(SERVICE2_UUID); // Advertise the service UUID
    pAdvertising->setScanResponse(true);       // Enable scan response
    
    BLEDevice::startAdvertising();

    Serial.println("BLE server is running and advertising...");
}

void loop() {
    // Empty loop since BLE server runs in the background
}
