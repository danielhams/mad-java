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

package uk.co.modularaudio.mads.base.mono_compressor.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class MonoCompressorMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<MonoCompressorMadDefinition, MonoCompressorMadInstance, MonoCompressorMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	private static final int[] CHAN_INDEXES = new int[] {
		MonoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT,
		MonoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT,
		MonoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 45, 100 ),
		new Point( 100, 100 ),
		new Point( 175, 100 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"ThresholdType",
		"Lookahead",
		"InMeter",
		"Threshold",
		"CompressionRatio",
		"Attack",
		"Release",
		"Attenuation",
		"MakeupGain",
		"OutMeter"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.COMBO,
		ControlType.CHECKBOX,
		ControlType.DISPLAY,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.DISPLAY,
		ControlType.SLIDER,
		ControlType.SLIDER,
		ControlType.DISPLAY
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		MonoCompressorThresholdTypeComboUiJComponent.class,
		MonoCompressorLookaheadCheckboxUiJComponent.class,
		MonoCompressorSourceSignalMeterUiComponent.class,
		MonoCompressorThresholdSliderUiJComponent.class,
		MonoCompressorRatioSliderUiJComponent.class,
		MonoCompressorAttackSliderUiJComponent.class,
		MonoCompressorReleaseSliderUiJComponent.class,
		MonoCompressorAttenuationMeterUiComponent.class,
		MonoCompressorMakeupGainSliderUiJComponent.class,
		MonoCompressorOutSignalMeterUiComponent.class
	};

	// 6 Between sliders
	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 16, 35, 80, 20 ),			// Threshold Type
		new Rectangle( 100, 35, 120, 20 ),	// Lookahead Checkbox
		new Rectangle( 6, 72, 44, 110 ),			// In Signal Meter
		new Rectangle( 56, 72, 50, 110 ),		// Threshold
		new Rectangle( 112, 72, 44, 110 ),	// Ratio
		new Rectangle( 162, 72, 50, 110 ),	// Attack
		new Rectangle( 218, 72, 50, 110 ),	// Release
		new Rectangle( 274, 72, 30, 110 ),	// Attenuation Meter
		new Rectangle( 306, 72, 50, 110 ),	// Makeup Gain
		new Rectangle( 361, 72, 44, 110 )	// Out Signal Meter
	};

	private static final Class<MonoCompressorMadUiInstance> INSTANCE_CLASS = MonoCompressorMadUiInstance.class;

	public MonoCompressorMadUiDefinition( final BufferedImageAllocator bia,
			final MonoCompressorMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
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
