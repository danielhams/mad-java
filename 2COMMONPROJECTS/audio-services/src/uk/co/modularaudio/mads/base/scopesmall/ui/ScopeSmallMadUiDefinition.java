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

package uk.co.modularaudio.mads.base.scopesmall.ui;

import java.awt.Color;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.scopen.ui.ScopeNMadUiDefinition;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNUiInstanceConfiguration;
import uk.co.modularaudio.mads.base.scopen.ui.display.ScopeWaveDisplay;
import uk.co.modularaudio.mads.base.scopesmall.mu.ScopeSmallMadDefinition;
import uk.co.modularaudio.mads.base.scopesmall.mu.ScopeSmallMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class ScopeSmallMadUiDefinition
	extends	ScopeNMadUiDefinition<ScopeSmallMadDefinition, ScopeSmallMadInstance, ScopeSmallMadUiInstance>
{
	private static final Span SPAN = new Span( 2, 4 );

	private static final String[] CONTROL_NAMES = new String[] {
		"CaptureLength",
		"TriggerChoice",
		"RepetitionChoice",
		"Recapture",
		"WaveDisplay",
		"SaveButton"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.BUTTON,
		ControlType.CUSTOM,
		ControlType.CUSTOM
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		ScopeSmallCaptureLengthSliderUiJComponent.class,
		ScopeSmallTriggerChoiceUiJComponent.class,
		ScopeSmallRepetitionsChoiceUiJComponent.class,
		ScopeSmallRecaptureButtonUiJComponent.class,
		ScopeSmallSaveImageButtonUiJComponent.class,
		ScopeSmallDisplayUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 116,   3, 434,  30 ),		// Capture Time
		new Rectangle(  16,  40, 170,  30 ),		// Trigger Choice
		new Rectangle( 200,  40, 165,  30 ),		// Repetition Choice
		new Rectangle( 382,  40, 110,  30 ),		// Recapture
		new Rectangle( 510,  40,  35,  30 ),		// Save Image
		new Rectangle(   6,  75, 544, 232 )			// Scope Display
	};

	private static final Class<ScopeSmallMadUiInstance> INSTANCE_CLASS = ScopeSmallMadUiInstance.class;

	protected static final int NUM_AMP_MARKS = 11;
	protected static final int NUM_TIME_MARKS = 11;
	protected static final int NUM_AMP_DECIMAL_PLACES = 1;

	private static final Color[] VIS_COLOURS = new Color[ScopeWaveDisplay.MAX_VIS_COLOURS];

	static
	{
		VIS_COLOURS[0] = Color.decode( "#d3d3d3" );
		VIS_COLOURS[1] = Color.decode( "#d31b00" );
		VIS_COLOURS[2] = Color.decode( "#d38c00" );
		VIS_COLOURS[3] = Color.decode( "#c1d300" );
		VIS_COLOURS[4] = Color.decode( "#08af00" );
	}

	public ScopeSmallMadUiDefinition( final BufferedImageAllocator bia,
			final ScopeSmallMadDefinition definition,
			final ComponentImageFactory cif )
		throws DatastoreException, MadProcessingException
	{
		this( bia,
				definition,
				cif,
				new ScopeNUiInstanceConfiguration( ScopeSmallMadDefinition.INSTANCE_CONFIGURATION,
						VIS_COLOURS ) );
	}

	public ScopeSmallMadUiDefinition( final BufferedImageAllocator bia,
			final ScopeSmallMadDefinition definition,
			final ComponentImageFactory cif,
			final ScopeNUiInstanceConfiguration uiInstanceConfiguration )
		throws DatastoreException
	{
		super( bia,
				definition,
				cif,
				MadUIStandardBackgrounds.STD_2X4_BLUE,
				SPAN,
				INSTANCE_CLASS,
				uiInstanceConfiguration,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
