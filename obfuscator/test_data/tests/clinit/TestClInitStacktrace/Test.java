public class Test {

    private static void printStackTrace() {
        String platform = System.getProperty("platform");
        StackTraceElement[] stack = new Exception().getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            if ("ANDROID".equals(platform) && stack[i].getClassName().equals("native0.hidden.Hidden0")) {
                continue;
            }
            System.out.printf("%d: %s.%s\n", i, stack[i].getClassName(), stack[i].getMethodName());
        }
    }

    public static void main(String[] args) {
    }

    static {
        printStackTrace();
    }
}