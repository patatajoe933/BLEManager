#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID        "00000001-74ee-43ce-86b2-0dde20dcefd6"
// UUID for the BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID "10000000-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TITLE_UUID "10000001-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TEXTVIEW_UUID "10000002-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_STRING_UUID "10000003-74ee-43ce-86b2-0dde20dcefd6"
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

    // Create a BLE service with a predefined UUID
    BLEService *pService = pServer->createService(SERVICE_UUID);
    //IF YOU ADD OR REMOVE CHARACTERISTIC FORGOT DEVICE IN BLUEETOTH AND BONDING AGAIN IS SOMETIMES NEEDED IN ANDROID TO TAKE EFFECT.
    // Create a BLE characteristic
    BLECharacteristic *pCharacteristicServiceName = pService->createCharacteristic(
                                         CHARACTERISTIC_SERVICE_NAME_UUID,
                                         BLECharacteristic::PROPERTY_READ
                                       );

    pCharacteristicServiceName->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);

    // Add a custom descriptor used by the Minglee app
    // Only one descriptor matching the mask is supported for each characteristic.
    // If a characteristic contains multiple descriptors matching the mask, a descriptor may be selected randomly.
    BLEDescriptor *serviceNameDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
    //Set control configuration
    //JSON format. Keys are case-sensitive.
    serviceNameDescriptor->setValue(
      // The value of this characteristic will be displayed as the service name.
      // The order value determines the order in which the service will be displayed in the Minglee app.
      // Only one serviceName characteristic is supported for each service.
      // If a service contains multiple serviceName characteristic, a serviceName characteristic may be selected randomly.
      R"({"type":"serviceName", "order":1})"
    );
  
    pCharacteristicServiceName->addDescriptor(serviceNameDescriptor);

    // Set an initial value for the characteristic
    pCharacteristicServiceName->setValue("Service 1");

    ////CONTROLS///
    //Title is readonly large text
    BLECharacteristic *pCharacteristicTitle = pService->createCharacteristic(
                                         CHARACTERISTIC_TITLE_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_NOTIFY
                                       );
    pCharacteristicTitle->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);
    BLEDescriptor *titleDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
    titleDescriptor->setValue(
      R"({"type":"title", "order":1, disabled:false})" //Control is always readonly. Disabled has only visual effect
    );
    pCharacteristicTitle->addDescriptor(titleDescriptor);
    pCharacteristicTitle->setValue("This is large text");

    //TextView is readonly regular size Text
    BLECharacteristic *pCharacteristicTextView = pService->createCharacteristic(
                                         CHARACTERISTIC_TEXTVIEW_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_NOTIFY
                                       );
    pCharacteristicTextView->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);
    BLEDescriptor *textViewDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
    textViewDescriptor->setValue(
      R"({"type":"textView", "order":2, disabled:false})" //Control is always readonly. Disabled has only visual effect
    );
    pCharacteristicTextView->addDescriptor(textViewDescriptor);
    pCharacteristicTextView->setValue("This is readonly text");

    //String is editable control for string characteristic
    //UTF-8 encoding is supported
    //Characteristic is not writeable. Setting of dissable property doesn not take effect, controll will be always disabled.
    BLECharacteristic *pCharacteristicString = pService->createCharacteristic(
                                         CHARACTERISTIC_STRING_UUID,
                                         //READONLY characteristic
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_NOTIFY
                                       );
    pCharacteristicString->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED);
    BLEDescriptor *stringDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
    stringDescriptor->setValue(
      R"({"type":"string", "order":2})" //R"({"type":"textView", "order":2, disabled:false})" Disabled setting has no effect in this example, can be skipped.
    );
    pCharacteristicString->addDescriptor(stringDescriptor);
    pCharacteristicString->setValue("This is string");

    // Start the BLE service
    pService->start();

    // Start BLE advertising
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID); // Advertise the service UUID
    pAdvertising->setScanResponse(true);       // Enable scan response
    BLEDevice::startAdvertising();

    Serial.println("BLE server is running and advertising...");
}

void loop() {
    // Empty loop since BLE server runs in the background
}
