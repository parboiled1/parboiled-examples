## How to build

**You must have a JDK version 6 for this package to work**.

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

