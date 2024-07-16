import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class test {

    public static void main(String[] args) throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        cw.visit(V21, ACC_PUBLIC, "test_class", null, "java/lang/Object", null);
        cw.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "test", "I", null, 1).visitEnd();
        byte[] b = cw.toByteArray();
        java.nio.file.Files.write(Paths.get("test_class.class"), b);
    }   
}