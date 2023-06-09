# MyLang
Meine bisher unbenannte Programmiersprache. Bisher gibt es einen Parser, einen Typechecker, der das Programm überprüft und einen Interpreter, der das Programm ausführt.

# Programmstruktur
Ein Programm besteht aus Deklarationen von Funktionen, Variablen und Datentypen. Das Programm startet dabei, wie in vielen anderen Programmiersprachen, in der Main-Funktion. Zu Funktionen aber später mehr

# Ausdrücke
Die Sprache unterstützt viele übliche Ausdrücke, wie z.B. `+`, das zwei Zahlen addiert oder zwei Strings verbindet oder `-`
, `*`, `/`, die für Subtraktion, Multiplikation und Division stehen. Außerdem gibt es noch `%`, das den Rest zweier Zahlen berechnet
Diese sind, wie einige andere Operatoren, Infix-Operatoren. Es gibt z.B. auch `^`, das die linke mit der rechten Zahl potenziert.

Gleichheit wird überprüft mit `=`, nicht wie häufig `==`. Zuweisung ist stattdessen `:=`. Es gibt auch `!=`, das Ungleichheit überprüft, und `<`, `<=` und `>`, `>=`, die überprüfen ob zwei Zahlen größer oder kleiner sind.

Bedingungen können verbunden werden duch `and` oder `or`, z.B. `3 > 2 and 2 < 3`.

Zahlen, Strings und Booleans können durch Literals erzeugt werden, z.B. `3` oder `"Dies ist ein String"`

`!`, `+` und `-` können als Präfix-Operatoren verwendet werden, die die übliche Bedeutung haben

Funktionen werden aufgerufen durch `funktion(parameter1, parameters2, ...)`

`if` ist kein Statement, sondern ein Ausdruck der Form `if bedingung then dannWert else sonstWert`. 

# Funktionen
Funktionen werden deklariert durch `fun`, gefolgt vom Namen, den Parametern, dem Rückgabetyp sowie den Rückgabe wert

Beispiel:
~~~
fun f(x: Number): Number := 2*x + 3;
~~~

Es können auch anonyme Funktionen als Ausdruck erzeugt werden, dabei fehlt dann der Nahme

~~~
var f := fun(x: Number): Number := 2*x + 3;
~~~

Diese können auch als Parameter oder Rückgabewert von anderen Funktionen verwendet werden

# Variablen
Variablen werden durch `var` oder `val` deklariert. Variablen deklariert mit var können mit `:=` verändert werden.

Beispiel:
~~~
var a: Number := 3;
a := 4; // a ist jetzt 4

val b: Number := 5;
b := 6; // Fehler: b kann nicht verändert werden
~~~
# Blöcke
Lokale Variablen können auf einen Teil des Codes reduziert werden durch Blöcke. Diese bestehen durch eine Folge von Statements oder Deklarationen, gefolgt von einem Ausdruck, der der Wert des ganzen Blocks ist. Blöcke beginnen mit `do` und enden mit `end`.

Beispiel: 
~~~
fun f(x: Number): Number := do 
  var a := x;
  a := a*a;
  a + 2
end;

// Wenn der Körper einer Funktion ein Block ist, kann das := und das ; weggelassen werden

fun f(x: Number): Number do 
  var a := x;
  a := a*a;
  a + 2
 end
~~~
In Funktionen kann auch return verwendet werden, z.B. in Verbindung mit dem `if`-`do`-Statement, das keinen sonst-Teil hat:
~~~
fun teileZuString(x: Number): String do 
  if x < 0 do 
    return "Kann nicht durch 0 teilen";
  end
  
  "Ergebnis: "+3/x
end
~~~
# Listen
Listen können einfach als Ausdruck konstruiert werden:
~~~
val a: Number[] := [1, 2, 3, 4];
// Man kann eine Liste von einer bis zu einer anderen Zahl vereinfacht konstruieren durch Ranges:
val b: Number[] := [1..6]; // [1, 2, 3, 4, 5]
// Listen können in eine neue Liste hinzugefügt werden durch den Spread-Operator
val c: Number[]  := [b.., 6, 7, 8]; // [1, 2, 3, 4, 5, 6, 7, 8]
// Es kann auch eine Sonderform von if verwendet werden, die ein Element nur unter einer Bedingung einfügt:
val d: Number[] := [1, 2, 3 if bedingung]; // wenn bedingung wahr ist [1, 2, 3] sonst [1, 2]

// Die Elemente einer Liste können durch ihre position gelesen oder verändert werden
val e: Number[] := d[1]; // 2
// Die Positionen beginnen bei 0
c[3] := 6; // c ist jetzt [1, 2, 3, 6, 5, 6, 7, 8]
~~~

# Schleifen
Es gibt zwei Arten von Schleifen, `for` und `while`-Schleifen. Es gibt jeweils eine `do` und eine `yield`-Variante.
Die `yield`-Variante sammelt die Ergebnisse aller Schleifendurchläufe in einer Liste zusammen
## While-Schleife
Die While-Schleife führt den Körper solange aus, wie eine Bedingung wahr ist

Beispiel:
~~~
var x := 10;
while x > 0 do 
  x := x - 1;
end;
x; // x ist jetzt 0
~~~
## For-Schleife
Die For-Schleife iteriert über die Elemente einer Liste. Einzelne Elemente können durch eine Bedingung übersprungen werden

Beispiel:
~~~
for x in [1..5] do 
  print(x); // druckt 1 2 3 4 nacheinander aus
end 

for i in [1..10] if i % 2 = 0 do 
  print(i); // Druckt 2 4 6 8 aus
end
~~~

# Datentypen
## Eingebaute Typen
Es gibt 4 eingebaute Datentypen, Zahlen `Number`, Booleans `Boolean`, Strings `String`, und den Rückgabetyp von Funktionen, die nichts zurückgeben `Void`, dessen einziger Wert `null` ist. Null ist bei keinem anderen Typ erlaubt
## Listen
Die Typen von Listen werden konstruiert, indem an den Typ der Elemente ein `[]` angehängt wird, wie z.B. `Number[]` für Listen von Zahlen
## Typen von Funktionen
Die Typen von Funktionen werden durch `Fun` geschrieben, gefolgt von den Parametertypen in Klammern und dem Rückgabetyp, z.B. `Fun(Number, Number): Number`
### Optionale Parameter
Parameter von Funktionen können als Optional markiert werden. In diesem Fall muss ein Standardwert deklariert werden.
~~~
fun neueKoordinate([x: Number := 0, y: Number := 0]): Koordinate := Koordinate(x, y);
neueKoordinate(3); // 3, 0
neueKoordinatee(); // 0, 0
neueKoordinate(3, 4); // 3, 4
~~~
### Benannte Parameter
Parameter können so deklariert werden, dass man ihren Namen sagen muss, wenn man sie als Parameter übergeben will
~~~
fun neueKoordinate({x: Number, y: Number}): Koordinate := Koordinate(x, y);
neueKoordinate(x: 3, y: 4); // 3, 4
~~~
### Funktionen mit variabler Anzahl an Parametern
Funktionen können angeben, dass beliebig viele Parameter übergeben werden können. Auf diese kann durch eine Liste zugreifen
~~~
fun listOf(xs..: Number): Number := for i in xs yield 2*i end;

listOf(3, 4, 5);// [6, 8, 10]
~~~
## Klassen
Klassen können deklariert werden durch `class`. Diese können Attribute, Methoden und einen Konstruktor festlegen

Beispiel: 
~~~
class Koordinate where 
  var x: Number; // Müssen nicht initialisiert werden
  var y: Number;
  
  new(x: Number, y: Number) do // Konstruktor wird deklariert durch new 
      this.x := x; // Auf attribute **muss** durch this zugegriffen werden
      this.y := y;
  end;
  
  fun drucke() do 
    print(x, ", ", y);
  end;
end;

// Außerhalb:
fun addiere(x: Koordinate, y: Koordinate): Koordinate := Koordinate(x.x + y.x, x.y + y.y);

// Referenzen zu Methoden sind Möglich:
val druckeFunktion := Koordinate(3, 4).drucke; // Funktion druckt "3, 4"
~~~
## Tupel
Tupel sind Listen bestimmter, festgelegter Größe, die mehrere Elemente verschiedenen Typs haben können, z.B. `(3, "hallo", true, Koordinate(3, 5))`. Sie können durch Pattern-matching verwendet werden

Der Typ von Tupel wird ähnlich den Tupel selber geschrieben:
~~~
val x: (Number, String, Boolean, Koordinate) := (3, "hallo", true, Koordinate(3, 5));
~~~
## Enums
Enums sind Datentypen, die eine oder mehrere Variante sein können. Jede Variante wird zu einem Konstruktor, der mehrere Parameter festlegen kann.

Beispiel: 
~~~
enum StringOderZahl where 
  Text(String),
  Zahl(Number);
end
~~~

Diese können durch Pattern-matching verwendet werden:
### Patterns
Patterns können entweder ein Konstruktor `konstruktor(element1, element2, element3)` sein, ein Tupel `(element1, element2, element3)` oder Variablen `variable`, Literals `3`, `"ein string"`, oder Wildcards `?` sein. Wildcards ignorieren das Objekt, auf dem gematched wird.

For-Schleifen können Patterns verwenden, um nur über die Elemente zu iterieren, die zu einem Pattern passen
### Match
`match`-Ausdrücke definieren einen Wert für jede Möglichkeit:
~~~
val x: String := match Text("test") do 
    case Text(text) := "String "+text;
    case Zahl(n)    := "Zahl "+n;
end;
~~~
### if val
Eine erweiterung von `if`, bei der der Dann-Teil nur ausgeführt wird, wenn ein Pattern zutrifft
~~~
val x: String := if val Text(txt) := Text("test") then "String "+txt else "Eine Zahl";
~~~
### While val
Eine Erweiterung von `while`, die solange läuft, wie ein Pattern zutrifft
~~~
var iterator = sammlung.iterator();
while val NächstesElement(element) := iterator.next() do 
  print(element);
end;
~~~
### Val-Else
Variablendeklarationen sind auch erweitert. Entweder wird eine oder mehrere Lokale Variablen deklariert, wenn ein Pattern zutrifft, oder ein Codeblock wird ausgeführt, der `return` verwenden muss
~~~
val Inhalt(inhalt) := öffneDatei(datei) else return "Fehler";
~~~
Normale Variablendeklarationen ohne else können nur Patterns verwenden, die immer erfolgreich sind, also Variablen, wildcards, und Tupel
## Typdeklarationen
Typen kann ein anderer, zusätzlicher Name gegeben werden durch `type`-Deklarationen
~~~
type Zahlfunktion := Fun(Number): Number;
~~~
