import java.util.List;

public class Fragment {
    private Node start;
    private List<Transition> dangling;

    public Fragment(Node start, List<Transition> dangling) {
        this.start = start;
        this.dangling = dangling;
    }

    public Node getStart() {
        return start;
    }

    public List<Transition> getDangling() {
        return dangling;
    }

    public void setDangling(List<Transition> dangling) {
        this.dangling = dangling;
    }
}
