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

package uk.co.modularaudio.mads.base.stereo_gate.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadDefinition;
import uk.co.modularaudio.mads.base.stereo_gate.mu.StereoGateMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class StereoGateMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<StereoGateMadDefinition, StereoGateMadInstance, StereoGateMadUiInstance>
{
	private static final Span span = new Span(2,4);
	
	private static final int[] uiChannelInstanceIndexes = new int[] {
		StereoGateMadDefinition.CONSUMER_IN_WAVE_LEFT,
		StereoGateMadDefinition.CONSUMER_IN_WAVE_RIGHT,
		StereoGateMadDefinition.PRODUCER_OUT_CV_GATE,
		StereoGateMadDefinition.PRODUCER_OUT_CV_OVERDRIVE
	};
	
	private static final Point[] uiChannelPositions = new Point[] {
		new Point( 45, 80 ),
		new Point( 65, 80 ),
		new Point( 45, 120 ),
		new Point( 65, 120 )
	};
	
	private static final String[] uiControlNames = new String[] {
		"ThresholdType",
		"Threshold"
	};
	
	private static final ControlType[] uiControlTypes = new ControlType[] {
		ControlType.COMBO,
		ControlType.SLIDER
	};
	
	private static final Class<?>[] uiControlClasses = new Class<?>[] {
		StereoGateThresholdTypeComboUiJComponent.class,
		StereoGateThresholdSliderUiJComponent.class
	};
	
	private static final Rectangle[] uiControlBounds = new Rectangle[] {
		new Rectangle( 16, 35, 80, 20 ),
		new Rectangle( 6, 72, 60, 110 )
	};
	
	private static final Class<StereoGateMadUiInstance> instanceClass = StereoGateMadUiInstance.class;
	
	public StereoGateMadUiDefinition( BufferedImageAllocator bia, StereoGateMadDefinition definition, ComponentImageFactory cif, String imageRoot )
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
