/*
You can control the visibility of services by using different descriptor masks.
In an application intended for limited users of your device, you would set a different descriptor mask than in an application for advanced users.
All services and characteristics would have a descriptor for advanced users, while selected properties for limited users would be assigned a limited descriptor.

In advanced scenarios, this approach can be used not only to control visibility but also to differentiate all properties described by the descriptor.
In this example, the advanced user has access to two services, while the limited user has access only to the first service and can send a maximum of 20 bytes.
*/

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUID for the BLE service
#define SERVICE_UUID                       "01000040-74ee-43ce-86b2-0dde20dcefd6"
#define SERVICE2_UUID                      "01000041-74ee-43ce-86b2-0dde20dcefd6"
// UUIDs for BLE characteristics
#define CHARACTERISTIC_SERVICE_NAME_UUID   "11000040-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_TEXT_UUID           "11000041-74ee-43ce-86b2-0dde20dcefd6"
#define CHARACTERISTIC_SWITCH_UUID         "11000042-74ee-43ce-86b2-0dde20dcefd6"

// Default UUID mask for the BLE Manager app is ####face-####-####-####-############
// The segment "face" (case-insensitive) is used by BLE Manager to identify descriptors
#define CUSTOM_DESCRIPTOR_UUID            "2000face-74ee-43ce-86b2-0dde20dcefd6"

//Set mask ####fBce-####-####-####-############ in device settings for limited access.
#define CUSTOM_LIMITED_DESCRIPTOR_UUID    "2000fbce-74ee-43ce-86b2-0dde20dcefd6"

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

class BooleanCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    String value = pCharacteristic->getValue();
    Serial.print("Received value: ");
    if (!value.isEmpty()) {
      if (value[0]) {
        Serial.println("true");
      } else {
        Serial.println("false");
      }
    }
  }
};

void setup() {
  Serial.begin(115200);

  // Initialize BLE device with a name
  BLEDevice::init("Multiple Masks Device");

  // Create a BLE server and set its callback class
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  // Create a BLE service with a predefined UUID
  // Service handles by default 15 handles. Each BLE Characteristic takes 2 handles and each BLE Descriptor takes 1 handle.
  BLEService *pService = pServer->createService(BLEUUID(SERVICE_UUID), 30);  //15 handlers
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

  //LIMITED DESCRIPTOR FOR SERVICE NAME
  BLEDescriptor *serviceNameLimitedDescriptor = new BLEDescriptor(CUSTOM_LIMITED_DESCRIPTOR_UUID, 200);
  serviceNameLimitedDescriptor->setValue(
    R"({"type":"serviceName", "order":1})");
  pCharacteristicServiceName->addDescriptor(serviceNameLimitedDescriptor);

  // Set an initial value for the characteristic
  pCharacteristicServiceName->setValue("Service 1");

  // If you add or remove characteristics, it may be necessary to forget the device
  // in the Bluetooth settings and re-pair it on Android for changes to take effect.

  // Text field: editable control for string characteristics
  // Supports UTF-8 encoding
  // If the characteristic is not writable, the "disabled" property is ignored, and the control remains disabled.
  BLECharacteristic *pCharacteristicText = pService->createCharacteristic(
    CHARACTERISTIC_TEXT_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
  );

  pCharacteristicText->setCallbacks(new StringCharacteristicCallbacks());

  //! The default maximum length of a descriptor is 100 bytes. Setting a descriptor value that exceeds this limit will cause a crash during startup.
  BLEDescriptor *textDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200); //200 bytes descriptor length
  textDescriptor->setValue(
    R"({"type":"text", "order":1, "disabled":false, "label":"My Text Field Label", "maxBytes": 80})"); //maxBytes specifies the maximum number of bytes. It can be set up to 512 bytes. The default value is 512 bytes.

  pCharacteristicText->addDescriptor(textDescriptor);

  //LIMITED DESCRIPTOR FOR TEXT FIELD
  BLEDescriptor *textLimitedDescriptor = new BLEDescriptor(CUSTOM_LIMITED_DESCRIPTOR_UUID, 200); //200 bytes descriptor length
  textLimitedDescriptor->setValue(
    R"({"type":"text", "order":1, "disabled":false, "label":"My Text Field Label", "maxBytes": 20})"); //20 Bytes only for limited users :)
  pCharacteristicText->addDescriptor(textLimitedDescriptor);

  pCharacteristicText->setValue("Hello, World!");
  pService->start();

  //SECOND SERVICE
  BLEService *pService2 = pServer->createService(BLEUUID(SERVICE2_UUID), 15);
  BLECharacteristic *pCharacteristicService2Name = pService2->createCharacteristic(
    CHARACTERISTIC_SERVICE_NAME_UUID,
    BLECharacteristic::PROPERTY_READ);
  BLEDescriptor *service2NameDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  service2NameDescriptor->setValue(R"({"type":"serviceName", "order":2})");
  pCharacteristicService2Name->addDescriptor(service2NameDescriptor);
  pCharacteristicService2Name->setValue("Service 2");

  //Boolean characteristic
  BLECharacteristic *pCharacteristicSwitch = pService2->createCharacteristic(
    CHARACTERISTIC_SWITCH_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
  );

  pCharacteristicSwitch->setCallbacks(new BooleanCharacteristicCallbacks());
  BLEDescriptor *switchDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  switchDescriptor->setValue(
    R"({"type":"switch", "order":1, "disabled":false, label:"Switch"})");

  pCharacteristicSwitch->addDescriptor(switchDescriptor);
  uint8_t b = false;
  pCharacteristicSwitch->setValue(&b, 1);
  
  pService2->start();

  // Start BLE advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);  // Advertise the service UUID
  pAdvertising->addServiceUUID(SERVICE2_UUID);
  pAdvertising->setScanResponse(true);         // Enable scan response
  BLEDevice::startAdvertising();

  Serial.println("BLE server is running and advertising...");
}

void loop() {
}
