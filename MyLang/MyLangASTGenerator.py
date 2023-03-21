class Type:
    name: str
    fields: str
    interface: str

class Interface:
    constructors: list[Type]
    name: str
    implemented_interfaces: list[str]

    def generate(self):
        if len(self.constructors) > 0:
            self.generate_visitor()
        
        print(f"public static sealed interface {self.name} extends {combine_interfaces(self.implemented_interfaces)} {{")

        if len(self.constructors) > 0:
            print(f"    public <T> T accept({self.name}Visitor<T> visitor);")

        print("}")

        for constructor in self.constructors:
            print(f"public static record {constructor.name}({constructor.fields}) implements {constructor.interface} {{")
            print(f"public <T> T accept({self.name}Visitor<T> visitor) {{")
            print(f"    return visitor.visit{constructor.name}(this);")
            print("}}")

    def generate_visitor(self):
        print(f"public static interface {self.name}Visitor<T> {{")
        for constructor in self.constructors:
            print(f"public T visit{constructor.name}({constructor.name} value);")
        print("}")


def combine_interfaces(interfaces: list[str]) -> str:
    if len(interfaces) == 0:
        return ""
    if len(interfaces) == 1:
        return interfaces[0]
    result = interfaces[0]
    for interface in interfaces[1::]:
        result += f", {interface}"

    return result

def get_interfaces_in(string: str) -> list[Interface]:
    interfaces: list[Interface] = []
    declarations = string.split(";")
    for declaration in declarations:
        if declaration != "":
            interface = Interface()
            parts = declaration.split("=")
            declarationParts = parts[0].strip().split("<")
            interface.name = declarationParts[0].strip()
            interface.implemented_interfaces = declarationParts[1].strip().split(",")
            if len(parts) > 1:
                interface.constructors = get_constructors(parts[1].strip(), interface.name)
            else:
                interface.constructors = []
            interfaces.append(interface)
    return interfaces

def get_constructors(string: str, interface: str) -> list[Type]:
    constructors: list[Type] = []
    parts = string.split("|")
    for part in parts:
        constructor = Type()
        constructorParts = part.split(":")
        constructor.name = constructorParts[0].strip()
        constructor.fields = constructorParts[1].strip()
        constructor.interface = interface
        constructors.append(constructor)
    return constructors

source_string = open("MyLangASTSource.txt").read()
interfaces = get_interfaces_in(source_string)
print("package MyLang;")
print("import java.util.List;")
print("import java.util.Map;")

print("public interface MyLangAST {")
for interface in interfaces:
    interface.generate()

print("}")