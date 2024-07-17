import os 
import sys
import tempfile

main_file_name = sys.argv[1]

def run(cmd : str) -> int:
    print(cmd)
    return os.system(cmd)

if run(f"java -jar \"MyLang.jar\" --java mylmyl/{main_file_name}.myl") != 0:
    exit(1)

libs = ["asm-9.7.jar"]

if run(f"javac -cp .;{';'.join(libs)} mylmyl/_{main_file_name}.java") != 0:
    exit(1)

fd, filename = tempfile.mkstemp()
try:
    # print(filename)
    manifest_file = f"Manifest-Version: 1.0\nCreated-By: 21.0.2 (Oracle Corporation)\nMain-Class: mylmyl/_{main_file_name}\nClass-Path: {' '.join(libs)}\n"
    # print(manifest_file)
    os.write(fd, manifest_file.encode())
    os.close(fd)
    jar_name = f"{main_file_name}.jar" if main_file_name != "Main" else "MyLang.jar"
    run(f"jar --create --file {jar_name} --manifest={filename} mylmyl std {' '.join(libs)}")
finally:
    os.remove(filename)