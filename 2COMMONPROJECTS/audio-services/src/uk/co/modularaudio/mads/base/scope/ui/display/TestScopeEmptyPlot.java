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

package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;

public class TestScopeEmptyPlot extends JPanel
{
	private static final long serialVersionUID = -290572139583210940L;

	public TestScopeEmptyPlot( final Color testColor )
	{
		setBackground( testColor );
		this.setMinimumSize( new Dimension( ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH, ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}
}
