#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define SERVICE_UUID        "00000001-74ee-43ce-86b2-0dde20dcefd6b"
#define CHARACTERISTIC_UUID "10000001-74ee-43ce-86b2-0dde20dcefd6"
//Default UUID mask for Minglee app is ####face-####-####-####-############
//Only face is FACE is significant value for descriptor edentification by Minglee
#define CUSTOM_DESCRIPTOR_UUID  "2000face-74ee-43ce-86b2-0dde20dcefd6"

class ServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        Serial.println("Device connected.");
    }

    void onDisconnect(BLEServer* pServer) {
        Serial.println("Device disconnected. Restarting advertising...");
        // Restart advertising after disconnect
        BLEDevice::startAdvertising();
    }
};

void setup() {
    Serial.begin(115200);

    //BLE Initialization
    BLEDevice::init("Minglee device"); //Device name

    BLESecurity *pSecurity = new BLESecurity();
    //Set ESP_LE_AUTH_REQ_SC_MITM_BOND for bonding
    pSecurity->setAuthenticationMode(ESP_LE_AUTH_NO_BOND);
    
    //Uncomment for PIN bonding
    //pSecurity->setCapability(ESP_IO_CAP_IO);
    //pSecurity->setInitEncryptionKey(ESP_BLE_ENC_KEY_MASK | ESP_BLE_ID_KEY_MASK);
    //pSecurity->setStaticPIN(123456);  

    //BLE server
    BLEServer *pServer = BLEDevice::createServer();
    pServer->setCallbacks(new ServerCallbacks());

    //BLE service
    BLEService *pService = pServer->createService(SERVICE_UUID);

    //BLE characteristic
    BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE |
                                         BLECharacteristic::PROPERTY_NOTIFY
                                       );
    
    //Uncomment for characteristic encryption
    //pCharacteristic->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED | ESP_GATT_PERM_WRITE_ENCRYPTED);

    //Custom descriptor form Minglee app
    BLEDescriptor *customDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID);
    customDescriptor->setValue("My descriptor"); // Nastavení hodnoty, např. jednotky měření
    pCharacteristic->addDescriptor(customDescriptor);

    //Charakteristic value
    pCharacteristic->setValue("Hello, BLE!");

    //Start service
    pService->start();

    //Start adveritising
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    BLEDevice::startAdvertising();

    Serial.println("BLE server is running and advertising...");
}

void loop() {
}
