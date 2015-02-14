package uk.co.modularaudio.mads.base.mixer8.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class Mixer8MadDefinition extends MixerNMadDefinition<Mixer8MadDefinition, Mixer8MadInstance>
{
	private static Log log = LogFactory.getLog( Mixer8MadDefinition.class.getName() );

	public final static String DEFINITION_ID = "mixer8";
	private final static String USER_VISIBLE_NAME = "Mixer (Eight Stereo Lanes)";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;

	private final static String CLASS_NAME="Mixer (Eight Stereo Lanes)";
	private final static String CLASS_DESC="An eight lane stereo mixer";

	private final static int NUM_MIXER_LANES = 8;

	public final static MixerNInstanceConfiguration INSTANCE_CONFIGURATION = getMixer8InstanceConfiguration();

	private static MixerNInstanceConfiguration getMixer8InstanceConfiguration()
	{
		try
		{
			final MixerNInstanceConfiguration retVal = new MixerNInstanceConfiguration( NUM_MIXER_LANES );
			return retVal;
		}
		catch( final MadProcessingException de )
		{
			log.error("Exception caught initialising instance configuration: " + de.toString(), de);
			return null;
		}
	}

	public Mixer8MadDefinition( final BaseComponentsCreationContext creationContent,
			final MadClassificationService classService )
		throws DatastoreException, RecordNotFoundException
	{
		super( DEFINITION_ID, USER_VISIBLE_NAME,
				new MadClassification( classService.findGroupById( CLASS_GROUP ),
						DEFINITION_ID,
						CLASS_NAME,
						CLASS_DESC,
						ReleaseState.RELEASED ),
				INSTANCE_CONFIGURATION );
	}

}
