package com.artemis;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.ClassIndexer;

/**
 * <p>An Aspect is used by systems as a matcher against entities, to check if a system is
 * interested in an entity. Aspects define what sort of component types an entity must
 * possess, or not possess.</p>
 * <p></p>
 * <p>This creates an aspect where an entity must possess A and B and C:
 * 
 * <p>new Aspect.Builder().all(A.class, B.class, C.class).build()</p>
 * <p></p>
 * <p>This creates an aspect where an entity must possess A and B and C, but must not possess U or V.</p>
 * 
 * <p>new Aspect.Builder().all(A.class, B.class, C.class).exclude(U.class, V.class).build()</p>
 * <p></p>
 * <p>This creates an aspect where an entity must possess A and B and C, but must not possess U or V, but must possess one of X or Y or Z.</p>
 * 
 * <p>new Aspect.Builder().all(A.class, B.class, C.class).exclude(U.class, V.class).one(X.class, Y.class, Z.class).build()</p>
 * 
 * @author Arni Arent
 *
 */
public final class Aspect
{
	/**
	 * This empty Aspect instance can be used if you want a system that
	 * processes no entities, but still gets invoked. Typical usages is when you
	 * need to create special purpose systems for debug rendering, like
	 * rendering FPS, how many entities are active in the world, etc.
	 */
	public static final Aspect EMPTY_ASPECT = new Builder().build();

	// Bit sets marking the components this aspect is interested in.
	private final OpenBitSet allSet;
	private final OpenBitSet exclusionSet;
	private final OpenBitSet oneSet;
	
	private final boolean hasAll;
	private final boolean hasExclusion;
	private final boolean hasOne;
	private final boolean hasNone;
	
	Aspect ( OpenBitSet all, OpenBitSet exclusion, OpenBitSet one )
	{
		hasAll = !all.isEmpty();
		hasExclusion = !exclusion.isEmpty();
		hasOne = !one.isEmpty();
		// Check if this Aspect actually could be interested in an Entity.
		hasNone = !(hasAll || hasExclusion || hasOne);
		
		/*
		 * If any of the OpenBitSets is empty, do not store them.
		 */
		if ( hasAll )
		{
			allSet = all;
		}
		else
		{
			allSet = null;
		}
		
		if ( hasExclusion )
		{
			exclusionSet = exclusion;
		}
		else
		{
			exclusionSet = null;
		}
		
		if ( hasOne )
		{
			oneSet = one;
		}
		else
		{
			oneSet = null;
		}
	}
	
	/**
	 * Checks if the Entity is interesting to this aspect.
	 * 
	 * @param e entity to check
	 * @return 'true' if it's interesting, 'false' otherwise.
	 */
	public final boolean isInteresting ( final Entity e )
	{
		// If has none, doesn't processes any entity.
		if ( hasNone )
		{
			return false;
		}

		final OpenBitSet cmpBits = e.componentBits;
		/*
		 * Early rejection if the entity has an 'exclusion' component or doesn't
		 * has any 'one' component.
		 */
		if ( 
				// Check if the entity possesses ANY of the exclusion components.
				( hasExclusion && exclusionSet.intersects( cmpBits ) ) 
				//  Check if the entity lacks ANY of the components in the oneSet.
				|| ( hasOne && !oneSet.intersects( cmpBits ) ) 
			)
		{
			// Aspect isn't interested in the entity.
			return false;
		}
		/*
		 * Check if the entity possesses ALL of the components defined in the
		 * aspect.
		 */
		if ( hasAll )
		{
			final OpenBitSet all = allSet;
			for ( int i = all.nextSetBit( 0 ); i >= 0; i = all.nextSetBit( i + 1 ) )
			{
				if ( !cmpBits.get( i ) )
				{
					// Aspect isn't interested in the entity.
					return false;
				}
				// Aspect is still interested, keep checking.
			}
		}

		// The Aspect is interested in the Entity.
		return true;
	}
	
	/**
	 * Builder class to configure and create Aspects from it.
	 * 
	 * @author dustContributor
	 *
	 */
	public static final class Builder
	{
		private final OpenBitSet all;
		private final OpenBitSet exclusion;
		private final OpenBitSet one;
		
		public Builder ()
		{
			all = new OpenBitSet();
			exclusion = new OpenBitSet();
			one = new OpenBitSet();
		}
		
		/**
		 * Returns a Builder where an entity must possess all of the specified
		 * component types.
		 * 
		 * @param types
		 *            a required component types.
		 * @return this Builder instance.
		 */
		@SafeVarargs
		public final Builder all ( final Class<? extends Component>... types )
		{
			setBits( all, types );
			return this;
		}

		/**
		 * Excludes all of the specified component types from the Builder. A system
		 * will not be interested in an entity that possesses one of the specified
		 * exclusion component types.
		 * 
		 * @param types
		 *            component types to exclude.
		 * @return this Builder instance.
		 */
		@SafeVarargs
		public final Builder exclude ( final Class<? extends Component>... types )
		{
			setBits( exclusion, types );
			return this;
		}

		/**
		 * Returns an Builder where an entity must possess any of the specified
		 * component types.
		 * 
		 * @param types
		 *            any of the types the entity must possess.
		 * @return this Builder instance.
		 */
		@SafeVarargs
		public final Builder one ( final Class<? extends Component>... types )
		{
			setBits( one, types );
			return this;
		}
		
		@SafeVarargs
		private static final void setBits (
			final OpenBitSet bits,
			final Class<? extends Component>... types )
		{
			for ( int i = types.length; i-- > 0; )
			{
				bits.set( ClassIndexer.getIndexFor( types[i], Component.class ) );
			}
		}
		
		/**
		 * Builds an Aspect based on how this Builder was configured.
		 * 
		 * @return new Aspect based on this Builder configuration.
		 */
		public final Aspect build ()
		{
			return new Aspect( all, exclusion, one );
		}
	}

}
