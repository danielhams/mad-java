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

package uk.co.modularaudio.mads.base.scopen.ui;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.scopen.mu.ScopeNMadDefinition;
import uk.co.modularaudio.mads.base.scopen.mu.ScopeNMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public class ScopeNMadUiDefinition<D extends ScopeNMadDefinition<D, I>,
I extends ScopeNMadInstance<D, I>,
U extends ScopeNMadUiInstance<D,I>>
	extends AbstractNonConfigurableMadUiDefinition<D, I, U>
{
	public ScopeNMadUiDefinition( final BufferedImageAllocator bia,
			final D definition,
			final ImageFactory cif,
			final String imagePrefix,
			final Span span,
			final Class<U> instanceClass,
			final int[] uiChannelInstanceIndexes,
			final Point[] uiChannelPositions,
			final String[] uiControlNames,
			final ControlType[] uiControlTypes,
			final Class<?>[] uiControlClasses,
			final Rectangle[] uiControlBounds )
		throws DatastoreException
	{
		super( bia,
				cif,
				imagePrefix,
				definition,
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
