import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Matcher {
    private NFA nfa;

    public Matcher(NFA nfa) {
        this.nfa = nfa;
    }

    public boolean test(String s) {
        HashMap<Integer, Node> currentNodes = new HashMap<>();
        List<Node> startNodes = new ArrayList<>();
        currentNodes.put(nfa.getStart().getId(), nfa.getStart());
        nfa.getStart().followEmptyTransitions(nfa.getStart(), startNodes);
        startNodes.forEach(n -> currentNodes.put(n.getId(), n));

        for (int i = 0; i < s.length(); i++) {
            if (!proceed(currentNodes, s.charAt(i))) {
                return false;
            }
        }

        return currentNodes.values().stream().anyMatch(Node::isEnd);
    }

    private boolean proceed(HashMap<Integer, Node> currentNodes, Character c) {
        HashMap<Integer, Node> nextNodes = new HashMap<>();

        for (Map.Entry<Integer, Node> node : currentNodes.entrySet()) {
            node.getValue().getNextNodes(c).forEach(n -> nextNodes.put(n.getId(), n));
        }

        if (nextNodes.isEmpty()) {
            return false;
        }
        currentNodes.clear();
        currentNodes.putAll(nextNodes);
        return true;
    }
}
