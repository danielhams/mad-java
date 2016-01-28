package uk.co.modularaudio.service.audioanalysis.impl;

import uk.co.modularaudio.util.exception.DatastoreException;

public class AnalysisException extends DatastoreException
{
	private static final long serialVersionUID = 1150661223022210073L;

	public AnalysisException( final String cause )
	{
		super( cause );
	}

	public AnalysisException( final String cause,
			final Exception root )
	{
		super( cause, root );
	}

}
