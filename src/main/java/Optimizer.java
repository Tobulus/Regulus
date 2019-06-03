public class Optimizer {

    public String prepare(String regex) {
        StringBuilder preparedRegex = new StringBuilder();
        StringLookAhead iterator = new StringLookAhead(regex);

        prepare(preparedRegex, iterator);
        return preparedRegex.toString();
    }

    private void prepare(StringBuilder preparedRegex, StringLookAhead iterator) {
        StringBuilder buffer = new StringBuilder();

        while (iterator.hasMore()) {
            if (iterator.current() == '(' || iterator.current() == '[') {
                preparedRegex.append(collectGroup(iterator));
            } else if (iterator.current() == ')' || iterator.current() == ']') {
                break;
            } else if (iterator.current() == '\\') {
                buffer.append(iterator.current());
                iterator.proceedPosition();
                buffer.append(iterator.current());
                iterator.proceedPosition();
                if (iterator.hasMore() && iterator.current() == '{') {
                    preparedRegex.append(parseMinMax(buffer, iterator));
                } else {
                    preparedRegex.append(buffer);
                }
            } else if (iterator.current() == '{') {
                preparedRegex.append(parseMinMax(buffer, iterator));
            } else {
                buffer.append(iterator.current());
                iterator.proceedPosition();
                if (iterator.hasMore() && iterator.current() == '{') {
                    preparedRegex.append(parseMinMax(buffer, iterator));
                } else {
                    preparedRegex.append(buffer);
                }
            }
            buffer.setLength(0);
        }
    }

    private StringBuilder collectGroup(StringLookAhead iterator) {
        Character limiter = iterator.current() == '(' ? ')' : ']';
        StringBuilder group = new StringBuilder();

        group.append(iterator.current());
        iterator.proceedPosition();

        prepare(group, iterator);

        group.append(limiter);
        iterator.eat(limiter);

        if (iterator.hasMore() && iterator.current() == '{') {
            group = parseMinMax(group, iterator);
        }

        return group;
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
}
