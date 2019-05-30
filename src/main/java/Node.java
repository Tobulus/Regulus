import java.util.*;
import java.util.stream.Collectors;

public class Node {

    private static Integer ID = 0;

    private boolean end = false;
    private Integer id;
    private List<Transition> transitions = new ArrayList<>(3);

    public Node() {
        this.id = ID++;
    }

    public boolean isEnd() {
        return end;
    }

    public void markAsEnd() {
        this.end = true;
    }

    public Integer getId() {
        return id;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void clearTransitions() {
        this.transitions.clear();
    }

    public void addTransition(Transition transition) {
        transition.setStart(this);
        transitions.add(transition);
    }

    public List<Node> getNextNodes(Character character) {
        List<Node> matching = transitions
                .stream()
                .filter(transition -> transition.matches(character))
                .map(Transition::getDestination)
                .collect(Collectors.toList());
        List<Node> expand = new ArrayList<>();
        matching.forEach(n -> followEmptyTransitions(n, expand));
        matching.addAll(expand);

        return matching;
    }

    public void followEmptyTransitions(Node node, List<Node> collector) {
        node.getTransitions().stream()
                .filter(transition -> transition.matches(null))
                .map(Transition::getDestination)
                .forEach(n -> {
                    collector.add(n);
                    followEmptyTransitions(n, collector);
                });
    }
}
