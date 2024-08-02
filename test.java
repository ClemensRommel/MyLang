import java.util.ArrayList;
import java.util.Arrays;
/*
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
*/
public class test {

    public static void main(String[] args) {
        Object o1 = new Object();
        Object o2 = new Object();
        boolean b8 = !o1.equals(o2);
        double val1 = 2;
        double val2 = 3;
        boolean b = val1 <= val2;
        boolean b1 = val1 < val2;
        boolean b2 = val1 == val2;
        boolean b3 = val1 > val2;
        boolean b4 = val1 >= val2;
        boolean b5 = b4 && b3;
        boolean b6 = b5 || b4;
        boolean b7 = b6 ^ b2;
    }   
}