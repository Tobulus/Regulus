
import java.util.Arrays;
import java.util.function.Predicate;

public class MetaCharacters {

    private static Predicate<Character>[] metaCharacters = new Predicate[256];

    static {
        Arrays.stream(metaCharacters).forEach(entry -> entry = null);
        metaCharacters['d'] = (c) -> Character.isDigit(c);
        metaCharacters['D'] = (c) -> !Character.isDigit(c);
        metaCharacters['s'] = (c) -> Character.isWhitespace(c);
        metaCharacters['S'] = (c) -> !Character.isWhitespace(c);
        metaCharacters['w'] = (c) -> Character.isLetterOrDigit(c) || c.equals('_');
        metaCharacters['W'] = (c) -> !(Character.isLetterOrDigit(c) || c.equals('_'));
    }

    public static boolean exists(Character character) {
        return metaCharacters[character] != null;
    }

    public static Predicate<Character> getHandler(Character character) {
        return metaCharacters[character];
    }
}
