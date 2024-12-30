
# BLE Manager

Aplikace BLE Manager pro android umožňuje snadno spravovat hodnoty charakteristik Vašeho zařízení. Pro použití aplikace s vaším zařízením nepotřebujete žádná dodatečné knihovny. Stačí pouze charakteristiku označit descriptorem, který určí datový typ, požadovanou grafickou komponentu a další dodatečné vlastnosti, jako je například minimální a maximální hodnota. A to je vše. Po připojení aplikace k zařízení se takto označená charakteristika zobrazí v aplikaci.

## Základní pojmy

### Služba
Jedná se o termín z BLE specifikace. Služba poskytuje charakteristiky. Každá služba může mít více charakteristik. V aplikaci se každá služba zobrazí jako tab.

### Charakteristika
Charakteristiky jsou hodnoty, které služba poskytuje. Jde o pole bajtů, které musí klient (Aplikace) interpretovat.

### Descriptor
Hodnota descriptoru popisuje charakteristiku. Existují standardní typy descriptorů, pro BLE Manager však budeme potřebovat custom descriptor, kterým BLE Manageru řekneme, jak má hodnotu interpretovat a jakou grafickou komponentu má použít pro zobrazení. Pro hodnoty descriptru se v aplikaci BLE Manager používá formát JSON.

### Maska UUID descriptoru
Aby aplikace poznala, který descriptor je ten správný custom descriptor, využívá masku descriptrou. Maska se nastavuje u každého zařízení. Může obsahovat hexadecimální číslice a `#`. Na místě `#` může být v UUID descriptoru libovolná číslice. Výchozí maska pro každé přidané zařízení je: `####face-####-####-####-############`. Takové masce vyhovuje například toto UUID `2000face-74ee-43ce-86b2-0dde20dcefd6`. V pokročilých scénářích můžete skrývat, nebo různě interpretovat hodnoty na základě rozdílných masek. 

## Co tedy musíte udělat pro možnost použití vašeho zařízení v aplikaci BLE Manager?

 - Nastavit custom descriptor. To je vše.
 
 Tento ukázkový descriptor zajistí, že se charakteristika zobrazí v aplikaci jako textové pole. Pokud má charakteristika možnost zápisu a máte tuto možnost zakoupenu, pak bude textové pole editovatelné:
```
BLEDescriptor *textDescriptor = new BLEDescriptor(CUSTOM_DESCRIPTOR_UUID, 200);
textDescriptor->setValue(
    R"({"type":"text", "order":1, "disabled":false, "label":"My Text Field Label", "maxBytes": 80})"
);
pCharacteristicText->addDescriptor(textDescriptor);
```  
## Průvodce
Všechny příklady, na které bude odkazováno, jsou určeny pro ESP32. Ale BLE manager lze použít s libovolným zařízením. 

### Pojmenováváme službu
Aby se v aplikaci zobrazil lidsky čitelný název služby, musí služba poskytovat charakteristku, jejíž hodnota se použije jako název. Takovou charakteristiku označíme descriptorem s UUID odpovídající masce a hodnotou ve formátu JSON. Například `{"type":"serviceName", "order":1}`
Vlastnost `"type":"serviceName"` říká, že tato charakteristika se má interpretovat jako název služby. Vlastnost `"order":1` určuje pořadí zobrazení v aplikaci.

### Popisujeme charakteristiku
Pokud máme v zařízení například charakteristiku obsahující editovatelnou textovou hodnotu. Můžeme k ní přidat přidat descriptor s touto hodnotou: `{"type":"text", "order":1, "disabled":false, "label":"My Text Field Label", "maxBytes": 80}`
Vlastnost `"type":"text"` říká, že se jedná o textovou hodnotu. Vlastnost `"order":1` určuje pořadí zobrazení v aplikaci. Vlastnost `"disabled":false` umožňuje řídit editovatelnost. Toto nastavení je však podřízeno možnostem charakteristiky. Pokud charakteristika není zapisovatelná, pak `"disabled":false` nebude mít efekt. Vlastnost `"label":"My Text Field Label"` určuje s jakým názvem se textové pole v aplikaci zobrazí. Vlastnost `"maxBytes": 80` omezuje maximální počet bajtů, které lze zapsat.

### Pokročilý scénář

Tento odstavec zatím klidně můžete přeskočit a vrátit se k němu, až si vyzkoušíte základní funkce.
Představte si situaci, že máte termostat s připojením na WI-FI. Chcete, aby většina členů domácnosti mohla nastavovat pouze teplotu v určitém rozsahu, ale vy chcete mít možnost nastavovat také připojení k WI-FI a občas si trochu více přitopit :-). Můžete toho docílit tak, že si vytvoříte dva descriptory odpovídající různým maskám. Například `####fBce-####-####-####-############` a `####fAce-####-####-####-############`. Descriptorem s UUID odpovídající první masce popíšete všechny vlastnosti zařízení a tuto masku si nastavíte ve vaší aplikaci. Descriptorem odpovídající druhé masce popíšete pouze charakteristiku nastavující teplotu, přičemž hodnota descriptrou může určovat jiné limity pro tuto charakteristiku. Tuto masku nastavíte v aplikacích ostatních členů domácnosti, nebo nemusíte nastavovat nic, pokud pro tyto účely použijete výchozí masku.

### Hodnoty descriptoru
Hodnoty descriptoru určují, jakým způsobem se intepretují hodnoty a jaká grafická komponenta se použije pro zobrazení.
V BLE komunikaci se pro interpretaci hodnot charakteristik většinou používá formát Little Endian, nicméně všechny komponenty interpretující vícebajtová čísla existují i ve variantě Big Endian.
Všechny komponenty podporují notifikace/indikace ze zařízení. V případě, že budete využívat notifikace/indikace, možná bude nutné v apliakci zapnout sjednání vyjednávání maximální MTU. Ve výchozím stavu je MTU 23 bajtů. Notifikace/Indikace umožňuje odeslat data o velikosti MTU - 3. Nastavení Vyjednat maximální MTU umožní v závislosti na zařízení použít MTU až 517 bajtů.

### Seznam možných hodnot descriptoru
Hodnoty descriptoru jsou ve formátu JSON. Parsování je poměrně benevoletní, nicméně klíče vlastností jsou case sensitive. Pokud má komponenta možnost omezení maximální a minimální hodnoty, výchozí nastavení odpovídá maximální a minimální hodnotě daného datového typu. U textu je výchozí maximální hodnota 512 bajtů. Každá hodnota descriptrou obsahuje vlastnost order. Order určuje pořadí zobrazení v aplikaci. Pokud není order nastaven, je pořadí určenou hodnotou UUID. Níže jsou uvedeny příklady jednotlivých nastavení s popisem chování.
#### Název služby

    {"type":"serviceName", "order":1}

Descriptorem s touto hodnotou označíme charakteristiku, jejíž hodnota se bude interpretovat jako text a použije se pro název tabu v aplikaci. Každá služba může obsahovat jednu takto popsanou charakteristiku.
#### *Texts*
#### Text View

    {"type":"textView", "order":1, "disabled":false}

Hodnota takto popsané charakteristiky bude interpretována jako text a zobrazena jen pro čtení.

#### Title

    {"type":"titleView", "order":1, "disabled":false}

Hodnota takto popsané charakteristiky bude interpretována jako text a zobrazena větším písmem jen pro čtení.

#### Rich Text View

    {"type":"richTextView", "order":1, "disabled":false}

Hodnota této charakteristky bude interpretována jako text ve formátu JSON a zobrazena jen pro čtení.

    {"text":"Colored Text", "color":"#000000", "background":"#F2E605", "title":true}

Hodnota charakteristkiy může mít tyto vlastnosti:
**text**
Zobrazený text
**color**
Barva textu.
Možné formáty: #RRGGBB nebo #AARRGGBB 
**background**
Barva pozadí textu
**title**
Pokud je `true`, bude použito větší písmo.

#### Text

    {"type":"text", "order":1, "disabled":false, "label":"Text Field Label", "maxBytes": 30}

Zapisovatelné textové pole. maxBytes umožňuje omezit maximální počet zadaných bajtů (ne počet znaků). Maximálně je možné zadat 512 bajtů. Používá kódování se UTF-8.

#### Password

    {"type":"password", "order":1, "disabled":false, label:"Pasword Field Label", "maxBytes": 30}

Zapisovatelné textové pole pro zadání hesla. maxBytes umožňuje omezit maximální počet zadaných bajtů (ne počet znaků). Maximálně je možné zadat 512 bajtů. Používá kódování se UTF-8.

#### PIN

    {"type":"pin", "order":1, "disabled":false, label:"PIN Field Label", "maxBytes": 30}

Zapisovatelné textové pole pro zadání číselného hesla. maxBytes umožňuje omezit maximální počet zadaných bajtů (ne počet znaků). Maximálně je možné zadat 512 bajtů. Používá kódování se UTF-8.

#### *Signed Integers*

#### SInt8

    {"type":"sint8", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-100, "maxInt":100}
    
Zapisovatelné pole pro 8bitový signed integer.

#### SInt16

    {"type":"sint16", "order":1, "disabled":false, "label":"Signed Int16", "minInt":-100, "maxInt":100}

Zapisovatelné pole pro 16bitový signed integer.

#### SInt32

    {"type":"sint32", "order":1, "disabled":false, "label":"Signed Int32", "minInt":-100, "maxInt":100}

Zapisovatelné pole pro 32bitový signed integer.

#### SInt64

    {"type":"sint64", "order":1, "disabled":false, "label":"Signed Int64", "minInt":-100, "maxInt":100}

Zapisovatelné pole pro 64bitový signed integer.

#### *Signed Integer Sliders*

#### SInt8Slider

    {"type":"sint8slider", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-50, "maxInt":50, "stepInt":1}

Slider pro nastavení 8bitového signed integeru. Vlastnot stepInt určuje velikost kroku.

#### SInt16Slider

    {"type":"sint16slider", "order":1, "disabled":false, "label":"Signed Int16", "minInt":-50, "maxInt":50, "stepInt":1}

Slider pro nastavení 16bitového signed integeru. Vlastnot stepInt určuje velikost kroku.

#### *Unsigned Integers*
#### UInt8

    {"type":"uint8", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":0, "maxInt":100}

Zapisovatelné pole pro 8bitový unsigned integer.

#### UInt16

    {"type":"uint16", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":0, "maxInt":100}

Zapisovatelné pole pro 16bitový unsigned integer.

#### UInt32

    {"type":"uint32", "order":1, "disabled":false, "label":"Unsigned Int32", "minInt":0, "maxInt":100}

Zapisovatelné pole pro 32bitový unsigned integer.

#### UInt64

    {"type":"uint64", "order":1, "disabled":false, "label":"Unsigned Int64", "minInt":0, "maxInt":100}

Zapisovatelné pole pro 64bitový unsigned integer.

#### *Unsigned Integer Sliders*

#### UInt8Slider

    {"type":"uint8slider", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":0, "maxInt":100, "stepInt":1}

Slider pro nastavení 8bitového unsigned integeru. Vlastnot stepInt určuje velikost kroku.

#### UInt16Slider

    {"type":"uint16slider", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":0, "maxInt":100, "stepInt":1}

Slider pro nastavení 16bitového unsigned integeru. Vlastnot stepInt určuje velikost kroku.

#### *Floats*

#### Half

    {"type":"half", "order":1, "disabled":false, "label":"Float 16", "minFloat": -100, "maxFloat": 100}

Zapisovatelné pole pro 16bitový float.

#### Float

    {"type":"float", "order":1, "disabled":false, label:"Float 32", "minFloat": -100, "maxFloat": 100}

Zapisovatelné pole pro 32bitový float.

#### Double

    {"type":"double", "order":1, "disabled":false, label:"Float 64", "minFloat": -100, "maxFloat": 100}

Zapisovatelné pole pro 64bitový float.

#### *Float Sliders*

#### HalfSlider

    {"type":"halfslider", "order":1, "disabled":false, "label":"Float 16", "minFloat": -50, "maxFloat": 50, "stepFloat": 0.1}

Slider pro nastavení hodnoty 16bitového float. Vlastnot stepFloat určuje velikost kroku.

#### FloatSlider

    {"type":"floatslider", "order":1, "disabled":false, label:"Float 32", "minFloat": -50, "maxFloat": 50, "stepFloat": 0.1}

Slider pro nastavení hodnoty 32bitového float. Vlastnot stepFloat určuje velikost kroku.

#### *Booleans*

#### Check

    {"type":"check", "order":1, "disabled":false, label:"Checkbox"}

Checkbox pro nastavení boolean hodnoty 8bitové charakteristiky. Čte 0 = false, jinak true. Zapisuje false = 0, true = 1 

#### Switch

    {"type":"switch", "order":1, "disabled":false, label:"Switch"}

Switch pro nastavení boolean hodnoty 8bitové charakteristiky. Čte 0 = false, jinak true. Zapisuje false = 0, true = 1 
#### *Actions*
#### Button

    {"type":"button", "order":1, "disabled":false, "label":"Button"}

Pos stisknutí nastaví hodnotu 8bitového unsigned integeru na hodnotu o jedna vyšší. Začíná na 0.

#### *Colors*
#### Color

    {"type":"color", "order":1, "disabled":false, "label":"Color", "alphaSlider":true}

Colorpicker pro výběr barvy. Pracuje s 32bitovou ABGR hodnotou, kde R je uložno na 1. bajtu. 0xFF05E6F2; //R:0xF2, G:0xE6, B:0x05, A:0xFF
Vlastnost alphaSlider určuje, zda se zobrazí slider pro nastavení alfa kanálu.

#### *Date and Time*

#### Time

    {"type":"time", "order":1, "disabled":false, "label":"Time"}

Umožňuje nastavit čas jako 32bitový unsigned integer. Hodnota udává počet sekund od půlnoci.

#### Date32

    {"type":"date32", "order":1, "disabled":false, "label":"Date 32"}

Umožňuje nastavit 32bitové datum. Hodnota určuje počet sekund od začátku Unix epochy. Čas je vždy 0:00.

#### Date64

    {"type":"date64", "order":1, "disabled":false, "label":"Date 64"}

Umožňuje nastavit 64bitové datum. Hodnota určuje počet sekund od začátku Unix epochy. Čas je vždy 0:00.

#### DateTime32

    {"type":"datetime32", "order":1, "disabled":false, "label":"DateTime 32"}

Umožňuje nastavit 32bitový datum a čas. Hodnota určuje počet sekund od začátku Unix epochy.

#### DateTime64

    {"type":"datetime64", "order":1, "disabled":false, "label":"DateTime 64"}

Umožňuje nastavit 32bitový datum a čas. Hodnota určuje počet sekund od začátku Unix epochy.

#### Dropdown

    {"type":"dropdown", "order":1, "disabled":false, label:"Dropdown Menu", "options":["Option1","Option2","Option3"]}

Umožňuje výběr jedné z textových hodnot z pole options.

### BigEndian varianty komponent
8bitové varinty se mohou zdát jako nadbytečné, ale jsou zde z důvodu konzistentní interpretace dat. Každá integer komponenta dokáže totiž zpravocat libovolně dlouhou hodnotu tak, že ji ořízne na požadovaný počet bajtů.

#### Big Endian Signed Integers
{"type":"sint8be", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-100, "maxInt":100}
{"type":"sint16be", "order":1, "disabled":false, "label":"Signed Int16", "minInt":-100, "maxInt":100}
{"type":"sint32be", "order":1, "disabled":false, "label":"Signed Int32", "minInt":-100, "maxInt":100}
{"type":"sint64be", "order":1, "disabled":false, "label":"Signed Int64", "minInt":-100, "maxInt":100}

#### Big Endian Signed Integer Sliders
{"type":"sint8sliderbe", "order":1, "disabled":false, "label":"Signed Byte", "minInt":-50, "maxInt":50, "stepInt":1}
{"type":"sint16sliderbe", "order":1, "disabled":false, "label":"Signed Int16", "minInt":-50, "maxInt":50, "stepInt":1}

#### Big Endian Unsigned Integers
{"type":"uint8be", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":0, "maxInt":100}
{"type":"uint16be", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":0, "maxInt":100}
{"type":"uint32be", "order":1, "disabled":false, "label":"Unsigned Int32", "minInt":0, "maxInt":100}
{"type":"uint64be", "order":1, "disabled":false, "label":"Unsigned Int64", "minInt":0, "maxInt":100}

#### Big Endian Unsigned Integer Sliders
{"type":"uint8sliderbe", "order":1, "disabled":false, "label":"Unsigned Byte", "minInt":0, "maxInt":50, "stepInt":1}
{"type":"uint16sliderbe", "order":1, "disabled":false, "label":"Unsigned Int16", "minInt":0, "maxInt":50, "stepInt":1}

#### Big Endian Floats
{"type":"halfbe", "order":1, "disabled":false, "label":"Float 16", "minFloat":-100, "maxFloat":100}
{"type":"floatbe", "order":1, "disabled":false, label:"Float 32", "minFloat":-100, "maxFloat":100}
{"type":"doublebe", "order":1, "disabled":false, label:"Float 64", "minFloat":-100, "maxFloat":100}

#### Big Endian Float Sliders
{"type":"halfsliderbe", "order":1, "disabled":false, "label":"Float 16", "minFloat": -50, "maxFloat": 50, "stepFloat": 0.1}
{"type":"floatsliderbe", "order":1, "disabled":false, label:"Float 32", "minFloat": -50, "maxFloat": 50, "stepFloat": 0.1}

#### Big Endian Date and Time
{"type":"timebe", "order":1, "disabled":false, "label":"Time"}
{"type":"date32be", "order":1, "disabled":false, "label":"Date 32"}
{"type":"date64be", "order":1, "disabled":false, "label":"Date 64"}
{"type":"datetime32be", "order":1, "disabled":false, "label":"DateTime 32"}
{"type":"datetime64be", "order":1, "disabled":false, "label":"DateTime 64"}



  
