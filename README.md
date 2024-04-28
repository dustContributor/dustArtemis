# dustArtemis
dustArtemis is a fork of [Artemis Entity System Framework](http://gamadu.com/artemis/), as such it provides the same kind of license. Both the 2-clause BSD license pertaining to the original Artemis code and the 3-clause BSD license for dustArtemis can be found at the root of the repository.

## Overview
An ECS (Entity Component System) framework provides the basics to compose Entities from Components and Systems to iterate over Entities. Artemis is a well know Java based framework for developing videogames based on ECS structures.

dustArtemis is a fork of Artemis that provides additional functionality, bug fixes, better performance and other tweaks compared the original codebase.

**Have in mind that Java 11 (or newer) is required for dustArtemis to work.**

## How different is it?
dustArtemis, on the outside, keeps working pretty much the same as original Artemis.

You still create your World instance, add EntitySystems to it, you create Aspects for specifying which entities each system is interested on, and so on.

## Overview of the changes

### Considerations
This is a "no strings attached" fork, so no effort is made into preserving API compatibility with original Artemis. So whenever changing something makes sense, that something gets changed for great justice!

### Configuration
Check out DAConstants, it details how you can provide a cfg file for tweaking various dustArtemis constants.

### DustStep, DustContext and you
A basic dustArtemis setup may look like this:
```java
// This example step would process entities with two specific components
class StepOne extends DustStep {
  private final IntBag activeEntities;
  private final IntBag removedEntities;
  // Gets injected by the framework
  private ComponentHandler<ComponentOne> ones;
  // Called during context initialization 
  public StepOne(EntityGroups groups) {
    // This step processes entities with these two components only
    EntityFilter filter = EntityFilter.all(ComponentOne.class, ComponentTwo.class);
    // Create an entity group that contains entities with these components
    EntityGroup group = groups.matching(filter);
    // Keep around the per-tick list of entities have these components for processing
    this.activeEntities = group.active();
    // Also get the list that had these components removed since the last tick
    this.removedEntities = group.removed();
  }
  // Executed every tick
  @Override
  public void run() {
    this.activeEntites.forEach(this::doThingWithActiveEntities);
  }
  // Executed at the end of every tick, after all the steps ran
  @Override
  public void cleanup() {
    this.removedEntities.forEach(this::doThingWithRemovedEntities);
  }
}
// Now we initialize the main context object with a builder
DustContext ctx = DustContext.builder()
  // Add as many steps as you need
  .step(StepOne::new) 
  .step(StepTwo::new)
   // Optional, available to every DustStep to hold configuration, constants, etc
  .data(MyDataContext.from("config/path"))
  // All possible entity component types must be specified at building time!
  .componentTypes(ComponentOne.class, ComponentTwo.class)
  .build();
// We add one entity with a component to the context
int entityId = ctx.createEntity();
// Get the related component handler to add them to the entity
ctx.getHandler(ComponentOne.class).add(entityId, new ComponentOne());
// You can keep the handler objects around if you want to avoid lookups
var twoHandler = ctx.getHandler(ComponentTwo.class);
twoHandler.add(entityId, new ComponentTwo());
// Now add the entity to the context, so it gets processed
ctx.addEntity(eid);
/* 
* Then the context processes all configured steps in the 
* specified order, adding/removing entities, notifying steps via the 
* active/removed/addd entity ID lists, etc.
*/
while (isNotClosed)
  ctx.process();
```

### Component
Component is an interface instead of an abstract class, this allows you to use normal inheritance in your Components. That being said, using too much inheritance in with the Components defeats the idea of an ECS, so be aware of that.

### Bag
Bag is kinda an iconic class of original Artemis, you can check out the sources, it has been extensively improved upon (and fixed a bit too).

For example, In the original Artemis codebase there wasn't a clear distinction between operations that were 'unsafe' (ie, didn't do any bounds check) and operations that were 'safe' to use. Now there is a distinction, so you get to choose between using operations on Bags that do checks or not.

You also get quite a few methods in Java 8 style for streams like:
```java
bag.forEach((element) -> doSomething(element));
bag.forEach(this::doSomething);
bag.parallelStream(this::doSomething);
```

Bag exposes internals too, with `data()` method you can retrieve its backing array for fast iteration. 

Bag's constructor has changed and now you need to pass a `Class` object so it can initialize its backing array with the proper type, this allows doing iterations over the backing array without casts:

```java
// You initialize bags like this
Bag<ActualType> bag = new Bag<>(ActualType.class);
// And instead of having to do this
Object[] array = bag.data();
for (int i = 0; i < bag.size(); ++i)
  doSomething( (ActualType)array[i] );
// You can do this directly:
ActualType[] array = bag.data();
for ( int i = 0; i < bag.size(); ++i )
  doSomething( array[i] );
```

### Trimmed code and tweaks
There is a lot of code in original Artemis that was either refactored into something different, or simply removed from the codebase.

Things such as ComponentMapper annotation, reduntant inner classes, ComponentType, unused utility classes, EntitySystem derivates, etc, were removed.

Reasons for this vary for each case, it was mostly to simplify the API, remove unused code, hopefully replacing implementations of some functions with something more compact or better performant (or sometimes, both!).

### As for the rest...
These are just a few of the most obvious changes, hopefully the sources are documented enough so you can just jump in and see how dustArtemis works.
