package com.artemis;

import java.util.Arrays;

import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.ClassIndexer;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;
import com.artemis.utils.MutableBitIterator;

/**
 * The most raw entity system. It should not typically be used, but you can
 * create your own entity system handling by extending this. It is recommended
 * that you use the other provided entity system implementations.
 * 
 * @author Arni Arent
 * 
 */
public abstract class EntitySystem extends EntityObserver
{
	private final int index;
	private final Aspect aspect;

	private final IntBag actives;

	private final OpenBitSet activeBits;
	private final MutableBitIterator bitIterator;

	private int minModifiedId = Integer.MAX_VALUE;

	/**
	 * Creates an entity system that uses the specified aspect as a matcher
	 * against entities.
	 * 
	 * @param aspect to match against entities.
	 */
	public EntitySystem ( final Aspect aspect )
	{
		this.index = ClassIndexer.getIndexFor( this.getClass(), EntitySystem.class );
		this.bitIterator = new MutableBitIterator();

		this.aspect = aspect.isEmpty() ? null : aspect;

		// Fetch entity amount per system.
		int actSize = DAConstants.APPROX_ENTITIES_PER_SYSTEM;

		this.actives = new IntBag( actSize );
		this.activeBits = new OpenBitSet( actSize );

		this.setActive( false );
	}

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
	 * Called before processing of entities begins.
	 */
	protected void begin ()
	{
		// Empty method.
	}

	@Override
	public final void process ()
	{
		if ( minModifiedId < Integer.MAX_VALUE )
		{
			rebuildEntityList();
			// Reset min modified entity ID.
			minModifiedId = Integer.MAX_VALUE;
		}

		begin();
		processEntities( actives );
		end();
	}

	private final void rebuildEntityList ()
	{
		final int newSize = activeBits.cardinality();
		final int oldSize = actives.size();
		actives.setSize( newSize );
		actives.ensureCapacity( newSize );

		final int[] ids = actives.data();

		final MutableBitIterator mbi = bitIterator;
		mbi.setBits( activeBits.getBits() );
		mbi.startingFrom( minModifiedId );

		int j = Arrays.binarySearch( ids, 0, oldSize, minModifiedId );
		// Fix index if Entity ID isn't on the list yet.
		j = Math.max( j, -j - 1 );

		// From the found position, rebuild the entity ID list.
		for ( int i = mbi.nextSetBit(); i >= 0; i = mbi.nextSetBit(), ++j )
		{
			ids[j] = i;
		}
	}

	/**
	 * Called after the processing of entities ends.
	 */
	protected void end ()
	{
		// Empty method.
	}

	/**
	 * Any implementing entity system must implement this method and the logic
	 * to process the given entities of the system.
	 * 
	 * @param entities the entities this system contains.
	 */
	protected abstract void processEntities ( ImmutableIntBag entities );

	/**
	 * Called if the system has received a entity it is interested in, e.g.
	 * created or a component was added to it.
	 * 
	 * @param eid the entity that was added to this system.
	 */
	protected void inserted ( final int eid )
	{
		// Empty method.
	}

	/**
	 * Called if a entity was removed from this system, e.g. deleted or had one
	 * of it's components removed.
	 * 
	 * @param eid the entity that was removed from this system.
	 */
	protected void removed ( final int eid )
	{
		// Empty method.
	}

	@Override
	public void added ( final ImmutableIntBag entities )
	{
		addAll( entities );
	}

	@Override
	public void changed ( final ImmutableIntBag entities )
	{
		checkAll( entities );
	}

	@Override
	public void deleted ( final ImmutableIntBag entities )
	{
		removeAll( entities );
	}

	@Override
	public void disabled ( final ImmutableIntBag entities )
	{
		removeAll( entities );
	}

	@Override
	public void enabled ( final ImmutableIntBag entities )
	{
		addAll( entities );
	}

	private final void removeFromSystem ( final int eid )
	{
		minModifiedId = Math.min( minModifiedId, eid );

		activeBits.fastClear( eid );
		removed( eid );
	}

	private final void insertToSystem ( final int eid )
	{
		minModifiedId = Math.min( minModifiedId, eid );

		activeBits.set( eid );
		inserted( eid );
	}

	private final void removeAll ( final ImmutableIntBag entities )
	{
		if ( aspect == null )
		{
			// Basically, a void entity system.
			return;
		}

		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;

		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];

			if ( acBits.get( eid ) )
			{
				removeFromSystem( eid );
			}
		}
	}

	private final void addAll ( final ImmutableIntBag entities )
	{
		final Aspect asp = aspect;
		if ( asp == null )
		{
			// Basically, a void entity system.
			return;
		}

		final FixedBitSet[] componentBits = world.componentManager().getComponentBits().data();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];

			if ( asp.isInteresting( componentBits[eid] ) )
			{
				insertToSystem( eid );
			}
		}
	}

	/**
	 * Adds entity if the system is interested in it and hasn't been added
	 * before.
	 * 
	 * Removes entity from system if its not interesting and it has been added
	 * before.
	 * 
	 * @param entities to check.
	 */
	private final void checkAll ( final ImmutableIntBag entities )
	{
		final Aspect asp = aspect;
		if ( asp == null )
		{
			// Basically, a void entity system.
			return;
		}

		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;

		final FixedBitSet[] componentBits = world.componentManager().getComponentBits().data();
		final int[] array = ((IntBag) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final int eid = array[i];
			// Second bit for 'contains'.
			int flags = acBits.getBit( eid ) << 1;
			// First bit for 'interesting'.
			flags |= asp.isInteresting( componentBits[eid] ) ? 0b1 : 0b0;

			switch (flags)
			{
			// Interesting and system doesn't contains.
				case 0b01:
				{
					insertToSystem( eid );
					continue;
				}
				// Not interesting and system does contains.
				case 0b10:
				{
					removeFromSystem( eid );
					continue;
				}
				// Otherwise do nothing.
				default:
					continue;
			}
		}
	}

	public int getIndex ()
	{
		return index;
	}

	public ImmutableIntBag getActives ()
	{
		return actives;
	}

}
