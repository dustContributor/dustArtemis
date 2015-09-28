package com.artemis;

import org.apache.lucene.util.FixedBitSet;

import com.artemis.utils.ClassIndexer;

/**
 * <p>An Aspect is used by systems as a matcher against entities, to check if a
 * system is interested in an entity. Aspects define what sort of component
 * types an entity must possess, or not possess.</p> <p></p> <p>This creates an
 * aspect where an entity must possess A and B and C:
 * 
 * <p>new Aspect.Builder().all(A.class, B.class, C.class).build()</p> <p></p>
 * <p>This creates an aspect where an entity must possess A and B and C, but
 * must not possess U or V.</p>
 * 
 * <p>new Aspect.Builder().all(A.class, B.class, C.class).exclude(U.class,
 * V.class).build()</p> <p></p> <p>This creates an aspect where an entity must
 * possess A and B and C, but must not possess U or V, but must possess one of X
 * or Y or Z.</p>
 * 
 * <p>new Aspect.Builder().all(A.class, B.class, C.class).exclude(U.class,
 * V.class).one(X.class, Y.class, Z.class).build()</p>
 * 
 * @author Arni Arent
 *
 */
public final class Aspect
{
	/**
	 * If you need an entity system that doesn't process any entities but still
	 * needs to be run, consider extending {@link EntityObserver} directly
	 * instead of passing {@link #EMPTY_ASPECT} to {@link EntitySystem}'s
	 * constructor. Typical usages of such systems are when you need to create
	 * special purpose systems for debug rendering, like rendering FPS, how many
	 * entities are active in the world, etc.
	 */
	public static final Aspect EMPTY_ASPECT = new Builder().build();

	// Bit sets marking the components this aspect is interested in.
	private final FixedBitSet allSet;
	private final FixedBitSet noneSet;
	private final FixedBitSet oneSet;

	private final boolean hasSome;

	Aspect ( FixedBitSet all, FixedBitSet none, FixedBitSet one )
	{
		boolean noAll = all.isEmpty();
		boolean noNone = none.isEmpty();
		boolean noOne = one.isEmpty();

		// Check if this Aspect actually could be interested in an Entity.
		hasSome = !(noAll && noNone && noOne);

		// If any of the FixedBitSets is empty, do not store them.
		allSet = (noAll) ? null : all;
		noneSet = (noNone) ? null : none;
		oneSet = (noOne) ? null : one;
	}

	/**
	 * Checks if the components bits are interesting to this aspect.
	 * 
	 * @param bits to check
	 * @return 'true' if it's interesting, 'false' otherwise.
	 */
	public final boolean isInteresting ( final FixedBitSet bits )
	{
		// If has none, doesn't processes any entity.
		return hasSome && checkNone( bits ) && checkOne( bits ) && checkAll( bits );
	}

	/**
	 * Checks if the provided bit set has at none of the bits set in noneSet.
	 * 
	 * @param bits to check.
	 * @return true if the two bit sets don't intersect, false otherwise.
	 */
	private final boolean checkNone ( final FixedBitSet bits )
	{
		// Reject entity if it has any of 'none' bits.
		return (noneSet == null) || !noneSet.intersects( bits );
	}

	/**
	 * Checks if the provided bit set has at least one of the bits set in
	 * oneSet.
	 * 
	 * @param bits to check.
	 * @return true if the two bit sets intersect, false otherwise.
	 */
	private final boolean checkOne ( final FixedBitSet bits )
	{
		// Reject entity if it has none of 'one' bits.
		return (oneSet == null) || oneSet.intersects( bits );
	}

	/**
	 * Checks if the provided bit set has all of the specified bits in allSet.
	 * 
	 * @param bits to check.
	 * @return true if bits has all the set bits in allSet, false otherwise.
	 */
	private final boolean checkAll ( final FixedBitSet bits )
	{
		/*
		 * If the intersection of the two bit sets is equal to allSet, it means
		 * the passed bit set has all of allSet's bits set. Otherwise, reject
		 * the entity.
		 */
		return (allSet == null) || allSet.isIntersectionEqual( bits );
	}

	/**
	 * Returns 'true' if this Aspect can't be interested in an Entity, 'false'
	 * otherwise.
	 * 
	 * @return 'true' if this Aspect can't be interested in an Entity, 'false'
	 *         otherwise.
	 */
	public final boolean isEmpty ()
	{
		return !hasSome;
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from.
	 * 
	 * @return new {@link Builder} instance.
	 */
	public static final Builder builder ()
	{
		return new Aspect.Builder();
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from. Works as if {@link Builder#all(Class...)}
	 * was called on the {@link Builder}.
	 * 
	 * @see Builder#all(Class...)
	 * 
	 * @param types required for an entity to be accepted.
	 * @return new Builder instance.
	 */
	@SafeVarargs
	public static final Builder all ( final Class<? extends Component>... types )
	{
		return builder().all( types );
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from. Works as if
	 * {@link Builder#exclude(Class...)} was called on the {@link Builder}.
	 * 
	 * @see Builder#exclude(Class...)
	 * 
	 * @param types avoided for an entity to be accepted.
	 * @return new {@link Builder} instance.
	 */
	@SafeVarargs
	public static final Builder exclude ( final Class<? extends Component>... types )
	{
		return builder().exclude( types );
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from. Works as if {@link Builder#one(Class...)}
	 * was called on the {@link Builder}.
	 * 
	 * @see Builder#one(Class...)
	 * 
	 * @param types any of the types the entity must possess.
	 * @return new {@link Builder} instance.
	 */
	@SafeVarargs
	public static final Builder one ( final Class<? extends Component>... types )
	{
		return builder().one( types );
	}

	/**
	 * Builder class to configure and create Aspects from it.
	 * 
	 * @author dustContributor
	 *
	 */
	public static final class Builder
	{
		private final FixedBitSet all;
		private final FixedBitSet none;
		private final FixedBitSet one;

		public Builder ()
		{
			final int wordCount = DAConstants.COMPONENT_BITS_WORD_COUNT;
			all = FixedBitSet.newBitSetByWords( wordCount );
			none = FixedBitSet.newBitSetByWords( wordCount );
			one = FixedBitSet.newBitSetByWords( wordCount );
		}

		/**
		 * Returns a {@link Builder} where an entity must possess all of the
		 * specified component types.
		 * 
		 * @param types a required component types.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder all ( final Class<? extends Component>... types )
		{
			setBits( all, types );
			return this;
		}

		/**
		 * Excludes all of the specified component types from the Builder. A
		 * system will not be interested in an entity that possesses one of the
		 * specified exclusion component types.
		 * 
		 * @param types component types to exclude.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder exclude ( final Class<? extends Component>... types )
		{
			setBits( none, types );
			return this;
		}

		/**
		 * Returns an {@link Builder} where an entity must possess any of the
		 * specified component types.
		 * 
		 * @param types any of the types the entity must possess.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder one ( final Class<? extends Component>... types )
		{
			setBits( one, types );
			return this;
		}

		@SafeVarargs
		private static final void setBits (
			final FixedBitSet bits,
			final Class<? extends Component>... types )
		{
			for ( int i = types.length; i-- > 0; )
			{
				bits.set( ClassIndexer.getIndexFor( types[i], Component.class ) );
			}
		}

		/**
		 * Builds an Aspect based on how this {@link Builder} was configured.
		 * 
		 * @return Aspect based on this {@link Builder} configuration.
		 */
		public final Aspect build ()
		{
			return new Aspect( all, none, one );
		}
	}

}
