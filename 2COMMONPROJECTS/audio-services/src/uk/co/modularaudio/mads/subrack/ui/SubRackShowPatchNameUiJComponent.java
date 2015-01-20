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

package uk.co.modularaudio.mads.subrack.ui;

import java.awt.Font;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacLabel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SubRackShowPatchNameUiJComponent extends PacLabel
		implements IMadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>
{
	private static final long serialVersionUID = 7488560789053700984L;

	private SubRackMadInstance instance = null;

	public SubRackShowPatchNameUiJComponent( SubRackMadDefinition definition,
			SubRackMadInstance instance,
			SubRackMadUiInstance uiInstance,
			SubRackShowPatchNameUiControlDefinition def )
	{
		this.instance = instance;

		this.setOpaque( true );

//		Font f = getFont().deriveFont( 9.0f );
		Font f = getFont();
		setFont( f );
		
//		this.setBackground( Color.WHITE );
//		this.setBorder( new LineBorder( Color.BLACK ) );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
		RackDataModel subRackDataModel = instance.getSubRackDataModel();

		String currentPatchName = instance.getCurrentPatchName();
		String currentLabelName = getText();
		
		if( subRackDataModel.isDirty() )
		{
			if( currentLabelName.length() == 0 ||
					currentLabelName.length() >= 1 && currentLabelName.charAt( currentLabelName.length() - 1 ) != '*' )
			{
				String newPatchName = currentPatchName + " *";
				subRackDataModel.setPath( "" );
				if( !currentLabelName.equals( newPatchName ) )
				{
					setText( newPatchName );
				}
			}
		}
		else
		{
			if (!currentPatchName.equals( currentLabelName ))
			{
				setText( currentPatchName );
			}
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

}
