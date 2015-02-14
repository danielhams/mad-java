package uk.co.modularaudio.mads.base.mixer3.ui;

import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadDefinition;
import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadInstance;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNInstanceConfiguration;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNMadUiDefinition;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class Mixer3MadUiDefinition extends MixerNMadUiDefinition<Mixer3MadDefinition, Mixer3MadInstance, Mixer3MadUiInstance>
{
	private final static String MIXER3_IMAGE_PREFIX = "mixer3";
	final static Span SPAN = new Span( 2, 4 );

	public Mixer3MadUiDefinition( final BufferedImageAllocator bia,
			final Mixer3MadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		this( bia,
				definition,
				cif,
				imageRoot,
				instanceConfigToUiConfig( Mixer3MadDefinition.INSTANCE_CONFIGURATION ) );
	}

	private static Mixer3UiInstanceConfiguration instanceConfigToUiConfig(
			final MixerNInstanceConfiguration instanceConfiguration )
	{
		return new Mixer3UiInstanceConfiguration( instanceConfiguration );
	}

	private Mixer3MadUiDefinition( final BufferedImageAllocator bia,
			final Mixer3MadDefinition definition,
			final ImageFactory cif,
			final String imageRoot,
			final Mixer3UiInstanceConfiguration uiConfiguration ) throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				imageRoot,
				MIXER3_IMAGE_PREFIX,
				SPAN,
				Mixer3MadUiInstance.class,
				uiConfiguration.getChanIndexes(),
				uiConfiguration.getChanPosis(),
				uiConfiguration.getControlNames(),
				uiConfiguration.getControlTypes(),
				uiConfiguration.getControlClasses(),
				uiConfiguration.getControlBounds() );
	}
}
