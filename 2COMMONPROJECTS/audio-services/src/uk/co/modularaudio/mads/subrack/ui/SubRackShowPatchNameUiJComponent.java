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
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacLabel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SubRackShowPatchNameUiJComponent extends PacLabel
		implements IMadUiControlInstance<SubRackMadDefinition, SubRackMadInstance, SubRackMadUiInstance>
{
	private static final long serialVersionUID = 7488560789053700984L;

	private final SubRackMadInstance instance;
	private final RackService rackService;

	public SubRackShowPatchNameUiJComponent( final SubRackMadDefinition definition,
			final SubRackMadInstance instance,
			final SubRackMadUiInstance uiInstance,
			final SubRackShowPatchNameUiControlDefinition def )
	{
		this.instance = instance;
		this.rackService = uiInstance.getRackService();

		this.setOpaque( true );

//		Font f = getFont().deriveFont( 9.0f );
		final Font f = getFont();
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
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// This is ugly :-/
		// Really shouldn't be doing this on the display tick
		// but should be done with dirty listeners

		// log.debug("Received display tick");
		final RackDataModel subRackDataModel = instance.getSubRackDataModel();

		final String currentPatchName = instance.getCurrentPatchName();
		final String currentLabelName = getText();

		if( rackService.isRackDirty( subRackDataModel ) )
		{
			if( currentLabelName.length() == 0 ||
					currentLabelName.length() >= 1 && currentLabelName.charAt( currentLabelName.length() - 1 ) != '*' )
			{
				final String newPatchName = currentPatchName + " *";
				// Setting the path to nothing means that when the patch is saved we inline the
				// sub rack as a local sub rack.
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
