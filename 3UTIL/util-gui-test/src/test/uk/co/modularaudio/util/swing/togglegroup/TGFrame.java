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

package test.uk.co.modularaudio.util.swing.togglegroup;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacToggleButton;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class TGFrame extends JFrame
{
	private static final long serialVersionUID = 1641807448218007424L;
	
	private static final String[] toggleLabels = new String[] {
		"+",
		"=",
		"-"
	};
	
	private TestToggleGroup toggleGroup;
	
	public TGFrame()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize( new Dimension( 500, 400 ) );
		
		toggleGroup = new TestToggleGroup(
				this,
				toggleLabels,
				0 );
				
		MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint("fill");
		msh.addLayoutConstraint("gap 0");
		msh.addLayoutConstraint("insets 0");
		MigLayout layout = msh.createMigLayout();
		setLayout(layout);
		
		for( PacToggleButton tb : toggleGroup.getToggleButtons() )
		{
			Font f = tb.getFont();
			Font newFont = f.deriveFont(8.0f);
			tb.setFont( newFont );
			add( tb, "grow, wrap");
		}
	}
	
	public Dimension getViewAreaDimensions()
	{
		return getContentPane().getSize();
	}
}
