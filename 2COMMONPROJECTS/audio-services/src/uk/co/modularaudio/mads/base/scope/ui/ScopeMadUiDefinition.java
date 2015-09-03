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

package uk.co.modularaudio.mads.base.scope.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.scope.mu.ScopeMadDefinition;
import uk.co.modularaudio.mads.base.scope.mu.ScopeMadInstance;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUIStandardBackgrounds;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;

public class ScopeMadUiDefinition extends
		AbstractNonConfigurableMadUiDefinition<ScopeMadDefinition, ScopeMadInstance, ScopeMadUiInstance>
{
	private static final Span SPAN = new Span( 2, 4 );

	private static final int[] CHAN_INDEXES = new int[] {
		ScopeMadDefinition.SCOPE_TRIGGER,
		ScopeMadDefinition.SCOPE_INPUT_0,
		ScopeMadDefinition.SCOPE_INPUT_1,
		ScopeMadDefinition.SCOPE_INPUT_2,
		ScopeMadDefinition.SCOPE_INPUT_3
	};

	private static final Point[] CHAN_POSITIONS = new Point[] {
		new Point( 120,  70 ),
		new Point( 140,  70 ),
		new Point( 140,  90 ),
		new Point( 140, 110 ),
		new Point( 140, 130 ),
	};

	private static final String[] CONTROL_NAMES = new String[] {
	};

	private static final ControlType[] CONTROL_TYPES = new ControlType[] {
	};

	private static final Class<?>[] COTNROL_CLASSES = new Class<?>[] {
	};

	private static final Rectangle[] CONTROL_BOUNDS = new Rectangle[] {
	};

	private static final Class<ScopeMadUiInstance> INSTANCE_CLASS = ScopeMadUiInstance.class;

	public ScopeMadUiDefinition( final BufferedImageAllocator bia,
			final ScopeMadDefinition definition,
			final ComponentImageFactory cif )
		throws DatastoreException
	{
		super( bia,
				cif,
				MadUIStandardBackgrounds.STD_2X4_RACINGGREEN,
				definition,
				SPAN,
				INSTANCE_CLASS,
				CHAN_INDEXES,
				CHAN_POSITIONS,
				CONTROL_NAMES,
				CONTROL_TYPES,
				COTNROL_CLASSES,
				CONTROL_BOUNDS );
	}
}
