package uk.co.modularaudio.mads.base.mixer3.mu;

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

public class Mixer3MadDefinition extends MixerNMadDefinition<Mixer3MadDefinition, Mixer3MadInstance>
{
	private static Log log = LogFactory.getLog( Mixer3MadDefinition.class.getName() );

	public final static String DEFINITION_ID = "mixer3";
	private final static String USER_VISIBLE_NAME = "Mixer3";

	private final static String CLASS_GROUP = MadClassificationService.SOUND_PROCESSING_GROUP_ID;

	private final static String CLASS_NAME="Mixer 3";
	private final static String CLASS_DESC="A 3 lane stereo mixer";

	private final static int NUM_MIXER_LANES = 3;

	public final static MixerNInstanceConfiguration INSTANCE_CONFIGURATION = getMixer3InstanceConfiguration();

	private static MixerNInstanceConfiguration getMixer3InstanceConfiguration()
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

	public Mixer3MadDefinition( final BaseComponentsCreationContext creationContent,
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
