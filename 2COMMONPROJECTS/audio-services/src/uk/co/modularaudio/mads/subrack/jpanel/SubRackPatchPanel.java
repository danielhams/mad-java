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

package uk.co.modularaudio.mads.subrack.jpanel;

import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.SubrackTab;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.GuiConstants;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;

public class SubRackPatchPanel extends JPanel implements SubrackTab
{
	private static final long serialVersionUID = 637534081127536206L;

	private String title = "*empty*";
	private final SubRackMadUiInstance uiInstance;
	private final RackModelRenderingComponent rmrc;

	private final RackService rackService;

	private final HashSet<SubrackTitleListener> titleListeners = new HashSet<SubrackTitleListener>();

	public SubRackPatchPanel( final SubRackMadUiInstance uiInstance, final RackModelRenderingComponent rmrc,
			final RackService rackService )
	{
		this.uiInstance = uiInstance;
		this.rmrc = rmrc;
		this.rackService = rackService;
		final MigLayout migLayout = new MigLayout( "insets 0, gap 0, fill");
		this.setLayout( migLayout );
		add( rmrc.getJComponent(), "");

		this.setSize( GuiConstants.GUI_DEFAULT_DIMENSIONS );
		this.setMinimumSize( GuiConstants.GUI_MINIMUM_DIMENSIONS );
	}

	public void setRackDataModel( final RackDataModel subRackDataModel )
	{
		this.title = rackService.getRackName( subRackDataModel );
		rmrc.setRackDataModel( subRackDataModel );
	}

	@Override
	public JComponent getJComponent()
	{
		return this;
	}

	@Override
	public String getTitle()
	{
		return( title );
	}

	public void setTitle( final String newTitle )
	{
		this.title = newTitle;
		for( final SubrackTitleListener l : titleListeners )
		{
			l.receiveTitleUpdate( this, newTitle );
		}
	}

	@Override
	public void addTitleListener( final SubrackTitleListener listener )
	{
		this.titleListeners.add( listener );
	}

	@Override
	public void removeTitleListener( final SubrackTitleListener listener )
	{
		this.titleListeners.remove( listener );
	}

	@Override
	public void doTabClose()
	{
		uiInstance.makeSubRackFrameVisible( false );
	}


}
