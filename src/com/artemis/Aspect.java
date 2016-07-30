package com.artemis;

import java.util.HashSet;
import java.util.Objects;

import org.apache.lucene.util.BitUtil;

/**
 * <p>
 * An Aspect is used by systems as a matcher against entities, to check if a
 * system is interested in an entity. Aspects define what sort of component
 * types an entity must possess, or not possess.
 * </p>
 *
 * <p>
 * This creates an aspect where an entity must possess A and B and C:
 * </p>
 *
 * <pre>
 * Aspect a = Aspect.all( A.class, B.class, C.class ).build()
 * </pre>
 *
 * <p>
 * This creates an aspect where an entity must possess A and B and C, but must
 * not possess U or V.
 * </p>
 *
 * <pre>
 * Aspect a = Aspect
 * 	.all( A.class, B.class, C.class )
 * 	.exclude( U.class, V.class )
 * 	.build()
 * </pre>
 *
 * <p>
 * This creates an aspect where an entity must possess A and B and C, but must
 * not possess U or V, but must possess one of X or Y or Z.
 * </p>
 *
 * <pre>
 * Aspect a = Aspect
 * 	.all( A.class, B.class, C.class )
 * 	.exclude( U.class, V.class )
 * 	.one( X.class, Y.class, Z.class )
 * 	.build()
 * </pre>
 *
 * @author Arni Arent
 * @author dustContributor
 *
 */
public final class Aspect
{
	private final int wordCount;
	// Bit sets marking the components this aspect is interested in.
	private final long[] all;
	private final long[] none;
	private final long[] one;

	Aspect ( final long[] all, final long[] none, final long[] one, final int wordCount )
	{
		DustException.enforceNonNull( Aspect.class, all, "all" );
		DustException.enforceNonNull( Aspect.class, none, "none" );
		DustException.enforceNonNull( Aspect.class, one, "one" );

		// If any of the bit sets is empty, do not store them.
		this.all = BitUtil.isEmpty( all ) ? null : all;
		this.none = BitUtil.isEmpty( none ) ? null : none;
		this.one = BitUtil.isEmpty( one ) ? null : one;

		this.wordCount = wordCount;
	}

	/**
	 * Checks if the components bits are interesting to this aspect.
	 *
	 * @param id of the entity to check
	 *
	 * @param bits to check
	 * @return 'true' if it's interesting, 'false' otherwise.
	 */
	public final boolean isInteresting ( final int id, final long[] bits )
	{
		// A bit of global access never hurt anyone.
		final int start = id * wordCount;

		return checkNone( bits, start )
				& checkOne( bits, start )
				& checkAll( bits, start );
	}

	/**
	 * Checks if the provided bit set has at none of the bits set in noneSet.
	 *
	 * @param bits to check.
	 * @return true if the two bit sets don't intersect, false otherwise.
	 */
	private final boolean checkNone ( final long[] other, final int otherStart )
	{
		if ( none == null )
		{
			return true;
		}
		// Reject entity if it has any of 'none' bits.
		return !BitUtil.intersects( none, 0, other, otherStart, wordCount );
	}

	/**
	 * Checks if the provided bit set has at least one of the bits set in oneSet.
	 *
	 * @param bits to check.
	 * @return true if the two bit sets intersect, false otherwise.
	 */
	private final boolean checkOne ( final long[] other, final int otherStart )
	{
		if ( one == null )
		{
			return true;
		}
		// Reject entity if it has none of 'one' bits.
		return BitUtil.intersects( one, 0, other, otherStart, wordCount );
	}

	/**
	 * Checks if the provided bit set has all of the specified bits in allSet.
	 *
	 * @param bits to check.
	 * @return true if bits has all the set bits in allSet, false otherwise.
	 */
	private final boolean checkAll ( final long[] other, final int otherStart )
	{
		if ( all == null )
		{
			return true;
		}
		/*
		 * If the intersection of the two bit sets is equal to allSet, it means the
		 * passed bit set has all of allSet's bits set. Otherwise, reject the
		 * entity.
		 */
		return BitUtil.intersectionEqual( all, 0, other, otherStart, wordCount );
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
		return (all == null && none == null && all == null);
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
		private final HashSet<Class<? extends Component>> all;
		private final HashSet<Class<? extends Component>> none;
		private final HashSet<Class<? extends Component>> one;

		Builder ()
		{
			this.all = new HashSet<>();
			this.none = new HashSet<>();
			this.one = new HashSet<>();
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
			addAll( all, types );
			return this;
		}

		/**
		 * Excludes all of the specified component types from the Builder. A system
		 * will not be interested in an entity that possesses one of the specified
		 * exclusion component types.
		 *
		 * @param types component types to exclude.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder exclude ( final Class<? extends Component>... types )
		{
			addAll( none, types );
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
			addAll( one, types );
			return this;
		}

		private static final <T> void addAll ( final HashSet<T> dest, final T[] items )
		{
			for ( final T item : items )
			{
				dest.add( Objects.requireNonNull( item ) );
			}
		}

		private static final long[] composeBitSet (
				final ComponentManager cm,
				final Iterable<Class<? extends Component>> types )
		{
			final long[] dest = new long[cm.wordsPerEntity()];

			for ( final Class<? extends Component> type : types )
			{
				final int componentIndex = cm.indexFor( type );

				if ( componentIndex < 0 )
				{
					final String msg = "Missing index for component of type: " + System.lineSeparator()
							+ type.toString() + System.lineSeparator()
							+ "Types need to be present in the world builder for aspects to use them!";
					throw new DustException( Aspect.class, msg );
				}

				BitUtil.set( dest, componentIndex );
			}

			return dest;
		}

		/**
		 * Builds an Aspect based on how this {@link Builder} was configured.
		 *
		 * @return Aspect based on this {@link Builder} configuration.
		 */
		final Aspect build ( final ComponentManager cm )
		{
			final long[] all = composeBitSet( cm, this.all );
			final long[] none = composeBitSet( cm, this.none );
			final long[] one = composeBitSet( cm, this.one );
			return new Aspect( all, none, one, cm.wordsPerEntity() );
		}
	}

}
