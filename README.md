# DiffTool

## Overview
The purpose of DiffTool is to provide a means of comparing two records and returning a list of differences between them. 
To this end, DiffTool provides a single function, `diff`, which takes two records and returns any changes.

## Usage
Given the following two records:

```kotlin
    val previous = Thing("Steve",
        Bar("This is a Bar", 23,
            Foo(
                listOf("red", "green", "blue"),
            )
        )
    )
    val current = Thing("Bob",
        Bar("This is a Baz", 23,
            Foo(
                listOf("purple", "cyan", "green", "blue"),
            )
        )
    )
```

DiffTool would recognize the following changes:

```kotlin
    {"property": "name", "previous": "Steve", "current": "Bob"}
    {"property": "bar.description", "previous": "This is a Bar", "current": "This is a Baz"}
    {"property": "bar.foo.colors", "added": [purple, cyan], "removed": [red]}
```

## Code Structure
DiffTool has but a single public method `diff`, but it is backed by a number of private methods that do the heavy lifting. 
The `diff` method itself is just a wrapper around a recursive `recDiff`, which in addition to tracking the previous and
current objects also tracks the path information.

The way this method works is by basically walking the object graph and comparing the values of each property. If the fields
are primitives, then the values are added to the result immediately as `PropertyUpdate`s. If the fields are complex types, 
the method updates its path info and recursively walks any subtypes.

One of the complex types it may encounter is a list. If it does, it will compare the lists and return a `ListUpdate` 
object of anything that may have been added or removed. If the list contains complex types, it will recursively walk
through these, ensuring each have a valid `id` property, and then recursively walking their respective properties and
generate either `PropertyUpdate`s or `ListUpdate`s as needed.

## Testing
When I was told that I could take a little extra time to finish up DiffTool's functionality, one of the first things I
decided to concentrate on was writing a comprehensive test suite. I approached this using the BDD tool Cucumber, and the
results can be found in the HTML file `cucumber-report.html`. The number of examples and scenarios were extensive and I
think they capture the acceptance criteria that I was originally provided.

## Challenges
The entire project definitely took me out of my comfort zone as I haven't done anything with reflection in over a decade.
I did get hung up during the first day with Kotlin and its `reified` generics, for I could neither perform recursion nor
declare inner functions. I ended up turning to Java instead and by the end of the first day was making substantial 
progress.

On the second day, I was able to get enough of the Java version running that I felt it was time to see if it could be
done in Kotlin. I originally tried to keep the code within a single method, as this is what I thought the requirements 
were asking, but in the end I decided to break it up into smaller methods to make it easier to read and understand. 
However, by the end of the second day, I did not have the `ListUpdate` functionality fully ironed out.

On Friday I was asked to finished out the remaining functionality, and so I did. I also took the time to clean up the
code and add tests.

In all, between my false on the first day, my work on the second, and the time I took to create tests, I would say that I 
ended up investing ~20 hours into this.

## Parting Thoughts
Though this was not necessarily in my wheelhouse, I did enjoy the challenge. One of the things that I love to do as a 
developer is to create DSLs using parser combinators or tools like Lex and Yacc. In many ways this felt akin to that type
of work, and I think that a good refinement of DiffTool might involve a similar approach.