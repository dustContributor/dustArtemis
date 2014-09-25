package com.artemis;

import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

/**
 * dustArtemis constants used through the framework.
 * 
 * @author dustContributor
 *
 */
public final class DAConstants
{
	private DAConstants ()
	{
		// Empty.
	}

	/** Name of the property that will be used to fetch config file path from. */
	private static final String CFG_FILE_PROPERTY_NAME = "dustArtemis.cfgpath";
	
	/** Default capacity for ImmutableBag and subclasses. */
	public static final int BAG_DEFAULT_CAPACITY;
	/** Threshold in which growth strategy is changed for bags. */
	public static final int BAG_GROW_RATE_THRESHOLD;
	/** Approximate amount of live entities in the world. */
	public static final int APROX_LIVE_ENTITIES;
	/** Approximate amount of entities active in each system. */
	public static final int APROX_ENTITIES_PER_SYSTEM;
	/** Approximate amount of component types. */
	public static final int APROX_COMPONENT_TYPES;
	/** Approximate amount of mappers per system. */
	public static final int APROX_MAPPERS_PER_SYSTEM;

	static
	{
		final Properties props = loadCfgFile();
		
		int tmp = 0;

		tmp = getIntOrDefault( props, "BAG_DEFAULT_CAPACITY", 16 );
		BAG_DEFAULT_CAPACITY = Math.max( tmp, 4 );

		tmp = getIntOrDefault( props, "BAG_GROW_RATE_THRESHOLD", 2048 );
		BAG_GROW_RATE_THRESHOLD = Math.max( tmp, 256 );

		tmp = getIntOrDefault( props, "APROX_LIVE_ENTITIES", 1024 );
		APROX_LIVE_ENTITIES = Math.max( tmp, 128 );

		tmp = getIntOrDefault( props, "APROX_ENTITIES_PER_SYSTEM", 1024 );
		APROX_ENTITIES_PER_SYSTEM = Math.max( tmp, 128 );

		tmp = getIntOrDefault( props, "APROX_COMPONENT_TYPES", 64 );
		APROX_COMPONENT_TYPES = Math.max( tmp, 16 );
		
		tmp = getIntOrDefault( props, "APROX_MAPPERS_PER_SYSTEM", 8 );
		APROX_MAPPERS_PER_SYSTEM = Math.max( tmp, 4 );
	}

	/**
	 * Returns an int fetched and parsed from the properties table, or defaults
	 * to passed value if there isn't one or the value couldn't be parsed.
	 */
	private static final int getIntOrDefault ( Properties props, String key, int defValue )
	{
		try
		{
			String tmp = props.getProperty( key );
			if ( tmp != null )
			{
				return Integer.parseInt( tmp );
			}
		}
		// Prolly the only exception type that could be raised here.
		catch ( NumberFormatException ex )
		{
			// Fail silently.
		}
		
		return defValue;
	}
	
	/**
	 * Loads configuration file for dustArtemis constants, fetching the path
	 * from the property "dustArtemis.cfgpath".
	 */
	private static final Properties loadCfgFile ()
	{
		final String dir = System.getProperty( CFG_FILE_PROPERTY_NAME );
		final Properties props = new Properties();

		if ( dir != null )
		{
			Path path = null;

			try
			{
				path = FileSystems.getDefault().getPath( dir );
			}
			// Don't care about the exception type.
			catch ( Exception e )
			{
				// Fail silently.
			}

			if ( path != null )
			{
				try ( FileReader fr = new FileReader( path.toFile() ) )
				{
					props.load( fr );
				}
				// Don't care about the exception type.
				catch ( Exception e )
				{
					// Fail silently.
				}
			}
		}
		
		return props;
	}
}
