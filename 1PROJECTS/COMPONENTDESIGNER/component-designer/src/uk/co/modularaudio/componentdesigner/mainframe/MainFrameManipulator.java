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

package uk.co.modularaudio.componentdesigner.mainframe;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.rack.GuiConstants;

public class MainFrameManipulator
{
//	private static Log log = LogFactory.getLog( MainFrameManipulator.class.getName() );

	private static String MAINFRAME_TITLE = "Component Designer";

	private ComponentDesignerFrontController fc = null;
//	private ComponentImageFactory cif = null;
//	private ConfigurationService cs = null;

	private MainFrameActions actions = null;

	// Menubar
	private JMenuBar menubar = null;

	// Content panel
	private JPanel contentFrame = null;

	// App Toolbar
	private ComponentDesignerToolbar componentDesignerToolbar = null;

	// Tabbed Pane area for racks
	private MainFrameTabbedPane rackTabbedPane = null;

	// Rack area
	private RackModelRenderingComponent rackModelRenderingComponent = null;

	public MainFrameManipulator( ComponentDesignerFrontController fc,
			ComponentImageFactory pcif,
			ConfigurationService cs,
			MainFrame mainFrame,
			MainFrameActions actions )
	{
		this.fc = fc;
//		this.cif = pcif;
//		this.cs = cs;
		this.actions = actions;

		mainFrame.setTitle( MAINFRAME_TITLE );
		mainFrame.setSize( GuiConstants.GUI_DEFAULT_DIMENSIONS );
		mainFrame.setMinimumSize( GuiConstants.GUI_MINIMUM_DIMENSIONS );
		mainFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		menubar = new Menubar( fc, actions );
		mainFrame.setJMenuBar( menubar );

		componentDesignerToolbar = getComponentDesignerToolbar();
		mainFrame.setToolbar( componentDesignerToolbar );

		Container cp = mainFrame.getContentPane();
		cp.add( getContentFrame() );

		// Register our global keys
		GlobalKeyHelper.setupKeys( menubar, actions );
		GlobalKeyHelper.setupKeys( contentFrame, actions );
	}

	private JPanel getContentFrame()
	{
		if( contentFrame == null )
		{
			contentFrame = new JPanel();
			contentFrame.setLayout( new MigLayout("insets 0, fill, flowy", "grow, fill", "[] [grow 100, fill]") );
			contentFrame.add( getComponentDesignerToolbar() );

			contentFrame.add( getRackTabbedPane() );
		}
		return contentFrame;
	}

	private JTabbedPane getRackTabbedPane()
	{
		if( rackTabbedPane == null )
		{
			rackTabbedPane = new MainFrameTabbedPane();
			fc.registerRackTabbedPane( rackTabbedPane );
			rackTabbedPane.addTab( "Main Rack", getRackModelRenderingComponent().getJComponent() );
		}
		return rackTabbedPane;

	}

	private ComponentDesignerToolbar getComponentDesignerToolbar()
	{
		if( componentDesignerToolbar == null )
		{
			componentDesignerToolbar = new ComponentDesignerToolbar( fc, actions );
		}
		return componentDesignerToolbar;
	}

	private RackModelRenderingComponent getRackModelRenderingComponent()
	{
		if( rackModelRenderingComponent == null )
		{
			rackModelRenderingComponent = fc.getGuiRack();
		}
		return rackModelRenderingComponent;
	}
}
