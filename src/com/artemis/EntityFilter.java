package com.artemis;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.lucene.util.BitUtil;

/**
 * <p>
 * An {@link EntityFilter} is used by systems as a matcher against entities, to
 * check if a system is interested in an entity. {@link EntityFilter}s define
 * what sort of component types an entity must possess, or not possess.
 * </p>
 *
 * <p>
 * This creates an {@link EntityFilter} where an entity must possess A and B and
 * C:
 * </p>
 *
 * <pre>
 * {@link EntityFilter} a = {@link EntityFilter}.all( A.class, B.class, C.class ).build()
 * </pre>
 *
 * <p>
 * This creates an {@link EntityFilter} where an entity must possess A and B and
 * C, but must not possess U or V.
 * </p>
 *
 * <pre>
 * {@link EntityFilter} a = {@link EntityFilter}
 * 	.all( A.class, B.class, C.class )
 * 	.exclude( U.class, V.class )
 * 	.build()
 * </pre>
 *
 * <p>
 * This creates an {@link EntityFilter} where an entity must possess A and B and
 * C, but must not possess U or V, but must possess one of X or Y or Z.
 * </p>
 *
 * <pre>
 * {@link EntityFilter} a = {@link EntityFilter}
 * 	.all( A.class, B.class, C.class )
 * 	.exclude( U.class, V.class )
 * 	.one( X.class, Y.class, Z.class )
 * 	.build()
 * </pre>
 *
 * @author dustContributor
 *
 */
public final class EntityFilter {
	private final int hashCode;

	private final Class<? extends Component>[] all;
	private final Class<? extends Component>[] none;
	private final Class<? extends Component>[] any;

	EntityFilter(final EntityFilter.Builder b) {
		this.all = b.all.stream().toArray(Class[]::new);
		this.none = b.none.stream().toArray(Class[]::new);
		this.any = b.any.stream().toArray(Class[]::new);
		this.hashCode = hashCode(this.all, this.any, this.none);
	}

	@Override
	public final boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof EntityFilter)) {
			return false;
		}

		final EntityFilter a = (EntityFilter) obj;

		return hashCode == a.hashCode
				&& Arrays.equals(all, a.all)
				&& Arrays.equals(none, a.none)
				&& Arrays.equals(any, a.any);
	}

	@Override
	public final int hashCode() {
		return this.hashCode;
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link EntityFilter} instances from. Works as if
	 * {@link Builder#all(Class...)} was called on the {@link Builder}.
	 *
	 * @see Builder#all(Class...)
	 *
	 * @param types required for an entity to be accepted.
	 * @return new Builder instance.
	 */
	@SafeVarargs
	public static final Builder all(final Class<? extends Component>... types) {
		return builder().all(types);
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link EntityFilter} instances from. Works as if
	 * {@link Builder#none(Class...)} was called on the {@link Builder}.
	 *
	 * @see Builder#none(Class...)
	 *
	 * @param types avoided for an entity to be accepted.
	 * @return new {@link Builder} instance.
	 */
	@SafeVarargs
	public static final Builder none(final Class<? extends Component>... types) {
		return builder().none(types);
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link EntityFilter} instances from. Works as if
	 * {@link Builder#any(Class...)} was called on the {@link Builder}.
	 *
	 * @see Builder#any(Class...)
	 *
	 * @param types any of the types the entity must possess.
	 * @return new {@link Builder} instance.
	 */
	@SafeVarargs
	public static final Builder any(final Class<? extends Component>... types) {
		return builder().any(types);
	}

	/**
	 * Factory method that returns a new {@link EntityFilter} instance to build
	 * {@link EntityFilter} instances from.
	 *
	 * @return new {@link EntityFilter} instance.
	 */
	public static final EntityFilter.Builder builder() {
		return new EntityFilter.Builder();
	}

	final long[] allBits(final ComponentManager cm) {
		return composeBitSet(cm, this.all);
	}

	final long[] noneBits(final ComponentManager cm) {
		return composeBitSet(cm, this.none);
	}

	final long[] anyBits(final ComponentManager cm) {
		return composeBitSet(cm, this.any);
	}

	final boolean isEmpty() {
		return this.all.length < 1
				&& this.none.length < 1
				&& this.any.length < 1;
	}

	private static final long[] composeBitSet(
			final ComponentManager cm,
			final Class<? extends Component>[] types) {
		final long[] dest = new long[cm.wordsPerEntity()];

		for (final Class<? extends Component> type : types) {
			final int componentIndex = cm.indexFor(type);

			if (componentIndex < 0) {
				final String msg = "Missing index for component of type: " + System.lineSeparator()
						+ type.toString() + System.lineSeparator()
						+ "Types need to be present in the world builder for EntityFilters to use them!";
				throw new DustException(EntityFilter.class, msg);
			}

			BitUtil.set(dest, componentIndex);
		}

		return dest;
	}

	/**
	 * We don't use wordCount here since it ought to be equal in all instances.
	 */
	@SafeVarargs
	private static final <T> int hashCode(final T[]... typeSets) {
		final int mul = 31;
		int hash = 1;

		for (int i = 0; i < typeSets.length; ++i) {
			final T[] types = typeSets[i];
			/*
			 * Avoids collisions for sets with the same component types but different kinds
			 * (ie, all/any/none).
			 */
			hash = mul * hash + i;
			for (final T type : types) {
				hash = mul * hash + type.hashCode();
			}
		}

		return hash;
	}

	/**
	 * {@link Builder} class to configure and create {@link EntityFilter} from it.
	 *
	 * @author dustContributor
	 *
	 */
	public static final class Builder {
		final HashSet<Class<? extends Component>> all;
		final HashSet<Class<? extends Component>> none;
		final HashSet<Class<? extends Component>> any;

		Builder() {
			this.all = new HashSet<>();
			this.none = new HashSet<>();
			this.any = new HashSet<>();
		}

		/**
		 * Returns a {@link Builder} where an entity must possess all of the specified
		 * component types.
		 *
		 * @param types a required component types.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder all(final Class<? extends Component>... types) {
			addAll(all, types);
			return this;
		}

		/**
		 * Excludes all of the specified component types from the {@link Builder}. A
		 * system will not be interested in an entity that possesses one of the
		 * specified exclusion component types.
		 *
		 * @param types component types to exclude.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder none(final Class<? extends Component>... types) {
			addAll(none, types);
			return this;
		}

		/**
		 * Returns an {@link Builder} where an entity must possess any of the specified
		 * component types.
		 *
		 * @param types any of the types the entity must possess.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder any(final Class<? extends Component>... types) {
			addAll(any, types);
			return this;
		}

		/**
		 * Builds an {@link EntityFilter} based on how this {@link Builder} was
		 * configured.
		 *
		 * @return {@link EntityFilter} based on this {@link Builder} configuration.
		 */
		public final EntityFilter build() {
			return new EntityFilter(this);
		}

		private final <T> void addAll(final HashSet<T> dest, final T[] items) {
			for (final T item : DustException.enforceNonNull(this, items, "items")) {
				dest.add(DustException.enforceNonNull(this, item, "item"));
			}
		}
	}

}
