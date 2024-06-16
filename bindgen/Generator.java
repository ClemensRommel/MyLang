package bindgen;
class Generator {
    public static void main(String[] args) throws ClassNotFoundException {
        if(args.length != 2) {
            System.err.println("Usage: java Generator <input> <output>");
            System.exit(1);
        }
        String targetClass = args[0];
        String targetDir = args[1];

        ClassGenerator.generate(targetClass, targetDir);
    }
}