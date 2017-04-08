package com.artemis;

import java.util.HashMap;

/**
 * The purpose of this class is to allow you to request {@link EntityGroup}
 * instances that match a specific filter. Its meant to collect the existing
 * instances so further requests for the same filter will return the same
 * {@link EntityGroup} instance.
 * 
 * @author dustContributor
 *
 */
public final class EntityGroups
{
	private final HashMap<EntityFilter, EntityGroup> groups = new HashMap<>();
	private final ComponentManager cm;

	EntityGroups ( final ComponentManager cm )
	{
		this.cm = DustException.enforceNonNull( this, cm, "cm" );
	}

	/**
	 * Convenience method that builds the {@link EntityFilter} first then
	 * delegates to {@link #matching(EntityFilter)}.
	 * 
	 * @param filter builder to construct an {@link EntityFilter} from.
	 * @return an {@link EntityGroup} matching the provided {@link EntityFilter}.
	 * @see EntityGroups#matching(EntityFilter)
	 */
	public final EntityGroup matching ( final EntityFilter.Builder filter )
	{
		DustException.enforceNonNull( this, filter, "filter" );
		return matching( filter.build() );
	}

	/**
	 * Returns an {@link EntityGroup} that only contains entities that match the
	 * supplied {@link EntityFilter}.
	 * 
	 * @param filter to match against entities in the {@link EntityGroup}.
	 * @return an {@link EntityGroup} matching the provided {@link EntityFilter}.
	 */
	public final EntityGroup matching ( final EntityFilter filter )
	{
		DustException.enforceNonNull( this, filter, "filter" );

		if ( filter.isEmpty() )
		{
			throw new DustException( this, "Cant pass an empty filter, it wont have any entities in it!" );
		}

		return groups.computeIfAbsent( filter, f -> new EntityGroup( f, this.cm ) );
	}

	final EntityGroup[] groups ()
	{
		return groups.values().toArray( new EntityGroup[groups.size()] );
	}
}
