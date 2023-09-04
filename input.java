class Main {
    public static final String helloWorld;static {
        helloWorld = "Hello world";
    }
    public static double plusThree(double x){
        return (x + 3);
    }
    public static Void printHelloWorld(){
        final Void __a0;
        {
            System.out.println(helloWorld);
            __a0 = null;
        }
        return __a0;
    }
    public static void main(){
        final Void __a1;
        {
            printHelloWorld();
            System.out.println(plusThree(4));
            __a1 = null;
        }
        Object __a2 = __a1;
    }
    
    public static void main(String[] args) {
        main();
    }
    
}
