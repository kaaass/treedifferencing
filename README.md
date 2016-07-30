This is a fork of [Gumtree](https://github.com/GumTreeDiff/gumtree).

It contains the MTDIFF implementation
and several general optimizations for tree differencing algorithms.
The data to the ASE 2016 publication "Move-Optimized Source Code Tree Differencing" is available
in a separate [repository](https://github.com/FAU-Inf2/tree-measurements).

## Structure

This repository is structured as follows:
- `src/main/java/com/github/gumtreediff/matchers/optimizations`:
Contains the general optimizations for tree differencing algorithms.
- `src/main/java/com/github/gumtreediff/matchers/heuristic/mtdiff`:
Contains the new MTDIFF implementation.

## Example

You can build the code with the following commands: 

```
https://github.com/FAU-Inf2/treedifferencing.git
cd treedifferencing
./gradlew build
```
You will have a `tar.gz` distribution of GumTree in the `dist/build/distributions` folder. Unzip the archive.

To run the example from the publication with MTDIFF use:
```
./bin/dist webmtdiff Examples/Example1Original.java  Examples/Example1Modified.java
./bin/dist webmtdiff Examples/Example2Original.java  Examples/Example2Modified.java
```

To run the example from the publication with GumTree use:
```
./bin/dist webdiff Examples/Example1Original.java  Examples/Example1Modified.java
./bin/dist webdiff Examples/Example2Original.java  Examples/Example2Modified.java
```

## License

- The source code in `src/java/main` is licensed under LGPL version 3 (see [LICENSE](https://github.com/FAU-Inf2/gumtree/blob/develop/LICENSE)).
