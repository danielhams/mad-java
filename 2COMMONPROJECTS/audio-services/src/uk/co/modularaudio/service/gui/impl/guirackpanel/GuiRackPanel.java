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

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
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
import uk.co.modularaudio.service.gui.valueobjects.GuiRackBackActionListener;
import uk.co.modularaudio.service.guicompfactory.GuiComponentFactoryService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;

public class GuiRackPanel extends JPanel implements RackModelRenderingComponent
{
//	private static Log log = LogFactory.getLog( GuiRackPanel.class.getName() );
	
	private static final long serialVersionUID = -8428902055860040205L;
	
	private static final boolean SHOW_GRIDS = false;
	public final static Dimension frontGridSize = new Dimension( 300, 80 );
	public final static Dimension backGridSize = new Dimension( 300, 80 );

	private GuiComponentFactoryService guiComponentFactoryService = null;
	private GuiService guiService = null;
	private RackService rackService = null;
	private MadComponentService componentService = null;
	private BufferedImageAllocationService bufferedImageAllocationService = null;
	private RackDataModel rackDataModel = null;
	
	private GuiRackToolbar toolbar = null;
	
	private GuiRackActions guiRackActions = null;
	
	private RackTable frontAudioComponentTable = null;
	private RackTableWithLinks backAudioComponentTable = null;
	private GuiRackAndWiresCardPanel rackAndWiresCardPanel = null;
	private GuiScrollableArea scrollableArea = null;
	private GuiRackBackActionListener backActionListener = null;
	
	public GuiRackPanel( GuiComponentFactoryService guiComponentFactoryService,
			GuiService guiService,
			RackService rackService,
			MadComponentService componentService,
			BufferedImageAllocationService bufferedImageAllocationService,
			GuiRackBackActionListener backActionListener,
			RackDataModel rackDataModel )
	{
		this.guiComponentFactoryService = guiComponentFactoryService;
		this.guiService = guiService;
		this.rackService = rackService;
		this.componentService = componentService;
		this.bufferedImageAllocationService = bufferedImageAllocationService;
		
		this.rackDataModel = rackDataModel;

		this.backActionListener = backActionListener;
		
		setupRackActions();
		
		setupToolbar();
		
		setupFrontComponent();
		setupBackComponent();
		setupRackAndWiresCardPanel();
		setupScrollableArea();

		// Now setup the panel
		// add the toolbar the scrollpane and cardpanel to it
		MigLayout migLayout = new MigLayout("insets 0, fill");
		this.setLayout( migLayout );
		this.add( toolbar, "wrap" );
		this.add( scrollableArea, "grow" );
		this.validate();
	}
	
	private void setupRackActions()
	{
		guiRackActions = new GuiRackActions( guiService, rackService, componentService, this, rackDataModel );
	}
	
	private void setupToolbar()
	{
		toolbar = new GuiRackToolbar( guiRackActions, guiService );
	}
	
	private void setupFrontComponent()
	{
		RackTableEmptyCellPainter frontEmptyCellPainter = new RackSidesEmptyCellPainter();
	
		Color gridColour = Color.BLUE;
		RackTableGuiFactory frontAudioComponentToGuiFactory = new FrontRackTableGuiFactory( guiComponentFactoryService );
		
		DndRackDragDecorations decorations = new DndRackDragDecorations();
		DndRackDragPolicy rackDragDndPolicy = new DndRackDragPolicy( rackService, guiService, rackDataModel, decorations );
	
		frontAudioComponentTable = new RackTable( rackDataModel,
				frontEmptyCellPainter,
				frontAudioComponentToGuiFactory,
				rackDragDndPolicy,
				decorations,
				frontGridSize,
				SHOW_GRIDS,
				gridColour);
	}
	
	private void setupBackComponent()
	{
		RackTableEmptyCellPainter backEmptyCellPainter = new RackSidesEmptyCellPainter();
		
		Color gridColour = Color.GREEN;
		RackTableGuiFactory backAudioComponentToGuiFactory = new BackRackTableGuiFactory( guiComponentFactoryService );
		
		DndRackDragDecorations rackDecorations = new DndRackDragDecorations();
		DndWireDragDecorations wireDecorations = new DndWireDragDecorations( bufferedImageAllocationService );
		
		RackTableDndPolicy backAudioComponentTableDndPolicy = new BackDndPolicy( rackService,
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
				backGridSize,
				SHOW_GRIDS, // showGrid,
				gridColour );
	}
	
	private void setupRackAndWiresCardPanel()
	{
		rackAndWiresCardPanel = new GuiRackAndWiresCardPanel( frontAudioComponentTable, backAudioComponentTable );
	}
	
	private void setupScrollableArea()
	{
		scrollableArea = new GuiScrollableArea();
		scrollableArea.getViewport().add( rackAndWiresCardPanel );
	}

	@Override
	public void setRackDataModel( RackDataModel rackDataModel )
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
		rackDataModel = null;
	}

	@Override
	public void setForceRepaints( boolean forceRepaints )
	{
		frontAudioComponentTable.setForceRepaints( forceRepaints );
		backAudioComponentTable.setForceRepaints( forceRepaints );		
	}
}
