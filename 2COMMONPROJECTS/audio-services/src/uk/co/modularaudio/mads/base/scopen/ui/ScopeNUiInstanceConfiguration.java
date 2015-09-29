package uk.co.modularaudio.mads.base.scopen.ui;

import java.awt.Color;
import java.awt.Point;

import uk.co.modularaudio.mads.base.scopen.mu.ScopeNInstanceConfiguration;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class ScopeNUiInstanceConfiguration extends ScopeNInstanceConfiguration
{

	private final int[] chanIndexes;
	private final Point[] chanPosis;

//	new Point( 120,  70 ),
//	new Point( 140,  70 ),
//	new Point( 140,  90 ),
//	new Point( 140, 110 ),
//	new Point( 140, 130 ),

	private final static int TRIGGER_X_OFFSET = 120;
	private final static int CHAN_Y_START = 70;
	private final static int SCOPE_CHAN_X_OFFSET = 140;
	private final static int CHAN_Y_DELTA = 20;

	private final Color[] visColours;

	public ScopeNUiInstanceConfiguration( final ScopeNInstanceConfiguration instanceConfiguration,
			final Color[] visColours )
		throws MadProcessingException
	{
		super( instanceConfiguration );
		final int numScopeChannels = instanceConfiguration.getNumScopeChannels();
		final int numTotalChannels = instanceConfiguration.getNumTotalChannels();

		chanIndexes = new int[ numTotalChannels ];
		chanPosis = new Point[ numTotalChannels ];

		chanIndexes[0] = ScopeNInstanceConfiguration.TRIGGER_INDEX;
		chanPosis[0] = new Point( TRIGGER_X_OFFSET, CHAN_Y_START );

		for( int i = 1 ; i <= numScopeChannels ; ++i )
		{
			chanIndexes[i] = i;
			chanPosis[i] = new Point( SCOPE_CHAN_X_OFFSET, CHAN_Y_START + ((i-1)*CHAN_Y_DELTA) );
		}

		this.visColours = visColours;
	}

	public int[] getChanIndexes()
	{
		return chanIndexes;
	}

	public Point[] getChanPosis()
	{
		return chanPosis;
	}

	public Color[] getVisColours()
	{
		return this.visColours;
	}

}
