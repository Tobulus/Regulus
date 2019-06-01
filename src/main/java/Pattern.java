public class Pattern {
    public static Matcher compile(String regex) {
        return new Compiler().compile(regex);
    }
}
