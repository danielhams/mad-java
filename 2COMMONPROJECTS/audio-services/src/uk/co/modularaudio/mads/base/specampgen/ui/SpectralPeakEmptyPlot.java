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

package uk.co.modularaudio.mads.base.specampgen.ui;

import java.awt.Dimension;

import javax.swing.JPanel;

public class SpectralPeakEmptyPlot extends JPanel
{
	private static final long serialVersionUID = 7201682973826590002L;

//	private static Log log = LogFactory.getLog( NewEmptyPlot.class.getName() );

	public SpectralPeakEmptyPlot()
	{
		setBackground( SpectralAmpColours.BACKGROUND_COLOR );
		this.setMinimumSize( new Dimension( SpectralAmpGenDisplayUiJComponent.AXIS_MARKS_LENGTH, SpectralAmpGenDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
	}
}
