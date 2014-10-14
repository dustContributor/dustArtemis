package com.artemis;

import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

/**
 * dustArtemis constants used through the framework.
 * 
 * <br> <p> These values are loaded from a config file when the class is loaded.
 * Config file path is retrieved from <b>"dustArtemis.cfgpath"</b>
 * property in Java's system properties. </p> </br>
 * 
 * <br><p> Constants not specified in the file will be set to a reasonable
 * default, also all constants have minimum bounds too. </p> </br>
 * 
 * <br><p> Remember that most of these values aren't limits, they're
 * approximates. If you add more entities than you specified, backing
 * collections will grow and dustArtemis keeps working. These values are useful
 * mostly to specify default values so the backing collections don't trash
 * arrays at application startup when adding components/entities to your World
 * instance. </p> </br>
 * 
 * <br><p> For example, to set the Bag constants, you'd need to put this in a
 * plain text file: </p></br>
 * 
 * <p><code><br>BAG_DEFAULT_CAPACITY=32</br>
 * <br>BAG_GROW_RATE_THRESOLD=1024</br></code></p>
 * 
 * <br><p> Then just point to its relative path (from the application's POV)
 * where the file is located like this:</p></br>
 * 
 * <p><code><br>System.getProperties().put( "dustArtemis.cfgpath",
 * "cfg/artemis.cfg");</br></code></p>
 * 
 * <br><p> Extension is not important, it just needs to be a plain text file.
 * Since this uses Java Properties API, all {@link java.util.Properties}
 * particulars apply. You can always check dustArtemis sources if you need to
 * know something specific about it. </p></br>
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
	private static final String CFG_FILE_PROPERTY_NAME = "dustArtemis.cfgpath";
	
	/** Default capacity for ImmutableBag and subclasses. */
	public static final int BAG_DEFAULT_CAPACITY;
	/** Threshold in which growth strategy is changed for bags. */
	public static final int BAG_GROW_RATE_THRESHOLD;
	/** Approximate amount of live entities in the world. */
	public static final int APPROX_LIVE_ENTITIES;
	/** Approximate amount of entities active in each system. */
	public static final int APPROX_ENTITIES_PER_SYSTEM;
	/** Approximate amount of component types. */
	public static final int APPROX_COMPONENT_TYPES;
	/** Approximate amount of mappers per system. */
	public static final int APPROX_MAPPERS_PER_SYSTEM;

	static
	{
		final Properties props = loadCfgFile();
		
		int tmp = 0;

		tmp = getIntOrDefault( props, "BAG_DEFAULT_CAPACITY", 16 );
		BAG_DEFAULT_CAPACITY = Math.max( tmp, 4 );

		tmp = getIntOrDefault( props, "BAG_GROW_RATE_THRESHOLD", 2048 );
		BAG_GROW_RATE_THRESHOLD = Math.max( tmp, 256 );

		tmp = getIntOrDefault( props, "APPROX_LIVE_ENTITIES", 1024 );
		APPROX_LIVE_ENTITIES = Math.max( tmp, 64 );

		tmp = getIntOrDefault( props, "APPROX_ENTITIES_PER_SYSTEM", 1024 );
		APPROX_ENTITIES_PER_SYSTEM = Math.max( tmp, 16 );

		tmp = getIntOrDefault( props, "APPROX_COMPONENT_TYPES", 64 );
		APPROX_COMPONENT_TYPES = Math.max( tmp, 16 );
		
		tmp = getIntOrDefault( props, "APPROX_MAPPERS_PER_SYSTEM", 8 );
		APPROX_MAPPERS_PER_SYSTEM = Math.max( tmp, 4 );
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
	 * from the property {@value #CFG_FILE_PROPERTY_NAME}.
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
