package com.artemis;

import java.util.HashMap;

public final class EntityGroups
{
	private final HashMap<EntityFilter, EntityGroup> groups = new HashMap<>();
	private final ComponentManager cm;

	EntityGroups ( final ComponentManager cm )
	{
		this.cm = DustException.enforceNonNull( this, cm, "cm" );
	}

	public final EntityGroup matching ( final EntityFilter.Builder filter )
	{
		DustException.enforceNonNull( this, filter, "filter" );
		return matching( filter.build() );
	}

	public final EntityGroup matching ( final EntityFilter filter )
	{
		DustException.enforceNonNull( this, filter, "filter" );

		if ( filter.isEmpty() )
		{
			throw new DustException( this, "Cant pass an empty filter, it wont have any entities in it!" );
		}

		return groups.computeIfAbsent( filter, f -> new EntityGroup( filter, cm ) );
	}

	final EntityGroup[] groups ()
	{
		return groups.values().stream().toArray( EntityGroup[]::new );
	}
}
