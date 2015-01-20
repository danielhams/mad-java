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

package test.uk.co.modularaudio.util.audio.mad.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import test.uk.co.modularaudio.util.audio.mad.units.singlechanvolume.SingleChannelVolumeMadDefinition;
import test.uk.co.modularaudio.util.audio.mad.units.stereotee.StereoTeeMadDefinition;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.factory.IMadDefinitionFactory;

public class TestMadDefinitionFactory implements
		IMadDefinitionFactory
{
	private List<MadDefinition<?,?>> myDefs = new ArrayList<MadDefinition<?,?>>();

	public TestMadDefinitionFactory()
	{
		MadClassificationGroup classificationGroup = new MadClassificationGroup( Visibility.PUBLIC, "Routing" );
		MadClassification stClassification = new MadClassification( classificationGroup,
				"stereo_audio_splitter",
				"Stereo Audio Splitter",
				"This unit allows the splitting of a stereo signal into two separate audio paths",
				ReleaseState.RELEASED );
		myDefs.add( new StereoTeeMadDefinition( stClassification )  );
		MadClassificationGroup audioStudioGroup = new MadClassificationGroup( Visibility.PUBLIC, "Audio Studio Tools" );
		MadClassification scvClassification = new MadClassification(  audioStudioGroup,
				"single_channel_volume",
				"Single Channel Volume",
				"Change the volume of a single channel of audio",
				ReleaseState.ALPHA );
		myDefs.add( new SingleChannelVolumeMadDefinition( scvClassification ) );
	}

	@Override
	public Collection<MadDefinition<?,?>> listDefinitions()
	{
		return myDefs;
	}

}
