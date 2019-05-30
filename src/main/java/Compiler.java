import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Compiler {

    public Matcher compile(String regex) {
        StringLookAhead iterator = new StringLookAhead(regex);
        NFA nfa = generateNfa(compile(iterator, new Stack<>()));
        return new Matcher(nfa);
    }

    private NFA generateNfa(Fragment fragment) {
        Node end = new Node();
        end.markAsEnd();

        fragment.getDangling().forEach(dangling -> {
            dangling.setDestination(end);
        });

        return new NFA(fragment.getStart(),
                fragment.getDangling().stream().map(Transition::getStart).collect(Collectors.toList()));
    }

    private Fragment compile(StringLookAhead iterator, Stack<Fragment> fragments) {
        while (iterator.hasMore()) {
            if (isQuantifier(iterator.current())) {
                compileQuantifier(iterator, fragments);
            } else if (iterator.current() == '|') {
                compileOr(iterator, fragments);
            } else if (iterator.current() == '(') {
                fragments.push(compileGroup(iterator));
            } else if (iterator.current() == ')') {
                break;
            } else {
                readConcat(iterator, fragments);
                pushConcat(iterator, fragments);
            }
        }

        return fragments.pop();
    }

    private boolean isQuantifier(Character character) {
        return character == '*' || character == '+' || character == '?';
    }

    private Fragment compileGroup(StringLookAhead iterator) {
        iterator.eat('(');
        Fragment fragment = compile(iterator, new Stack<>());
        iterator.eat(')');

        return fragment;
    }

    private void compileQuantifier(StringLookAhead iterator, Stack<Fragment> fragments) {
        Fragment fragment = fragments.pop();

        Node n = new Node();
        Transition nextDangling = new Transition(Transition.EMPTY_TRANSITION, null);
        n.addTransition(nextDangling);
        n.addTransition(new Transition(Transition.EMPTY_TRANSITION, fragment.getStart()));

        if (iterator.current() != '?') {
            fragment.getDangling().forEach(dangling -> dangling.setDestination(n));
        }

        if (iterator.current() == '*') {
            fragments.push(new Fragment(n, Collections.singletonList(nextDangling)));
        } else if (iterator.current() == '+') {
            fragments.push(new Fragment(fragment.getStart(), Collections.singletonList(nextDangling)));
        } else if (iterator.current() == '?') {
            List<Transition> danglings = new ArrayList<>();
            danglings.add(nextDangling);
            danglings.addAll(fragment.getDangling());
            fragments.push(new Fragment(n, danglings));
        }

        iterator.proceedPosition();
    }

    private void compileOr(StringLookAhead iterator, Stack<Fragment> fragments) {
        iterator.eat('|');

        if (iterator.current() == '(') {
            fragments.push(compileGroup(iterator));
            if (isQuantifier(iterator.current())) {
                compileQuantifier(iterator, fragments);
            }
        } else {
            fragments.push(compile(iterator, new Stack<>()));
        }

        pushOr(iterator, fragments);
    }

    private void pushOr(StringLookAhead iterator, Stack<Fragment> fragments) {
        Fragment right = fragments.pop();
        Fragment left = fragments.pop();

        Node split = new Node();
        split.addTransition(new Transition(Transition.EMPTY_TRANSITION, left.getStart()));
        split.addTransition(new Transition(Transition.EMPTY_TRANSITION, right.getStart()));

        List<Transition> nextDanglings = new ArrayList<>(left.getDangling());
        nextDanglings.addAll(right.getDangling());

        fragments.push(new Fragment(split, nextDanglings));
    }

    private void readConcat(StringLookAhead iterator, Stack<Fragment> fragments) {
        removeEscaping(iterator);

        Node start = new Node();
        Character transitionChar = iterator.current();
        Transition transition = new Transition(compileMatcher(transitionChar), null);
        start.addTransition(transition);

        Fragment fragment = new Fragment(start, Collections.singletonList(transition));
        fragments.push(fragment);

        iterator.proceedPosition();

        if (iterator.hasMore()) {
            if (isQuantifier(iterator.current())) {
                compileQuantifier(iterator, fragments);
            }
        }
    }

    private void removeEscaping(StringLookAhead iterator) {
        if (iterator.current() == '\\') {
            iterator.proceedPosition();
        }
    }

    private Predicate<Character> compileMatcher(Character character) {
        if (character == '.') {
            // TODO check correct semantics
            return (c) -> !c.equals('\n');
        } else {
            return (c) -> c.equals(character);
        }
    }

    private void pushConcat(StringLookAhead iterator, Stack<Fragment> fragments) {
        Fragment second = fragments.pop();
        Fragment first = fragments.isEmpty() ? null : fragments.pop();

        if (first == null) {
            first = new Fragment(second.getStart(), second.getDangling());
        } else {
            first.getDangling().forEach(dangling -> {
                dangling.setDestination(second.getStart());
            });
            first.setDangling(second.getDangling());
        }

        fragments.push(first);
    }
}
