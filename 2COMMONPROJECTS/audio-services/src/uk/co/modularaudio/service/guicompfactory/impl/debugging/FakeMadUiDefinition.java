package uk.co.modularaudio.service.guicompfactory.impl.debugging;

import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

class FakeMadUiDefinition extends MadUiDefinition<FakeMadDefinition, FakeMadInstance>
{

	public FakeMadUiDefinition( final BufferedImageAllocator bia,
			final ImageFactory cif,
			final String imageRoot,
			final String imagePrefix,
			final FakeMadDefinition definition ) throws DatastoreException
	{
		super( bia, cif, imageRoot, imagePrefix, definition );
	}

	@Override
	public AbstractMadUiInstance<?, ?> createNewUiInstance( final FakeMadInstance instance ) throws DatastoreException
	{
		return new FakeMadUiInstance( instance, this );
	}

	@Override
	public Span getCellSpan()
	{
		return FakeRackComponent.SPAN;
	}
}