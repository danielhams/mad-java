package uk.co.modularaudio.service.guicompfactory.impl.debugging;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class FakeRackComponent extends RackComponent
{
	final static String fakeStr = "fake";
	private final static MadClassificationGroup classGroup = new MadClassificationGroup( Visibility.PUBLIC, fakeStr );
	final static Span SPAN = new Span(2,2);

	public FakeRackComponent( final String name, final FakeMadInstance mi, final FakeMadUiInstance mui )
	{
		super( name, mi, mui );
	}


	public static FakeRackComponent createInstance( final BufferedImageAllocator bia ) throws MadProcessingException, DatastoreException
	{
		final MadClassification fakeClass = new MadClassification( classGroup, fakeStr, fakeStr, fakeStr, ReleaseState.RELEASED );
		final FakeMadDefinition md = new FakeMadDefinition( fakeClass );
		final FakeMadInstance mi = new FakeMadInstance( md );
		final FakeMadUiDefinition mud = new FakeMadUiDefinition( bia, md );
		final FakeMadUiInstance mui = (FakeMadUiInstance) mud.createNewUiInstance( mi );
		return new FakeRackComponent( "fakey", mi, mui );
	}
}
