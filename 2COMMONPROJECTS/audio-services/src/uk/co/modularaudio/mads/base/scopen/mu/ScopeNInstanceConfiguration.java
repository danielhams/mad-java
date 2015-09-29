package uk.co.modularaudio.mads.base.scopen.mu;

import uk.co.modularaudio.mads.base.scopen.ui.display.ScopeWaveDisplay;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class ScopeNInstanceConfiguration
{
	public static final int TRIGGER_INDEX = 0;

	private final int numScopeChannels;
	private final int numTotalChannels;

	private final String[] channelNames;
	private final MadChannelType[] channelTypes;
	private final MadChannelDirection[] channelDirections;
	private final MadChannelPosition[] channelPositions;

	public ScopeNInstanceConfiguration( final int numScopeChannels )
	 throws MadProcessingException
	{
		if( numScopeChannels > ScopeWaveDisplay.MAX_VIS_COLOURS )
		{
			throw new MadProcessingException( "Num scope channels currently unsupported" );
		}
		this.numScopeChannels = numScopeChannels;
		this.numTotalChannels = numScopeChannels + 1;

		channelNames = new String[ numTotalChannels ];
		channelTypes = new MadChannelType[ numTotalChannels ];
		channelDirections = new MadChannelDirection[ numTotalChannels ];
		channelPositions = new MadChannelPosition[ numTotalChannels ];

		// First channel, the trigger
		channelNames[0] = "Input Trigger";
		channelTypes[0] = MadChannelType.CV;
		channelDirections[0] = MadChannelDirection.CONSUMER;
		channelPositions[0] = MadChannelPosition.MONO;

		for( int i = 1 ; i <= numScopeChannels ; ++i )
		{
			channelNames[i] = "Input Signal " + i;
			channelTypes[i] = MadChannelType.CV;
			channelDirections[i] = MadChannelDirection.CONSUMER;
			channelPositions[i] = MadChannelPosition.MONO;
		}
	}

	public ScopeNInstanceConfiguration( final ScopeNInstanceConfiguration ic ) throws MadProcessingException
	{
		this( ic.numScopeChannels );
	}

	public int getNumScopeChannels()
	{
		return numScopeChannels;
	}

	public int getNumTotalChannels()
	{
		return numTotalChannels;
	}

	public String[] getChannelNames()
	{
		return channelNames;
	}

	public MadChannelType[] getChannelTypes()
	{
		return channelTypes;
	}

	public MadChannelDirection[] getChannelDirections()
	{
		return channelDirections;
	}

	public MadChannelPosition[] getChannelPositions()
	{
		return channelPositions;
	}


}
