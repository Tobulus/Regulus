public class Optimizer {
    public String prepare(String regex) {
        StringBuilder preparedRegex = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        StringLookAhead iterator = new StringLookAhead(regex);

        while (iterator.hasMore()) {
            if (iterator.current() == '(' || iterator.current() == '[') {
                appendAndClearBuffer(preparedRegex, buffer);
                buffer.append(collectGroup(iterator));
            } else if (iterator.current() == '\\') {
                appendAndClearBuffer(preparedRegex, buffer);
                buffer.append(iterator.current());
                iterator.proceedPosition();
                buffer.append(iterator.current());
                iterator.proceedPosition();
            } else if (iterator.current() == '{') {
                preparedRegex.append(parseMinMax(buffer, iterator));
                buffer.setLength(0);
            } else {
                appendAndClearBuffer(preparedRegex, buffer);
                buffer.append(iterator.current());
                iterator.proceedPosition();
            }
        }

        preparedRegex.append(buffer);

        return preparedRegex.toString();
    }

    private StringBuilder parseMinMax(StringBuilder buffer, StringLookAhead iterator) {
        StringBuilder result = new StringBuilder();

        iterator.eat('{');
        int min = iterator.currentAsInt();
        iterator.proceedPosition();
        int max = min;

        iterator.skipWhitespace();

        if (iterator.current() == ',') {
            iterator.proceedPosition();
            iterator.skipWhitespace();
            max = iterator.current() == '}' ? Integer.MAX_VALUE : iterator.currentAsInt();
            if (iterator.current() != '}') {
                iterator.proceedPosition();
            }
        }

        for (int i = 0; i < min; i++) {
            result.append(buffer);
        }

        if (max == Integer.MAX_VALUE) {
            result.append(buffer).append("*");
        } else {
            for (int i = 0; i < max - min; i++) {
                result.append(buffer).append("?");
            }
        }

        iterator.eat('}');

        return result;
    }

    private StringBuilder collectGroup(StringLookAhead iterator) {
        StringBuilder group = new StringBuilder();
        Character delimiter = iterator.current() == '(' ? ')' : ']';

        while (iterator.hasMore() && iterator.current() != delimiter) {
            group.append(iterator.current());
            iterator.proceedPosition();
        }
        if (iterator.current() == delimiter) {
            group.append(iterator.current());
            iterator.proceedPosition();
        }

        return group;
    }

    private void appendAndClearBuffer(StringBuilder preparedRegex, StringBuilder buffer) {
        preparedRegex.append(buffer);
        buffer.setLength(0);
    }
}
