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
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class EnvelopeMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<EnvelopeMadDefinition, EnvelopeMadInstance, EnvelopeMadUiInstance>
{
	private static final Span span = new Span(2, 4);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		EnvelopeMadDefinition.CONSUMER_GATE,
		EnvelopeMadDefinition.CONSUMER_RETRIGGER,
		EnvelopeMadDefinition.PRODUCER_EGATE,
		EnvelopeMadDefinition.PRODUCER_EAMP };
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 50, 45 ),
		new Point( 70, 45 ),
		new Point( 130, 45 ),
		new Point( 150, 45 ),
	};
	
	private static final String[] uiControlNames = new String[] {
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
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
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
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
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
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
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
	
	private static final Class<EnvelopeMadUiInstance> instanceClass = EnvelopeMadUiInstance.class;

	public EnvelopeMadUiDefinition( BufferedImageAllocator bia, EnvelopeMadDefinition definition, ComponentImageFactory cif, String imageRoot )
			throws DatastoreException
		{
			super( bia, definition, cif, imageRoot,
					span,
					instanceClass,
					uiChannelInstanceIndexes,
					uiChannelPositions,
					uiControlNames,
					uiControlTypes,
					uiControlClasses,
					uiControlBounds );
		}
}
