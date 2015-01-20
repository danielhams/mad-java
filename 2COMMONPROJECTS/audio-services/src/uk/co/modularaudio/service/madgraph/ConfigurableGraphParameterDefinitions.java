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

package uk.co.modularaudio.service.madgraph;

import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public class ConfigurableGraphParameterDefinitions
{
	public static MadParameterDefinition numInputAudioChannelsParameterDef = new MadParameterDefinition( "num_input_audio_channels",
			"How many input audio channels");
	public static MadParameterDefinition numOutputAudioChannelsParameterDef = new MadParameterDefinition( "num_output_audio_channels",
			"How many output audio channels");

	public static MadParameterDefinition numInputCVChannelsParameterDef = new MadParameterDefinition( "num_input_cv_channels",
			"How many input cv channels");
	public static MadParameterDefinition numOutputCVChannelsParameterDef = new MadParameterDefinition( "num_output_cv_channels",
			"How many output cv channels");
	
	public static MadParameterDefinition numInputNoteChannelsParameterDef = new MadParameterDefinition( "num_input_note_channels",
			"How many input note channels");
	public static MadParameterDefinition numOutputNoteChannelsParameterDef = new MadParameterDefinition( "num_output_note_channels",
			"How many output note channels");
}
