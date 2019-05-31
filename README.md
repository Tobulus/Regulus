# jRegex- Matching regular expression in linear time

Implements the basic ideas described by [Russ Cox](https://swtch.com/~rsc/regexp/regexp1.html) which basically uses [Thompson's Construction](https://en.wikipedia.org/wiki/Thompson%27s_construction) to generate a non-deterministic finite automaton. The generated NFA can be used to match strings against a regex in linear time.

## Currently supported
- Quantifiers: "*", "+", "?"
- Character classes: "[abc]", "[^abc]"
- Grouping: "(abc)*"
- Alternation: "a|b"
- Wildcard: "."

## TODO
- Min/max quantifiers: "{x,y}"
- Predefined character classes: "\s", "\d", ...
- Group extraction
- ...
