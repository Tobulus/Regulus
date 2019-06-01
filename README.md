# jRegex- Matching regular expressions in linear time

Implements the basic ideas described by [Russ Cox](https://swtch.com/~rsc/regexp/regexp1.html) which basically uses [Thompson's Construction](https://en.wikipedia.org/wiki/Thompson%27s_construction) to generate a non-deterministic finite automaton. The generated NFA can be used to match strings against a regex in linear time.

This is mainly for educational purposes. If you are searching for a production ready regex engine which doesn't use backtracking, check out [google's re2](https://github.com/google/re2j).

## Currently supported
- Quantifiers: "*", "+", "?", "{3,4}"
- Character classes: "[abc]", "[^abc]"
- Grouping: "(abc)*"
- Alternation: "a|b"
- Wildcard: "."

## TODO
- Predefined character classes: "\s", "\d", ...
- Group extraction
- ...
