import java.util.List;

public class NFA {
    private Node start;
    private List<Node> ends;

    public NFA(Node start, List<Node> ends) {
        this.start = start;
        this.ends = ends;
    }

    public Node getStart() {
        return start;
    }

    public List<Node> getEnds() {
        return ends;
    }
}
