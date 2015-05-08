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

package uk.co.modularaudio.service.gui.impl.guirackpanel.sub;

import java.awt.CardLayout;
import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.impl.racktable.RackSidesEmptyCellPainter;
import uk.co.modularaudio.service.gui.impl.racktable.RackTable;
import uk.co.modularaudio.service.gui.impl.racktable.back.RackTableWithLinks;

public class GuiRackAndWiresCardPanel extends JPanel
{
	private static final long serialVersionUID = -562552338640474197L;

	private static final String ID_FRONT = "Front";
	private static final String ID_BACK = "Back";

	private final CardLayout frontBackCardLayout;

	private boolean isFront = true;

	public GuiRackAndWiresCardPanel( final RackTable frontAudioComponentTable,
			final RackTableWithLinks backAudioComponentTable )
	{
		frontBackCardLayout = new CardLayout();
		this.setLayout( frontBackCardLayout );
		this.add( frontAudioComponentTable.getJComponent(), ID_FRONT );
		this.add( backAudioComponentTable.getJComponent(), ID_BACK );
		frontBackCardLayout.addLayoutComponent( frontAudioComponentTable.getJComponent(), ID_FRONT );
		frontBackCardLayout.addLayoutComponent( backAudioComponentTable.getJComponent(), ID_BACK );

		frontBackCardLayout.show( this, ID_FRONT );

		setBackground( RackSidesEmptyCellPainter.LOWLIGHT_COLOR );
	}

	public void rotateRack()
	{
		if( isFront )
		{
			frontBackCardLayout.show( this, ID_BACK );
			isFront = false;
		}
		else
		{
			frontBackCardLayout.show( this, ID_FRONT );
			isFront = true;
		}
	}

	public boolean isFrontShowing()
	{
		return isFront;
	}

}
