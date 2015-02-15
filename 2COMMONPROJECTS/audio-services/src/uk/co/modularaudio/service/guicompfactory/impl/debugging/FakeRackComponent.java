/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.service.guicompfactory.impl.debugging;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class FakeRackComponent extends RackComponent
{
	final static String FAKE_STR = "fake";
	private final static MadClassificationGroup CLASS_GROUP = new MadClassificationGroup( Visibility.PUBLIC, FAKE_STR );
	final static Span SPAN = new Span(2,2);

	public FakeRackComponent( final String name, final FakeMadInstance mi, final FakeMadUiInstance mui )
	{
		super( name, mi, mui );
	}


	public static FakeRackComponent createInstance( final BufferedImageAllocator bia,
			final ImageFactory imageFactory,
			final String imageRoot,
			final String imagePrefix
			) throws MadProcessingException, DatastoreException
	{
		final MadClassification fakeClass = new MadClassification( CLASS_GROUP, FAKE_STR, FAKE_STR, FAKE_STR, ReleaseState.RELEASED );
		final FakeMadDefinition md = new FakeMadDefinition( fakeClass );
		final FakeMadInstance mi = new FakeMadInstance( md );
		final FakeMadUiDefinition mud = new FakeMadUiDefinition( bia, imageFactory, imageRoot, imagePrefix, md );
		final FakeMadUiInstance mui = (FakeMadUiInstance) mud.createNewUiInstance( mi );
		return new FakeRackComponent( "fakey", mi, mui );
	}
}
