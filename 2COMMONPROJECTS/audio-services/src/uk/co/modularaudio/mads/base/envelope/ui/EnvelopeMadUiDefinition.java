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

package uk.co.modularaudio.mads.base.envelope.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadDefinition;
import uk.co.modularaudio.mads.base.envelope.mu.EnvelopeMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class EnvelopeMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<EnvelopeMadDefinition, EnvelopeMadInstance, EnvelopeMadUiInstance>
{
	private static final Span SPAN = new Span(2, 4);

	private static final int[] CHAN_INDEXES = new int[] {
		EnvelopeMadDefinition.CONSUMER_GATE,
		EnvelopeMadDefinition.CONSUMER_RETRIGGER,
		EnvelopeMadDefinition.PRODUCER_EGATE,
		EnvelopeMadDefinition.PRODUCER_EAMP };

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 50, 45 ),
		new Point( 70, 45 ),
		new Point( 130, 45 ),
		new Point( 150, 45 ),
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Display",
		"Attack From Zero",
		"Attack Wave Choice",
		"Decay Wave Choice",
		"Release Wave Choice",
		"Attack Slider",
		"Decay Slider",
		"Sustain Slider",
		"Release Slider",
		"Scale Slider"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.DISPLAY,
		ControlType.CHECKBOX,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.COMBO,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		EnvelopeDisplayUiJComponent.class,
		EnvelopeAttackFromZeroCheckboxUiJComponent.class,
		EnvelopeAttackWaveComboUiJComponent.class,
		EnvelopeDecayWaveComboUiJComponent.class,
		EnvelopeReleaseWaveComboUiJComponent.class,
		EnvelopeAttackSliderUiJComponent.class,
		EnvelopeDecaySliderUiJComponent.class,
		EnvelopeSustainSliderUiJComponent.class,
		EnvelopeReleaseSliderUiJComponent.class,
		EnvelopeTimescaleSliderUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 6, 22, 245, 90 ),			// Display
		new Rectangle( 6, 110, 100, 25 ),		// Attack From Zero
		new Rectangle( 30, 135, 50, 25 ),		// Attack Wave Choice
		new Rectangle( 85, 135, 50, 25 ),		// Decay Wave Choice
		new Rectangle( 200, 135, 50, 25 ),	// Release Wave Choice
		new Rectangle( 30, 165, 50, 85 ),		// Attack Slider
		new Rectangle( 85, 165, 50, 85 ),		// Decay Slider
		new Rectangle( 140, 165, 50, 85 ),	// Sustain Slider
		new Rectangle( 200, 165, 50, 85 ),	// Release Slider
		new Rectangle( 6, 260, 240, 25 )		// Timescale Slider
	};

	private static final Class<EnvelopeMadUiInstance> INSTANCE_CLASS = EnvelopeMadUiInstance.class;

	public EnvelopeMadUiDefinition( final BufferedImageAllocator bia, final EnvelopeMadDefinition definition, final ComponentImageFactory cif, final String imageRoot )
			throws DatastoreException
		{
			super( bia,
					cif,
					imageRoot,
					MadUIStandardBackgrounds.STD_2X4_BLUE,
					definition,
					SPAN,
					INSTANCE_CLASS,
					CHAN_INDEXES,
					CHAN_POSIS,
					CONTROL_NAMES,
					CONTROL_TYPES,
					CONTROL_CLASSES,
					CONTROL_BOUNDS );
		}
}
