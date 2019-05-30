import java.util.function.Predicate;

public class Transition {
    public static final Predicate<Character> EMPTY_TRANSITION = (c) -> true;

    private Predicate<Character> matcher;
    private Node start;
    private Node destination;

    public Transition(Predicate<Character> matcher, Node destination) {
        this.matcher = matcher;
        this.destination = destination;
    }

    public Node getStart() {
        return start;
    }

    public void setStart(Node start) {
        this.start = start;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public Node getDestination() {
        return destination;
    }

    public boolean matches(Character character) {
        if (character == null) {
            return matcher == EMPTY_TRANSITION;
        }
        if (matcher == EMPTY_TRANSITION) {
            return character == null;
        }
        return matcher.test(character);
    }
}
