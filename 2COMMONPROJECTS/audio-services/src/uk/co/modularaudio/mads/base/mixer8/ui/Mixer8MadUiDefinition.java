package uk.co.modularaudio.mads.base.mixer8.ui;

import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadDefinition;
import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadInstance;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNMadUiDefinition;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class Mixer8MadUiDefinition extends MixerNMadUiDefinition<Mixer8MadDefinition, Mixer8MadInstance, Mixer8MadUiInstance>
{
	private final static String MIXER8_IMAGE_PREFIX = "mixer8";
	final static Span SPAN = new Span( 4, 4 );

	public Mixer8MadUiDefinition( final BufferedImageAllocator bia,
			final Mixer8MadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		this( bia,
				definition,
				cif,
				imageRoot,
				instanceConfigToUiConfig( Mixer8MadDefinition.INSTANCE_CONFIGURATION ) );
	}

	private static Mixer8UiInstanceConfiguration instanceConfigToUiConfig(
			final MixerNInstanceConfiguration instanceConfiguration )
	{
		return new Mixer8UiInstanceConfiguration( instanceConfiguration );
	}

	private Mixer8MadUiDefinition( final BufferedImageAllocator bia,
			final Mixer8MadDefinition definition,
			final ImageFactory cif,
			final String imageRoot,
			final Mixer8UiInstanceConfiguration uiConfiguration ) throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				imageRoot,
				MIXER8_IMAGE_PREFIX,
				SPAN,
				Mixer8MadUiInstance.class,
				uiConfiguration.getChanIndexes(),
				uiConfiguration.getChanPosis(),
				uiConfiguration.getControlNames(),
				uiConfiguration.getControlTypes(),
				uiConfiguration.getControlClasses(),
				uiConfiguration.getControlBounds() );
	}
}
