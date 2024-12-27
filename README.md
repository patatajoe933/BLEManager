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
Service Name
{"type":"serviceName", "order":1}
Texts (výchozí maximální počet bajtů je 512)
{"type":"textView", "order":1, "disabled":false}
{"type":"titleView", "order":1, "disabled":false}
{"type":"richTextView", "order":1, "disabled":false} {"text":"Colored Text", "color":"#000000", "background":"#F2E605", "title":true}
{"type":"text", "order":1, "disabled":false, "label":"Text Field Label", "maxBytes": 30}
{"type":"password", "order":1, "disabled":false, label:"Pasword Field Label", "maxBytes": 30}
{"type":"pin", "order":1, "disabled":false, label:"PIN Field Label", "maxBytes": 30}

Signed Integers
{"type":"sint8", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-70, "maxInt":70}
{"type":"sint16", "order":1, "disabled":false, "label":"Signed Int16", "minInt":-7070, "maxInt":7070}
{"type":"sint32", "order":1, "disabled":false, "label":"Signed Int32", "minInt":-707070, "maxInt":707070}
{"type":"sint64", "order":1, "disabled":false, "label":"Signed Int64", "minInt":-7070707070, "maxInt":7070707070}

Signed Integer Sliders
{"type":"sint8slider", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-50, "maxInt":50, "stepInt":1}
{"type":"sint16slider", "order":1, "disabled":false, "label":"Signed Int16", "minInt":0, "maxInt":100, "stepInt":2}

Unsigned Integers
{"type":"uint8", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":60, "maxInt":70}
{"type":"uint16", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":6060, "maxInt":7070}
{"type":"uint32", "order":1, "disabled":false, "label":"Unsigned Int32", "minInt":606060, "maxInt":707070}
{"type":"uint64", "order":1, "disabled":false, "label":"Unsigned Int64", "minInt":6060606060, "maxInt":7070707070}

Unsigned Integer Sliders
{"type":"uint8slider", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":0, "maxInt":100, "stepInt":1}
{"type":"uint16slider", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":100, "maxInt":200, "stepInt":2}

Floats
{"type":"half", "order":1, "disabled":false, "label":"Float 16", "minFloat": -10, "maxFloat": 10}
{"type":"float", "order":1, "disabled":false, label:"Float 32", "minFloat": -20, "maxFloat": 20}
{"type":"double", "order":1, "disabled":false, label:"Float 64", "minFloat": -30, "maxFloat": 30}

Float Sliders
{"type":"halfslider", "order":1, "disabled":false, "label":"Float 16", "minFloat": 0, "maxFloat": 75, "stepFloat": 0.1}
{"type":"floatslider", "order":1, "disabled":false, label:"Float 32", "minFloat": -50, "maxFloat": 50, "stepFloat": 1}

Booleans
{"type":"check", "order":1, "disabled":false, label:"Checkbox"}
{"type":"switch", "order":1, "disabled":false, label:"Switch"}

Button
{"type":"button", "order":1, "disabled":false, "label":"Button"}

Color
{"type":"color", "order":1, "disabled":false, "label":"Color", "alphaSlider":true}

Date and Time
{"type":"time", "order":1, "disabled":false, "label":"Time"}
{"type":"date32", "order":1, "disabled":false, "label":"Date 32"}
{"type":"date64", "order":1, "disabled":false, "label":"Date 64"}
{"type":"datetime32", "order":1, "disabled":false, "label":"DateTime 32"}
{"type":"datetime64", "order":1, "disabled":false, "label":"DateTime 64"}

Dropdown
{"type":"dropdown", "order":1, "disabled":false, label:"Dropdown Menu", "options":["Option1","Option2","Option3"]}

BigEndian

 Big Endian Signed Integers
{"type":"sint8be", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-70, "maxInt":70}
{"type":"sint16be", "order":1, "disabled":false, "label":"Signed Int16", "minInt":-7070, "maxInt":7070}
{"type":"sint32be", "order":1, "disabled":false, "label":"Signed Int32", "minInt":-707070, "maxInt":707070}
{"type":"sint64be", "order":1, "disabled":false, "label":"Signed Int64", "minInt":-7070707070, "maxInt":7070707070}

Big Endian Signed Integer Sliders
{"type":"sint8sliderbe", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-70, "maxInt":70, "stepInt":1}
{"type":"sint16sliderbe", "order":1, "disabled":false, "label":"Signed Int16", "minInt":-20, "maxInt":100, "stepInt":2}

Big Endian Unsigned Integers
{"type":"uint8be", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":60, "maxInt":70}
{"type":"uint16be", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":6060, "maxInt":7070}
{"type":"uint32be", "order":1, "disabled":false, "label":"Unsigned Int32", "minInt":606060, "maxInt":707070}
{"type":"uint64be", "order":1, "disabled":false, "label":"Unsigned Int64", "minInt":6060606060, "maxInt":7070707070}

Big Endian Unsigned Integer Sliders
{"type":"uint8sliderbe", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":0, "maxInt":50, "stepInt":1}
{"type":"uint16sliderbe", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":0, "maxInt":50, "stepInt":2}

Big Endian Floats
{"type":"halfbe", "order":1, "disabled":false, "label":"Float 16", "minFloat": -10, "maxFloat": 10}
{"type":"floatbe", "order":1, "disabled":false, label:"Float 32", "minFloat": -20, "maxFloat": 20}
{"type":"doublebe", "order":1, "disabled":false, label:"Float 64", "minFloat": -30, "maxFloat": 30}

Big Endian Float Sliders
{"type":"halfsliderbe", "order":1, "disabled":false, "label":"Float 16", "minFloat": 0, "maxFloat": 75, "stepFloat": 0.1}
{"type":"floatsliderbe", "order":1, "disabled":false, label:"Float 32", "minFloat": -50, "maxFloat": 50, "stepFloat": 1}

Big Endian Date and Time
{"type":"timebe", "order":1, "disabled":false, "label":"Time"}
{"type":"date32be", "order":1, "disabled":false, "label":"Date 32"}
{"type":"date64be", "order":1, "disabled":false, "label":"Date 64"}
{"type":"datetime32be", "order":1, "disabled":false, "label":"DateTime 32"}
{"type":"datetime64be", "order":1, "disabled":false, "label":"DateTime 64"}



  
