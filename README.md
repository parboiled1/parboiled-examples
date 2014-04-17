## How to build

**You must have a JDK version 6 or 7 for this package to work**. Make sure that
you use the same version to build all packages (including this one).

You will need to fork both:

* [parboiled-core](https://github.com/parboiled1/parboiled-core);
* [parboiled-java](https://github.com/parboiled1/parboiled-java).

Build them in this order, using:

```
# Unix
./gradlew clean test install
# Windows
gradlew.bat clean test install
```

Then you can build this package.

