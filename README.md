# BLE Manager
Aplikace BLE Manager pro android umožňuje snadno spravovat hodnoty charakteristick Vašeho zařízení. Pro použití nepotřebujete žádná dodatečné knihovny. Stačí pouze charakteristiku označit descriptorem, který určí datový typ, požadovanou grafickou komponentu a další dodatečné vlastnoti, jako je například minimální a maximální hodnota. A to je vše. Po připojení aplikace k zařízení se takto označená charakteristika zobrazí v aplikaci.

Základní pojmy
Služba
Jedná se o termín z BLE specifikace. Služba poskytuje charakteristiky. Služba může mít více charakteristik. V aplikaci se každá služba zobrazí jako tab.
Charakteristika
Charakteristiky jsou hodnoty, které služba poskytuje. Jedná se o pole bajtů, které musí klient (Aplikace) interpretovat.

Descriptor
Hodnota desxriptoru popisuje charakteristiku. Existují standardní typy descriptorů, pro BLE Manager však budeme potřebovat custom descriptor, kterým BLE Manageru řekneme, jak má hodnotu interpretovat a jakou grafickou komponentu má použít pro zobrazení. Pro hodnoty descriptru se v aplikaci BLE Manager používá formát JSON.

Maska UUID descriptoru.
Aby aplikace poznala, který descriptor je ten správný custom descriptor, využívá masku descriptrou. Maska se nastavuje u každého zařízení a je case insensitive. Může obsahovat hexadecimální číslice a #. Na místě # může být v UUID descriptoru libovolná číslice. Výchozí maska pro každé přidané zařízení je ####face-####-####-####-############. Takové masce vyhovuje například toto UUID 2000face-74ee-43ce-86b2-0dde20dcefd6. V pokročilých scénářích můžete skrývat, nebo různě interpretovat hodnoty na základě rozdílných masek. 

Co tedy musíte udělat pro možnost použití vašeho zařízení v aplikace BLE Manager?
Nastavit custom descriptor. To je vše.
Tento ukázkový descriptor zajistí, že se charakteristika zobrazí v aplikaci jako textové pole. Pokud má charakteristika možnost zápisu a máte zakoupenu možnost zápisu pak bude textové pole editovatelné.
  BLEDescriptor *textDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
  textDescriptor->setValue(
    R"({"type":"text", "order":1, "disabled":false, "label":"My Text Field Label", "maxBytes": 80})");

  pCharacteristicText->addDescriptor(textDescriptor);

Průvodce
Všechny následující příklady jsou určeny pro ESP32. Ale BLE manager lze použít s libovolným zařízením.
Pojmenováváme službu
Aby se v aplikaci zobrazil lidsky čitelný název služby, musí služba poskytovat charakteristku, jejíž hodnota se použije jako název. Takovou charakteristiku označíme descriptorem s UUID odpovídající masce a hodnotou ve formátu JSON {"type":"serviceName", "order":1}.
"type":"serviceName" říká, že tato charakteristika se má interpretovat jako název charakteristiky. "order":1 určuje pořadí zobrazení v aplikaci.

Popisujeme charakteristiku
Pokud máme v zařízení charakteristiku obsahující editovatelnou textovou hodnotu. Můžeme k ní přidat přidat descriptor s touto hodnotou. {"type":"text", "order":1, "disabled":false, "label":"My Text Field Label", "maxBytes": 80}
"type":"text" říká, že se jedná o textvou hodnotu. "order":1 určuje pořadí zobrazení v aplikaci. "disabled":false umožňuje řídit editovatelnost. Toto nastavení je však podřízeno možnostem charakteristiky. Pokud charakteristika není zapisovatelná, pak "disabled":false nebude mít efekt. "label":"My Text Field Label" určuje s jakým názvem se textové pole v aplikaci zobrazí. "maxBytes": 80 Tímto lze omezit počet bajtů, které lze zapsat.

Pokročilý scénář
Tento odstavec zatím klidně můžete přeskočit a vrátit se k němu, až si vyzkoušíte základní funkce. Představte si situaci, že máte termostat s připojením na WI-FI. Chcete, aby většina členů domácnosti mohla nastavovat pouze teplotu v určitém rozsahu, ale vy chcete mít možnost nastavovat také připojení k WI-FI a občas si trochu více přitopit :). Můžete toho docílit tak, že si vytovoříte dva descriptory odpovídající různým maskám. Například ####fBce-####-####-####-############ a ####fAce-####-####-####-############. Descriptorem s UUID odpovídající jedné první masce popíšete všechny vlastnosti zařízení a tuto masku si nastavíte ve vaší aplikaci. Descriptorem odpovídající druhé masce popíšete pouze charakteristiku nastavující teplotu, přičemž hodnota descriptrou může určovat jiné limity pro tuto charakteristiku. Tuto masku nastavíte v aplikacích ostatních členů domácnosti, nebo nemusíte nastavovat nic, pokud pro tyto účely použijete výchozí masku.

Hodnoty descriptoru
Texts
Signed Integers
Signed Integer Sliders
Unsigned Integers
Signed Integer Sliders
Floats
Float Sliders
Booleans
Button
Color
Date and Time
Dropdown



  
