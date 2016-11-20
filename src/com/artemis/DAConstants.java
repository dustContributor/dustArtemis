package com.artemis;

import java.io.File;
import java.io.FileReader;
import java.nio.file.FileSystems;
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
		throw new DustException( DAConstants.class, "Can't create an instance of this class!" );
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

	static
	{
		final Properties props = loadCfgFile();

		BAG_DEFAULT_CAPACITY = Math.max( getIntOrDefault( props, "BAG_DEFAULT_CAPACITY", 16 ), 4 );
		BAG_GROW_RATE_THRESHOLD = Math.max( getIntOrDefault( props, "BAG_GROW_RATE_THRESHOLD", 2048 ), 256 );
		APPROX_LIVE_ENTITIES = Math.max( getIntOrDefault( props, "APPROX_LIVE_ENTITIES", 1024 ), 64 );
		APPROX_ENTITIES_PER_SYSTEM = Math.max( getIntOrDefault( props, "APPROX_ENTITIES_PER_SYSTEM", 1024 ), 16 );
	}

	/**
	 * Returns an int fetched and parsed from the properties table, or defaults to
	 * passed value if there isn't one or the value couldn't be parsed.
	 */
	private static final int getIntOrDefault ( final Properties props, final String key, final int defValue )
	{
		try
		{
			return Integer.parseInt( props.getProperty( key ) );
		}
		// Prolly the only exception type that could be raised here.
		catch ( final Exception ex )
		{
			// Fail silently.
		}
		return defValue;
	}

	/**
	 * Returns a boolean fetched and parsed from the properties table, or defaults
	 * to passed value if there isn't one or the value couldn't be parsed.
	 */
	private static final boolean getBoolOrDefault ( final Properties props, final String key, final boolean defValue )
	{
		try
		{
			return Boolean.parseBoolean( props.getProperty( key ) );
		}
		catch ( final Exception ex )
		{
			// Fail silently.
		}
		return defValue;
	}

	/**
	 * Loads configuration file for dustArtemis constants, fetching the path from
	 * the property {@value #CFG_FILE_PROPERTY_NAME}.
	 */
	private static final Properties loadCfgFile ()
	{
		final Properties props = new Properties();

		try
		{
			final String dir = System.getProperty( CFG_FILE_PROPERTY_NAME );
			final File file = FileSystems.getDefault().getPath( dir ).toFile();
			// Try load the constants configuration from the file.
			try ( final FileReader fr = new FileReader( file ) )
			{
				props.load( fr );
			}
		}
		catch ( final Exception ex )
		{
			// Fail silently.
		}

		return props;
	}
}
