class StringLookAhead {
    private String string;
    private int currentIndex = 0;

    public StringLookAhead(String string) {
        this.string = string;
    }

    public Character current() {
        return string.charAt(currentIndex);
    }

    public Character next() {
        return string.charAt(currentIndex + 1);
    }

    public Character nextNext() {
        return string.charAt(currentIndex + 2);
    }

    public int currentAsInt() throws CompileException {
        try {
            return Integer.parseInt(String.valueOf(current()));
        } catch (NumberFormatException e) {
            throw new CompileException(String.format("Cannot parse value '%s' to int at position '%s'", current(), currentPosition()));
        }
    }

    public void eat(Character character) {
        if (!character.equals(current())) {
            throw new CompileException(String.format("Excepted '%s' but found '%s' at position %s", character, current(), currentPosition()));
        }
        proceedPosition();
    }

    public void skipWhitespace() {
        while (current() == ' ' && hasNext()) {
            proceedPosition();
        }
    }

    public boolean hasNext() {
        return string.length() > currentIndex + 1;
    }

    public boolean hasNextNext() {
        return string.length() > currentIndex + 2;
    }

    public boolean hasMore() {
        return currentIndex < string.length();
    }

    public void proceedPosition() {
        currentIndex++;
    }

    public int currentPosition() {
        return currentIndex;
    }
}
