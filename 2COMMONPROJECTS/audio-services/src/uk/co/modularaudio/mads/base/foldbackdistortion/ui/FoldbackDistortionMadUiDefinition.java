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

package uk.co.modularaudio.mads.base.foldbackdistortion.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionMadDefinition;
import uk.co.modularaudio.mads.base.foldbackdistortion.mu.FoldbackDistortionMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class FoldbackDistortionMadUiDefinition
	extends AbstractNonConfigurableMadUiDefinition<FoldbackDistortionMadDefinition, FoldbackDistortionMadInstance, FoldbackDistortionMadUiInstance>
{
	private static final Span SPAN = new Span(2,1);

	private static final int[] CHAN_INDEXES = new int[] {
		FoldbackDistortionMadDefinition.CONSUMER_IN_LEFT,
		FoldbackDistortionMadDefinition.CONSUMER_IN_RIGHT,
		FoldbackDistortionMadDefinition.PRODUCER_OUT_LEFT,
		FoldbackDistortionMadDefinition.PRODUCER_OUT_RIGHT
	};

	private static final Point[] CHAN_POSIS = new Point[] {
		new Point( 150, 40 ),
		new Point( 170, 40 ),
		new Point( 150, 70 ),
		new Point( 170, 70 )
	};

	private static final String[] CONTROL_NAMES = new String[] {
		"Max Foldovers",
		"Threshold"
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
		ControlType.SLIDER,
		ControlType.SLIDER
	};

	private static final Class<?>[] CONTROL_CLASSES = new Class<?>[] {
		FoldbackDistortionMaxFoldoversSliderUiJComponent.class,
		FoldbackDistortionThresholdSliderUiJComponent.class
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
		new Rectangle( 113, 53, 75, 43 ),
		new Rectangle( 113, 3, 75, 43 )
	};

	private static final Class<FoldbackDistortionMadUiInstance> INSTANCE_CLASS = FoldbackDistortionMadUiInstance.class;

	public FoldbackDistortionMadUiDefinition( final BufferedImageAllocator bia,
			final FoldbackDistortionMadDefinition definition,
			final ComponentImageFactory cif,
			final String imageRoot )
		throws DatastoreException
	{
		super( bia,
				cif,
				imageRoot,
				MadUIStandardBackgrounds.STD_2X1_METALLIC,
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
