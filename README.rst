===========
dustArtemis
===========

dustArtemis is a fork of `Artemis Entity System Framework <http://gamadu.com/artemis/>`_ As such it is licenced under the 2-clause BSD licence found in com/artemis/LICENCE.

You can check out the `GDNet dustArtemis dev blog <http://www.gamedev.net/blog/1871-dustartemis-ecs-framework/>`_.

Overview
========

An ECS (Entity Component System) framework provides the basics to compose Entities from Components and Systems to iterate over Entities. Artemis is a well know Java based framework for developing videogames based on ECS structures.

dustArtemis is a fork of Artemis that provides additional functionality, bug fixes, better performance and other tweaks compared the original codebase.

**Have in mind that Java 8 (or newer) is required for dustArtemis to work.** Having said that, Java 8 features are used sporadically in dustArtemis, so it's not too complex to just set the target to Java 7 and fix any compile error that pops up.

How different is it?
=======

dustArtemis, on the outside, keeps working pretty much the same as original Artemis.

You still create your World instance, add EntitySystems to it, you create Aspects for specifying which entities each system is interested on, and so on.

Overview of the changes
=======

Considerations
-----------

This is a "no strings attached" fork, so no effort is made into preserving API compatibility with original Artemis. So whenever changing something makes sense, that something gets changed for great justice!

Configuration
--------------------

Check out DAConstants, it details how you can provide a cfg file for tweaking various dustArtemis constants.

Component
--------------------

Component is an interface instead of an abstract class, this allows you to use normal inheritance in your Components. That being said, using too much inheritance in with the Components defeats the idea of an ECS, so be aware of that.

Bag
--------------------

Bag is kinda an iconic class of original Artemis, you can check out the sources, it has been extensively improved upon (and fixed a bit too).

For example, In the original Artemis codebase there wasn't a clear distinction between operations that were 'unsafe' (ie, didn't do any bounds check) and operations that were 'safe' to use. Now there is a distinction, so you get to choose between using operations on Bags that do checks or not.

You also get quite a few methods in Java 8 style like:
    bag.forEach( (element) -> doSomething(element) );
Or the more idiomatic:
     bag.forEach( this::doSomething );
Also with shiny new parallel iteration like this:
    bag.parallelStream( this::doSomething );

Bag exposes internals too, with data() method you can retrieve its backing array for fast iteration. 

Bag's constructor has changed and now you need to pass a Class object so it can initialize its backing array with the proper type, this allows doing iterations over the backing array without ugly casts:

You initialize Bags like this:
    Bag<ActualType> bag = new Bag<>(ActualType.class);

And instead of having to do this

    Object[] array = bag.data();
    
    for ( int i = 0; i < bag.size(); ++i )
        doSomething( (ActualType)array[i] );
    
You can do this directly:

     ActualType[] array = bag.data();
     
     for ( int i = 0; i < bag.size(); ++i )
         doSomething( array[i] );

Trimmed stuff
-------------

There is a lot of code in original Artemis that was either refactored into something different, or simply removed from the codebase.

Things such as ComponentMapper annotation, reduntant inner classes, ComponentType, unused utility classes, EntitySystem derivates, etc, were removed.

Reasons for this vary for each case, it was mostly to simplify the API, remove unused code, hopefully replacing implementations of some functions with something more compact or better performant (or sometimes, both!). See ClassIndexer for example, it replaces a few classes original Artemis had.

Tweaks
------

There are various tweaks through all dustArtemis, everything from trimmed code, refactored methods, simplification of various functions, etc.

As for the rest...
========

These are just a few of the most obvious changes, hopefully the sources are documented enough so you can just jump in and see how dustArtemis works.
