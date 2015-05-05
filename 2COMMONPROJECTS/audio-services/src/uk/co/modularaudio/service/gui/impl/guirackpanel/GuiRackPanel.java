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

package uk.co.modularaudio.service.gui.impl.guirackpanel;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.gui.GuiRackBackActionListener;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.impl.guirackpanel.sub.GuiRackActions;
import uk.co.modularaudio.service.gui.impl.guirackpanel.sub.GuiRackAndWiresCardPanel;
import uk.co.modularaudio.service.gui.impl.guirackpanel.sub.GuiRackToolbar;
import uk.co.modularaudio.service.gui.impl.guirackpanel.sub.GuiScrollableArea;
import uk.co.modularaudio.service.gui.impl.racktable.RackSidesEmptyCellPainter;
import uk.co.modularaudio.service.gui.impl.racktable.RackTable;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableDndPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableEmptyCellPainter;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableGuiFactory;
import uk.co.modularaudio.service.gui.impl.racktable.back.BackRackTableGuiFactory;
import uk.co.modularaudio.service.gui.impl.racktable.back.RackTableWithLinks;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.BackDndPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag.DndRackDragDecorations;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag.DndRackDragPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag.DndWireDragDecorations;
import uk.co.modularaudio.service.gui.impl.racktable.front.FrontRackTableGuiFactory;
import uk.co.modularaudio.service.guicompfactory.GuiComponentFactoryService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class GuiRackPanel extends JPanel implements RackModelRenderingComponent
{
//	private static Log log = LogFactory.getLog( GuiRackPanel.class.getName() );

	private static final long serialVersionUID = -8428902055860040205L;

	private static final boolean SHOW_GRIDS = false;
	public final static Dimension FRONT_GRID_SIZE = new Dimension( 300, 80 );
	public final static Dimension BACK_GRID_SIZE = new Dimension( 300, 80 );

	private final GuiRackToolbar toolbar;

	private final GuiRackActions guiRackActions;

	private final RackTable frontAudioComponentTable;
	private final RackTableWithLinks backAudioComponentTable;
	private final GuiRackAndWiresCardPanel rackAndWiresCardPanel;
	private final GuiScrollableArea scrollableArea;
	private final GuiRackBackActionListener backActionListener;

	public GuiRackPanel( final GuiComponentFactoryService guiComponentFactoryService,
			final GuiService guiService,
			final RackService rackService,
			final MadComponentService componentService,
			final BufferedImageAllocationService bufferedImageAllocationService,
			final GuiRackBackActionListener backActionListener,
			final RackDataModel rackDataModel ) throws DatastoreException
	{
		this.backActionListener = backActionListener;

		guiRackActions = new GuiRackActions( guiService, rackService, componentService, this, rackDataModel );

		toolbar = new GuiRackToolbar( guiRackActions, guiService );

		final RackTableEmptyCellPainter frontEmptyCellPainter = new RackSidesEmptyCellPainter();

		final Color gridColour = Color.BLUE;
		final RackTableGuiFactory frontAudioComponentToGuiFactory = new FrontRackTableGuiFactory( guiComponentFactoryService );

		final DndRackDragDecorations decorations = new DndRackDragDecorations();
		final DndRackDragPolicy rackDragDndPolicy = new DndRackDragPolicy( rackService, guiService, rackDataModel, decorations );

		frontAudioComponentTable = new RackTable( rackDataModel,
				frontEmptyCellPainter,
				frontAudioComponentToGuiFactory,
				rackDragDndPolicy,
				decorations,
				FRONT_GRID_SIZE,
				SHOW_GRIDS,
				gridColour);

		final RackTableEmptyCellPainter backEmptyCellPainter = new RackSidesEmptyCellPainter();

		final Color backGridColour = Color.GREEN;
		final RackTableGuiFactory backAudioComponentToGuiFactory = new BackRackTableGuiFactory( guiComponentFactoryService );

		final DndRackDragDecorations rackDecorations = new DndRackDragDecorations();
		final DndWireDragDecorations wireDecorations = new DndWireDragDecorations( bufferedImageAllocationService );

		final RackTableDndPolicy backAudioComponentTableDndPolicy = new BackDndPolicy( rackService,
				guiService,
				rackDataModel,
				rackDecorations,
				wireDecorations,
				backActionListener );

		backAudioComponentTable = new RackTableWithLinks( bufferedImageAllocationService,
				rackDataModel,
				backEmptyCellPainter,
				backAudioComponentToGuiFactory,
				backAudioComponentTableDndPolicy,
				rackDecorations,
				wireDecorations,
				BACK_GRID_SIZE,
				SHOW_GRIDS, // showGrid,
				backGridColour );

		rackAndWiresCardPanel = new GuiRackAndWiresCardPanel( frontAudioComponentTable, backAudioComponentTable );

		scrollableArea = new GuiScrollableArea();
		scrollableArea.getViewport().add( rackAndWiresCardPanel );

		// Now setup the panel
		// add the toolbar the scrollpane and cardpanel to it
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );

		this.setLayout( msh.createMigLayout() );
		this.add( toolbar, "wrap" );
		this.add( scrollableArea, "grow" );
	}

	@Override
	public void setRackDataModel( final RackDataModel rackDataModel ) throws DatastoreException
	{
		frontAudioComponentTable.setRackDataModel( rackDataModel );
		backAudioComponentTable.setRackDataModel( rackDataModel );
		backActionListener.setRackDataModel( rackDataModel );
		guiRackActions.setRackDataModel( rackDataModel );
	}

	@Override
	public JComponent getJComponent()
	{
		return this;
	}

	@Override
	public void rotateRack()
	{
		rackAndWiresCardPanel.rotateRack();
	}

	@Override
	public boolean isFrontShowing()
	{
		return rackAndWiresCardPanel.isFrontShowing();
	}

	@Override
	public void destroy()
	{
//		log.debug("Destroying rack panel.");
		frontAudioComponentTable.destroy();
		backAudioComponentTable.destroy();
		backActionListener.destroy();
		guiRackActions.destroy();
	}

	@Override
	public void setForceRepaints( final boolean forceRepaints )
	{
		frontAudioComponentTable.setForceRepaints( forceRepaints );
		backAudioComponentTable.setForceRepaints( forceRepaints );
	}
}
