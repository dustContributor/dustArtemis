===========
dustArtemis
===========

dustArtemis is a fork of `Artemis Entity System Framework <http://gamadu.com/artemis/>`_ As such it is licenced under the 2-clause BSD licence found in src/licence.txt.

Overview
========

An ECS (Entity Component System) framework provides the basics to compose Entities from Components and Systems to iterate over Entities. Artemis is a well know Java based framework for developing videogames based on ECS structures.

dustArtemis is a fork of Artemis that while doesn't provides ground breaking changes, it does irons out a few things on the original Artemis codebase.

**Have in mind that Java 8 (or newer) is required for dustArtemis to work.**

Changes
=======

Bug Fixes
---------

- Bag.set(index) doesn't messes up the bag anymore.
- Fixed IndexOutOfBounds exception inside ComponentManager, if you requested a Component that an Entity didn't had, and the Component index was high enough, it would go over the Bag's capacity and throw an exception.
- ComponentMappers weren't being initialized if they were declared in a super class of the EntitySystem. So if you had a hierarchy of EntitySystems, the only ComponentMappers initialized were the ones declared in the downmost class of the hierarchy.
- ComponentMapper field access restrictions weren't being restored after initialization, it was being kept "open" for reflection forever.
- Probably a few more that I forgot about.

Bag and ImmutableBag
--------------------

In the original Artemis codebase there wasn't a clear distinction between operations that were 'unsafe' (ie, didn't do any bounds check) and operations that were 'safe' to use. There is a distinction now between 'safe' methods and 'unsafe' methods now so you get to choose between using operations on Bags that do checks or not.

You also get quite a few methods in Java 8 style like:
    $bag.forEach( (element) -> doSomething(element) );
Or use shiny new parallel iteration like:
    $bag.parallelStream( (element) -> doSomethingInParallel(element) );

(you can check ParallelEntityProcessingSystem for a dead simple implementation of a parallel EntitySystem)

Bag exposes internals too, with data() method you can retrieve its backing array for fast iteration. Bag's constructor has changed and now you need to pass a Class object so it can initialize its backing array with the proper type, this allows doing iterations over the backing array without ugly casts:

You initialize Bags like this:
    $ Bag<ActualType> bag = new Bag<>(ActualType.class);

And instead of having to do this:
    $ Object[] array = bag.data();
    $ for ( int i = 0; i < bag.size(); ++i )
    $ {
    $    doSomething( (ActualType)array[i] );
    $ }
You can do this directly:
    $ ActualType[] array = bag.data();
    $ for ( int i = 0; i < bag.size(); ++i )
    $ {
    $    doSomething( array[i] );
    $ }

So its a tradeoff between uglier initialization for cleaner iteration (you'll be doing more iteration than initialization).

Bag also follows predictable growth implementing a simple grow strategy. If the number of elements is below Bag.GROW_RATE_THRESHOLD, the growth will be the double of the capacity. After that theresold, it will be (capacity + halfCapacity), its implemented that way so the backing arrays are more "tame" after a few thousand entities are added.

There are quite a few tweaks ( bag.clear() now uses the fastest way I know of for filling an array of nulls) and bug fixes too (Artemis bag.set() was broken for example).

New classes
-----------

For assigning Entity IDs, EntityManager was using its own internal identity pool, now its implemented in a more generalized way with **IdPool** class, which uses the new **IntStack** for managing integer IDs.

**IntStack** follows the same principles of predictable growth and 'safe'/'unsafe' methods like Bag.

There is a new **ClassIndexer** which is a (as far as I could make it) thread safe implementation of the sort of type-subtype indexing Artemis did with EntitySystems and Components.

Now instead of having their own ways to set up EntitySystem and Component indices (used in various BitSets around the framework), there is a single (more or less) generic way to request an index given a type and a super type to index from.

**MapperImplementor** is a new class with a static method for initializing **ComponentMappers**. I found that looking for annotations was noticeably slower than for fields, so now MapperImplementor initializes ComponentMappers by their type, it also has a few optimizations given the new way to access Bag's backing array.

Trimmed stuff
-------------

Since MapperImplementor uses field types instead of annotations to initialize ComponentMappers, the @ComponentMapper annotation is gone.

Given the new ClassIndexer, now EntityManagers don't have a specialized way to deal with Entity IDs, they just do it through an IdPool instance.

ComponentType is gone too, its main purpose was to have an index for each Component subclass, that function is now made through ClassIndexer.

Classes that didn't had much to do with the ECS framework like TrigLUT, FastMath or Utils were removed. You're better off looking somewhere else for a specialized math library (like LibGDX's) or doing your own.

DelayedEntitySystem was removed since it was broken, Timer was removed too since its only use was inside DelayedEntitySystem.

Static factory-like methods in Aspect were removed since they didn't provided any additional advantage over initializing an Aspect directly. There is a getEmpty() method that always returns the same empty aspect so you can use in your VoidEntitySystems.

Tweaks
------

Entity was using UUID instances for assigning unique IDs to entities. Those aren't the most cheap objects to initialize precisely, and were overkill for any sort of reasonable (and some unreasonable) usage. 

Now Entities get assigned an unique int ID sequentially from an AtomicInteger (which also means that ID generation is thread safe). So you get 4294967296 (aka 2^32) possible unique IDs. And if you're dealing with more than 4 billion entities at a time, use a database, I doubt this framework would even be useful for such volume of entities.

Direct array iteration over Bag contents are used whenever possible. 

Also direct field access is made whenever possible, there were plenty of fields that had getter/setters with the same visibility, and that did no additional work at all, so those were removed in favor of direct field access with proper visibility (mostly 'protected' was used).

Reworked EntitySystem check() method so it returns as soon as possible, that method is run for every Entity changed in every EntitySystem.

Reworked the 'notify' methods in World so they iterate in a different manner, hopefully making many, many less method calls.