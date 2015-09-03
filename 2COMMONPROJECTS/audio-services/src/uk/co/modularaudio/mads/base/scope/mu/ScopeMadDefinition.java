package uk.co.modularaudio.mads.base.scope.mu;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.helper.AbstractNonConfigurableMadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class ScopeMadDefinition extends AbstractNonConfigurableMadDefinition<ScopeMadDefinition, ScopeMadInstance>
{
	// Indexes into the channels
	public final static int SCOPE_TRIGGER = 0;
	public final static int SCOPE_INPUT_0 = 1;
	public final static int SCOPE_INPUT_1 = 2;
	public final static int SCOPE_INPUT_2 = 3;
	public final static int SCOPE_INPUT_3 = 4;
	public final static int NUM_CHANNELS = 5;

	public static final String DEFINITION_ID = "scope";

	private final static String USER_VISIBLE_NAME = "Scope";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_ANALYSIS_GROUP_ID;
	private final static String CLASS_NAME = "Scope";
	private final static String CLASS_DESC = "A multi-channel oscilloscope";

	// These must match the channel indexes given above
	private final static String[] CHAN_NAMES = new String[] {
		"Input Trigger",
		"Input Signal 1",
		"Input Signal 2",
		"Input Signal 3",
		"Input Signal 4"
	};

	private final static MadChannelType[] CHAN_TYPES = new MadChannelType[] {
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV,
		MadChannelType.CV
	};

	private final static MadChannelDirection[] CHAN_DIRS = new MadChannelDirection[] {
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER,
		MadChannelDirection.CONSUMER
	};

	private final static MadChannelPosition[] CHAN_POSI = new MadChannelPosition[] {
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO,
		MadChannelPosition.MONO
	};

	public ScopeMadDefinition( final BaseComponentsCreationContext creationContext,
			final MadClassificationService classService )
		throws RecordNotFoundException, DatastoreException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				new ScopeIOQueueBridge(),
				NUM_CHANNELS,
				CHAN_NAMES,
				CHAN_TYPES,
				CHAN_DIRS,
				CHAN_POSI );

	}
}
