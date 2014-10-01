package com.artemis;

import static org.apache.lucene.util.OpenBitSet.intersectionCount;

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
	private final OpenBitSet noneSet;
	private final OpenBitSet oneSet;
	
	private final boolean noAll;
	private final boolean noNone;
	private final boolean noOne;
	private final boolean hasSome;
	
	Aspect ( OpenBitSet all, OpenBitSet none, OpenBitSet one )
	{
		noAll = all.isEmpty();
		noNone = none.isEmpty();
		noOne = one.isEmpty();
		
		// Check if this Aspect actually could be interested in an Entity.
		hasSome = !(noAll && noNone && noOne);
		
		// If any of the OpenBitSets is empty, do not store them.
		allSet = ( noAll ) ? null : all;
		noneSet = ( noNone ) ? null : none;
		oneSet = ( noOne ) ? null : one;
	}
	
	/**
	 * Checks if the Entity is interesting to this aspect.
	 * 
	 * @param e entity to check
	 * @return 'true' if it's interesting, 'false' otherwise.
	 */
	public final boolean isInteresting ( final Entity e )
	{
		final OpenBitSet bits = e.componentBits;

		// If has none, doesn't processes any entity.
		return 	hasSome &&
				checkNone( bits ) && 
				checkOne( bits ) && 
				checkAll( bits );
	}

	/**
	 * Checks if the provided bit set has at none of the bits set in
	 * noneSet.
	 * 
	 * @param bits to check.
	 * @return true if the two bit sets don't intersect, false otherwise.
	 */
	private final boolean checkNone ( final OpenBitSet bits )
	{
		// Reject entity if it has any of 'none' bits.
		return noNone || !noneSet.intersects( bits );
	}

	/**
	 * Checks if the provided bit set has at least one of the bits set in
	 * oneSet.
	 * 
	 * @param bits to check.
	 * @return true if the two bit sets intersect, false otherwise.
	 */
	private final boolean checkOne ( final OpenBitSet bits )
	{
		// Reject entity if it has none of 'one' bits.
		return noOne || oneSet.intersects( bits );
	}

	/**
	 * Checks if the provided bit set has all of the specified bits in allSet.
	 * 
	 * @param bits to check.
	 * @return true if bits has all the set bits in allSet, false otherwise.
	 */
	private final boolean checkAll ( final OpenBitSet bits )
	{
		final OpenBitSet all = allSet;
		/*
		 * Intersection bit count between allSet and bits should be same
		 * if Entity possesses all the components. Otherwise Aspect isn't
		 * interested.
		 */
		return noAll || all.cardinality() == intersectionCount( all, bits );
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
		private final OpenBitSet none;
		private final OpenBitSet one;
		
		public Builder ()
		{
			all = new OpenBitSet();
			none = new OpenBitSet();
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
			setBits( none, types );
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
			return new Aspect( all, none, one );
		}
	}

}
