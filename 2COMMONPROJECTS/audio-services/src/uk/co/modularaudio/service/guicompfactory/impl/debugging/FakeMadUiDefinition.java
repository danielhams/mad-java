package uk.co.modularaudio.service.guicompfactory.impl.debugging;

import java.awt.image.BufferedImage;

import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

class FakeMadUiDefinition extends MadUiDefinition<FakeMadDefinition, FakeMadInstance>
{

	public FakeMadUiDefinition( final BufferedImageAllocator bia, final FakeMadDefinition definition )
	{
		super( bia, definition );
	}

	@Override
	public BufferedImage getFrontBufferedImage()
	{
		return null;
	}

	@Override
	public BufferedImage getBackBufferedImage()
	{
		return null;
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