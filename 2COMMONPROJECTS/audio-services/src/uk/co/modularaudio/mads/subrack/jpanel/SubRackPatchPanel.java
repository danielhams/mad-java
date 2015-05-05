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

import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.ContainerTab;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.GuiConstants;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class SubRackPatchPanel extends JPanel implements ContainerTab
{
	private static final long serialVersionUID = 637534081127536206L;

	private String title = "*empty*";
	private final SubRackMadUiInstance uiInstance;
	private final RackModelRenderingComponent rmrc;

	private final RackService rackService;

	private final HashSet<ContainerTabTitleListener> titleListeners = new HashSet<ContainerTabTitleListener>();

	public SubRackPatchPanel( final SubRackMadUiInstance uiInstance, final RackModelRenderingComponent rmrc,
			final RackService rackService )
	{
		this.uiInstance = uiInstance;
		this.rmrc = rmrc;
		this.rackService = rackService;
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		this.setLayout( msh.createMigLayout() );
		add( rmrc.getJComponent(), "grow");

		this.setSize( GuiConstants.GUI_DEFAULT_DIMENSIONS );
	}

	public void setRackDataModel( final RackDataModel subRackDataModel ) throws DatastoreException
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
		for( final ContainerTabTitleListener l : titleListeners )
		{
			l.receiveTitleUpdate( this, newTitle );
		}
	}

	@Override
	public void addTitleListener( final ContainerTabTitleListener listener )
	{
		this.titleListeners.add( listener );
	}

	@Override
	public void removeTitleListener( final ContainerTabTitleListener listener )
	{
		this.titleListeners.remove( listener );
	}

	@Override
	public void doTabClose()
	{
		uiInstance.makeSubRackFrameVisible( false );
	}


}
