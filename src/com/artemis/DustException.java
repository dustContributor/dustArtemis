package com.artemis;

final class DustException extends RuntimeException
{
	private static final String ERROR_TAG_START = "[dustArtemis:";
	private static final String ERROR_TAG_END = ":ERROR] ";

	private static final long serialVersionUID = 1L;

	DustException ( final Class<?> sourceType, final String message )
	{
		super( makeMessage( sourceType, message ) );
	}

	DustException ( final Object source, final String message )
	{
		super( makeMessage( source, message ) );
	}

	DustException ( final Class<?> sourceType, final String message, final Throwable throwable )
	{
		super( makeMessage( sourceType, message ), throwable );
	}

	DustException ( final Object source, final String message, final Throwable throwable )
	{
		super( makeMessage( source, message ), throwable );
	}

	private static final String makeMessage ( final Class<?> sourceType, final String message )
	{
		return makeMessage( (sourceType != null) ? sourceType.getSimpleName() : "null", message );
	}

	private static final String makeMessage ( final Object source, final String message )
	{
		return makeMessage( (source != null) ? source.getClass().getSimpleName() : "null", message );
	}

	private static final String makeMessage ( final String sourceName, final String message )
	{
		final StringBuilder sb = new StringBuilder( 128 );

		sb.append( ERROR_TAG_START );
		sb.append( sourceName );
		sb.append( ERROR_TAG_END );
		sb.append( message );

		return sb.toString();
	}

}
