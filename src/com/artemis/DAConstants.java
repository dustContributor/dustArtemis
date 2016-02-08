package com.artemis;

import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

/**
 * dustArtemis constants used through the framework.
 * 
 * <p>
 * These values are loaded from a config file when the class is loaded. Config
 * file path is retrieved from {@value #CFG_FILE_PROPERTY_NAME} property in
 * Java's system properties.
 * </p>
 * 
 * <p>
 * Constants not specified in the file will be set to a reasonable default, also
 * all constants have minimum bounds too.
 * </p>
 * 
 * <p>
 * Remember that most of these values aren't limits, they're approximates. If
 * you add more entities than you specified, backing collections will grow and
 * dustArtemis keeps working. These values are useful mostly to specify default
 * values so the backing collections don't trash arrays at application startup
 * when adding components/entities to your World instance.
 * </p>
 * 
 * <p>
 * In this regard, {@link #COMPONENT_TYPES_COUNT} is actually a limit. Since the
 * introduction of fixed-size bit sets (which are much more cost efficient),
 * dustArtemis needs to know how many component types there will be around to
 * decide the size of the bit sets that it will use, so have that in mind too.
 * </p>
 * 
 * <p>
 * For example, to set the Bag constants, you'd need to put this in a plain text
 * file:
 * </p>
 * 
 * <pre>
 * BAG_DEFAULT_CAPACITY=32
 * BAG_GROW_RATE_THRESOLD=1024
 * </pre>
 * 
 * <p>
 * Then just point to its relative path (from the application's POV) where the
 * file is located like this:
 * </p>
 * 
 * <pre>
 * System.getProperties().put( {@value #CFG_FILE_PROPERTY_NAME},
 * "cfg/artemis.cfg");
 * </pre>
 * 
 * <p>
 * Extension is not important, it just needs to be a plain text file. Since this
 * uses Java Properties API, all {@link java.util.Properties} particulars apply.
 * You can always check dustArtemis sources if you need to know something
 * specific about it.
 * </p>
 * 
 * @author dustContributor
 *
 */
public final class DAConstants
{
	/** Non-instantiable class. */
	private DAConstants ()
	{
		// Empty.
	}

	/** Name of the property that will be used to fetch config file path from. */
	public static final String CFG_FILE_PROPERTY_NAME = "dustArtemis.cfgpath";

	/** Default capacity for ImmutableBag and subclasses. */
	public static final int BAG_DEFAULT_CAPACITY;
	/** Threshold in which growth strategy is changed for bags. */
	public static final int BAG_GROW_RATE_THRESHOLD;
	/** Approximate amount of live entities in the world. */
	public static final int APPROX_LIVE_ENTITIES;
	/** Approximate amount of entities active in each system. */
	public static final int APPROX_ENTITIES_PER_SYSTEM;
	/**
	 * Count of the different component types there will be. This value is used so
	 * dustArtemis knows the size of the bit set it will use for initializing
	 * entities and aspects. If you add more component types than you specify
	 * here, things will probably break.
	 */
	public static final int COMPONENT_TYPES_COUNT;
	/**
	 * How many 64 bit words the bit sets will need to hold the different
	 * component types there will be. This value is computed from
	 * {@value #COMPONENT_TYPES_COUNT}, so you don't have to specify it.
	 */
	public static final int COMPONENT_BITS_WORD_COUNT;

	static
	{
		final Properties props = loadCfgFile();

		int itmp = 0;

		itmp = getIntOrDefault( props, "BAG_DEFAULT_CAPACITY", 16 );
		BAG_DEFAULT_CAPACITY = Math.max( itmp, 4 );

		itmp = getIntOrDefault( props, "BAG_GROW_RATE_THRESHOLD", 2048 );
		BAG_GROW_RATE_THRESHOLD = Math.max( itmp, 256 );

		itmp = getIntOrDefault( props, "APPROX_LIVE_ENTITIES", 1024 );
		APPROX_LIVE_ENTITIES = Math.max( itmp, 64 );

		itmp = getIntOrDefault( props, "APPROX_ENTITIES_PER_SYSTEM", 1024 );
		APPROX_ENTITIES_PER_SYSTEM = Math.max( itmp, 16 );

		itmp = getIntOrDefault( props, "COMPONENT_TYPES_COUNT", 64 );
		itmp = Math.max( itmp, 16 );
		COMPONENT_TYPES_COUNT = itmp;
		COMPONENT_BITS_WORD_COUNT = ((Math.min( itmp, 256 ) - 1) / 64) + 1;
	}

	/**
	 * Returns an int fetched and parsed from the properties table, or defaults to
	 * passed value if there isn't one or the value couldn't be parsed.
	 */
	private static final int getIntOrDefault ( Properties props, String key, int defValue )
	{
		final String tmp = props.getProperty( key );
		if ( tmp != null )
		{
			try
			{
				return Integer.parseInt( tmp );
			}
			// Prolly the only exception type that could be raised here.
			catch ( NumberFormatException ex )
			{
				// Fail silently.
			}
		}

		return defValue;
	}

	/**
	 * Returns a boolean fetched and parsed from the properties table, or defaults
	 * to passed value if there isn't one or the value couldn't be parsed.
	 */
	private static final boolean getBoolOrDefault ( Properties props, String key, boolean defValue )
	{
		final String tmp = props.getProperty( key );
		if ( tmp != null )
		{
			try
			{
				return Boolean.parseBoolean( tmp );
			}
			// Prolly the only exception type that could be raised here.
			catch ( NumberFormatException ex )
			{
				// Fail silently.
			}
		}

		return defValue;
	}

	/**
	 * Loads configuration file for dustArtemis constants, fetching the path from
	 * the property {@value #CFG_FILE_PROPERTY_NAME}.
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
