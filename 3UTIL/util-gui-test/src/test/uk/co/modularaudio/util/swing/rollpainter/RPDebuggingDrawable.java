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

package test.uk.co.modularaudio.util.swing.rollpainter;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class RPDebuggingDrawable extends PacPanel
{
	private static final long serialVersionUID = 4875042076834975578L;
	
//	private RPCanvas canvas;
	
	public RPDebuggingDrawable(RPCanvas canvas)
	{
//		this.canvas = canvas;
		
		MigLayoutStringHelper mlh = new MigLayoutStringHelper();
		mlh.addLayoutConstraint( "fill" );
		mlh.addLayoutConstraint( "insets 10" );
		mlh.addLayoutConstraint( "gap 0" );
		MigLayout layout = mlh.createMigLayout();
		this.setLayout( layout );
		this.add( canvas, "grow" );
	}
	
	

}
