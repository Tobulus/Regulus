import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

public class Compiler {
    public Matcher compile(String regex) {
        StringLookAhead iterator = new StringLookAhead(new Optimizer().prepare(regex));
        Node nfa = generateNfa(compile(iterator, new Stack<>()));
        return new Matcher(nfa);
    }

    private Node generateNfa(Fragment fragment) {
        Node end = new Node();
        end.markAsEnd();

        fragment.getDangling().forEach(dangling -> dangling.setDestination(end));

        return fragment.getStart();
    }

    private Fragment compile(StringLookAhead iterator, Stack<Fragment> fragments) {
        while (iterator.hasMore()) {
            if (isQuantifier(iterator.current())) {
                compileQuantifier(iterator, fragments);
            } else if (iterator.current() == '|') {
                compileOr(iterator, fragments);
            } else if (iterator.current() == '(') {
                fragments.push(compileGroup(iterator));
                if (iterator.hasMore()) {
                    if (isQuantifier(iterator.current())) {
                        compileQuantifier(iterator, fragments);
                    }
                }
                pushConcat(fragments);
            } else if (iterator.current() == ')') {
                break;
            } else if (iterator.current() == '[') {
                compileCharacterClass(iterator, fragments);
                pushConcat(fragments);
            } else {
                readConcat(iterator, fragments);
                pushConcat(fragments);
            }
        }

        return fragments.pop();
    }

    private void compileCharacterClass(StringLookAhead iterator, Stack<Fragment> fragments) {
        iterator.eat('[');

        boolean negate = false;
        if (iterator.current() == '^') {
            negate = true;
            iterator.proceedPosition();
        }

        Predicate<Character> matcher = null;
        while (iterator.hasMore() && iterator.current() != ']') {
            if (iterator.current() == '\\' && iterator.hasNext() && MetaCharacters.exists(iterator.next())) {
                iterator.proceedPosition();
                matcher = or(matcher, MetaCharacters.getHandler(iterator.current()));
            } else if (iterator.hasNext() && iterator.next() == '-' && iterator.hasNextNext() && iterator.nextNext() != ']') {
                matcher = parseRange(iterator);
            } else {
                removeEscaping(iterator);

                Character transitionChar = iterator.current();
                matcher = or(matcher, (character) -> character.equals(transitionChar));
            }
            iterator.proceedPosition();
        }

        if (negate) {
            matcher = matcher.negate();
        }

        Node start = new Node();
        Transition transition = new Transition(matcher, null);
        start.addTransition(transition);

        Fragment fragment = new Fragment(start, Collections.singletonList(transition));
        fragments.push(fragment);

        iterator.eat(']');

        if (iterator.hasMore()) {
            if (isQuantifier(iterator.current())) {
                compileQuantifier(iterator, fragments);
            }
        }
    }

    private Predicate<Character> parseRange(StringLookAhead iterator) {
        Character from = iterator.current();
        iterator.proceedPosition();
        iterator.eat('-');
        Character to = iterator.current();

        return (character) -> character >= from && character <= to;
    }

    private Predicate<Character> or(Predicate<Character> current, Predicate<Character> next) {
        if (current == null) {
            return next;
        } else {
            return current.or(next);
        }
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

        pushOr(fragments);
    }

    private void pushOr(Stack<Fragment> fragments) {
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
        if (iterator.current() == '\\' && iterator.hasNext() && MetaCharacters.exists(iterator.next())) {
            iterator.proceedPosition();

            Node start = new Node();
            Transition transition = new Transition(MetaCharacters.getHandler(iterator.current()), null);
            start.addTransition(transition);

            Fragment fragment = new Fragment(start, Collections.singletonList(transition));
            fragments.push(fragment);
        } else {
            boolean escaped = removeEscaping(iterator);

            Node start = new Node();
            Character transitionChar = iterator.current();
            Transition transition = new Transition(compileMatcher(transitionChar, escaped), null);
            start.addTransition(transition);

            Fragment fragment = new Fragment(start, Collections.singletonList(transition));
            fragments.push(fragment);
        }

        iterator.proceedPosition();

        if (iterator.hasMore()) {
            if (isQuantifier(iterator.current())) {
                compileQuantifier(iterator, fragments);
            }
        }
    }

    private boolean removeEscaping(StringLookAhead iterator) {
        if (iterator.current() == '\\') {
            iterator.proceedPosition();
            return true;
        }
        return false;
    }

    private Predicate<Character> compileMatcher(Character character, boolean escaped) {
        if (character == '.' && !escaped) {
            // TODO check correct semantics
            return (c) -> !c.equals('\n');
        } else {
            return (c) -> c.equals(character);
        }
    }

    private void pushConcat(Stack<Fragment> fragments) {
        Fragment second = fragments.pop();
        Fragment first = fragments.isEmpty() ? null : fragments.pop();

        if (first == null) {
            first = new Fragment(second.getStart(), second.getDangling());
        } else {
            first.getDangling().forEach(dangling -> dangling.setDestination(second.getStart()));
            first.setDangling(second.getDangling());
        }

        fragments.push(first);
    }
}
