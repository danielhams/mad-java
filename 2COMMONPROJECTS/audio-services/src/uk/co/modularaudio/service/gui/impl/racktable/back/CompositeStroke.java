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

package uk.co.modularaudio.service.gui.impl.racktable.back;

import java.awt.Shape;
import java.awt.Stroke;

public class CompositeStroke implements Stroke {
	private final Stroke stroke1, stroke2;

	public CompositeStroke( final Stroke stroke1, final Stroke stroke2 ) {
		this.stroke1 = stroke1;
		this.stroke2 = stroke2;
	}

	@Override
	public Shape createStrokedShape( final Shape shape ) {
		return stroke2.createStrokedShape( stroke1.createStrokedShape( shape ) );
	}
}
