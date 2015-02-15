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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class StereoCompressorMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<StereoCompressorMadDefinition, StereoCompressorMadInstance, StereoCompressorMadUiInstance>
{
	private static final Span SPAN = new Span(2,4);

	private static final int[] CHAN_INDEXES = new int[] {
		StereoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT,
		StereoCompressorMadDefinition.CONSUMER_IN_WAVE_RIGHT,
		StereoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT,
		StereoCompressorMadDefinition.CONSUMER_IN_COMP_RIGHT,
		StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT,
		StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_RIGHT,
		StereoCompressorMadDefinition.PRODUCER_OUT_DRY_LEFT,
		StereoCompressorMadDefinition.PRODUCER_OUT_DRY_RIGHT
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point(  75, 160 ),
		new Point(  95, 160 ),
		new Point( 150, 160 ),
		new Point( 170, 160 ),
		new Point( 390, 160 ),
		new Point( 410, 160 ),
		new Point( 465, 160 ),
		new Point( 485, 160 )
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
		StereoCompressorThresholdTypeComboUiJComponent.class,
		StereoCompressorLookaheadCheckboxUiJComponent.class,
		StereoCompressorSourceSignalMeterUiComponent.class,
		StereoCompressorThresholdSliderUiJComponent.class,
		StereoCompressorRatioSliderUiJComponent.class,
		StereoCompressorAttackSliderUiJComponent.class,
		StereoCompressorReleaseSliderUiJComponent.class,
		StereoCompressorAttenuationMeterUiComponent.class,
		StereoCompressorMakeupGainSliderUiJComponent.class,
		StereoCompressorOutSignalMeterUiComponent.class
	};

	// 6 Between sliders
	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 200,  3,  80,  30 ),			// Threshold Type
		new Rectangle( 280,  3, 120,  30 ),			// Lookahead Checkbox
		new Rectangle(  16, 40,  50, 250 ),			// In Signal Meter
		new Rectangle(  75, 40,  70, 250 ),			// Threshold
		new Rectangle( 145, 40,  70, 250 ),			// Ratio
		new Rectangle( 210, 40,  70, 250 ),			// Attack
		new Rectangle( 280, 40,  70, 250 ),			// Release
		new Rectangle( 350, 40,  40, 250 ),			// Attenuation Meter
		new Rectangle( 420, 40,  70, 250 ),			// Makeup Gain
		new Rectangle( 490, 40,  50, 250 )			// Out Signal Meter
	};

	private static final Class<StereoCompressorMadUiInstance> INSTANCE_CLASS = StereoCompressorMadUiInstance.class;

	public StereoCompressorMadUiDefinition( final BufferedImageAllocator bia,
			final StereoCompressorMadDefinition definition,
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
				CHAN_POSITIONS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				CONTROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
