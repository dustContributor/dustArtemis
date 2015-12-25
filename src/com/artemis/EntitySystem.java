package com.artemis;

import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;
import com.artemis.utils.MutableBitIterator;

/**
 * The most raw entity system. It should not typically be used, but you can
 * create your own entity system handling by extending this. It is recommended
 * that you use the other provided entity system implementations.
 * 
 * @author Arni Arent
 * @author dustContributor
 * 
 */
public abstract class EntitySystem extends EntityObserver
{
	private final Aspect aspect;

	private final IntBag actives;
	private final IntBag inserted;
	private final IntBag removed;

	private final OpenBitSet activeBits;
	private final MutableBitIterator bitIterator;

	/**
	 * Creates an entity system that builds an {@link Aspect} instance using the
	 * passed {@link Aspect.Builder}, and uses that Aspect as a matcher against
	 * entities.
	 * 
	 * @param builder to create an {@link Aspect} to match against entities.
	 */
	public EntitySystem ( final Aspect.Builder builder )
	{
		this( builder.build() );
	}

	/**
	 * Creates an entity system that uses the specified aspect as a matcher
	 * against entities.
	 * 
	 * @param aspect to match against entities.
	 */
	public EntitySystem ( final Aspect aspect )
	{
		if ( aspect == null || aspect.isEmpty() )
		{
			final String cause = (aspect != null) ? "empty" : "null";
			throw new DustException( this,
					"Cant pass an Aspect that is " + cause + '!' +
							" Extend EntityObserver if you want an observer" +
							" that doesn't processes any entity!" );
		}
		// Fetch entity amount per system.
		int actSize = DAConstants.APPROX_ENTITIES_PER_SYSTEM;
		this.aspect = aspect;
		this.actives = new IntBag( actSize );
		this.inserted = new IntBag( actSize / 2 );
		this.removed = new IntBag( actSize / 2 );
		this.activeBits = new OpenBitSet( actSize );
		this.bitIterator = new MutableBitIterator();
		// Inactive by default.
		this.setActive( false );
	}

	/**
	 * Called before processing of entities begins. Always called.
	 */
	protected void begin ()
	{
		// Empty method.
	}

	@Override
	public final void process ()
	{
		// Always called.
		begin();
		// If there is any entity to process.
		if ( !actives.isEmpty() )
		{
			processEntities( actives );
		}
		// Also always called.
		end();
	}

	/**
	 * Called after the processing of entities ends. Always called.
	 */
	protected void end ()
	{
		// Empty method.
	}

	/**
	 * Any implementing entity system must implement this method and the logic to
	 * process the given entities of the system. Called only if there are entities
	 * to process.
	 * 
	 * @param entities the entities this system contains.
	 */
	protected abstract void processEntities ( final ImmutableIntBag entities );

	/**
	 * Called only if the system received matching entities, e.g. created or a
	 * component was added to it.
	 * 
	 * @param entities that were inserted into this system.
	 */
	protected void inserted ( final ImmutableIntBag entities )
	{
		// Empty method.
	}

	/**
	 * Called only if the system got any entity removed from itself, e.g. entity
	 * deleted or had one of it's components removed.
	 * 
	 * @param entities that were removed from this system.
	 */
	protected void removed ( final ImmutableIntBag entities )
	{
		// Empty method.
	}

	@Override
	public final void added ( final ImmutableIntBag entities )
	{
		addAll( entities );
	}

	@Override
	public final void changed ( final ImmutableIntBag entities )
	{
		checkAll( entities );
	}

	@Override
	public final void deleted ( final ImmutableIntBag entities )
	{
		removeAll( entities );
	}

	@Override
	public final void disabled ( final ImmutableIntBag entities )
	{
		removeAll( entities );
	}

	@Override
	public final void enabled ( final ImmutableIntBag entities )
	{
		addAll( entities );
	}

	@Override
	final void processModifiedEntities ()
	{
		// Now start checking of something actually changed.
		final int minIdRemoved = processIfModified( removed, this::removed );
		final int minIdInserted = processIfModified( inserted, this::inserted );
		// Compute real min modified entity id.
		final int minId = Math.min( minIdRemoved, minIdInserted );
		// Using max value as flag for no changes.
		if ( minId < Integer.MAX_VALUE )
		{
			// And rebuild this system's entity list.
			rebuildEntityList( minId );
		}
	}

	/**
	 * If there is something in the passed entities bag, it will call the
	 * operation on the bag, clear it, then return the minimum affected entity id
	 * found.
	 * 
	 * @param entities to process.
	 * @param operation to make on the entities.
	 * @return minimum affected entity id, or {@link Integer#MAX_VALUE} if there
	 *         was nothing in the bag.
	 */
	private static final int processIfModified (
			final IntBag entities,
			final Consumer<IntBag> operation )
	{
		// Using max value as flag for no changes.
		int minId = Integer.MAX_VALUE;

		if ( entities.isEmpty() )
		{
			// Nothing to compute.
			return minId;
		}

		final int[] data = entities.data();
		// Check changes in removed entities.
		for ( int i = entities.size(); i-- > 0; )
		{
			minId = Math.min( minId, data[i] );
		}
		// Let the system process the events.
		operation.accept( entities );
		// Clear the container.
		entities.setSize( 0 );
		// Return min entity id that was modified.
		return minId;
	}

	private final void rebuildEntityList ( final int startId )
	{
		final int newSize = activeBits.cardinality();
		final int oldSize = actives.size();
		actives.setSize( newSize );
		actives.ensureCapacity( newSize );

		final int[] ids = actives.data();

		final MutableBitIterator mbi = bitIterator;
		mbi.setBits( activeBits.getBits() );
		mbi.startingFrom( startId );

		int j = Arrays.binarySearch( ids, 0, oldSize, startId );
		// Fix index if Entity ID isn't on the list yet.
		j = Math.max( j, -j - 1 );

		/*
		 * From the found position, rebuild the entity ID list.
		 * 
		 * NOTE: It seems explicitly checking for j < ids.length helps the JIT a
		 * bit, j wont ever be bigger than ids.length, but probably the JIT can't
		 * infer that and checks every loop if it has to raise an out of bounds
		 * exception.
		 */
		for ( int i = mbi.nextSetBit(); i >= 0 && j < ids.length; i = mbi.nextSetBit(), ++j )
		{
			ids[j] = i;
		}
	}

	private final void removeAll ( final ImmutableIntBag entities )
	{
		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;
		final IntBag removs = removed;
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];

			if ( acBits.get( eid ) )
			{
				removs.add( eid );
				acBits.fastClear( eid );
			}
		}
	}

	private final void addAll ( final ImmutableIntBag entities )
	{
		final Aspect asp = aspect;
		final OpenBitSet acBits = activeBits;
		final IntBag insrts = inserted;
		final FixedBitSet[] cmpBits = world.componentManager().componentBits();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];

			if ( asp.isInteresting( cmpBits[eid] ) )
			{
				insrts.add( eid );
				acBits.set( eid );
			}
		}
	}

	/**
	 * Adds entity if the system is interested in it and hasn't been added before.
	 * 
	 * Removes entity from system if its not interesting and it has been added
	 * before.
	 * 
	 * @param entities to check.
	 */
	private final void checkAll ( final ImmutableIntBag entities )
	{
		final Aspect asp = aspect;
		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;
		final IntBag insrts = inserted;
		final IntBag removs = removed;
		final FixedBitSet[] cmpBits = world.componentManager().componentBits();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];
			// Second bit for 'contains'.
			int flags = acBits.getBit( eid ) << 1;
			// First bit for 'interesting'.
			flags |= asp.isInteresting( cmpBits[eid] ) ? 0b1 : 0b0;

			switch ( flags )
			{
				case 0b01:
				{
					// Interesting and system doesn't contains. Insert.
					insrts.add( eid );
					acBits.set( eid );
					continue;
				}
				case 0b10:
				{
					// Not interesting and system does contains. Remove.
					removs.add( eid );
					acBits.fastClear( eid );
					continue;
				}
				default:
					// Otherwise do nothing.
					continue;
			}
		}
	}

	public final ImmutableIntBag actives ()
	{
		return actives;
	}

}
