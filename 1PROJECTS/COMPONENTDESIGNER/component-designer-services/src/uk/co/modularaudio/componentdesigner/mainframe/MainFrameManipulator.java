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

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.tabbedpane.MainFrameTabbedPane;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.rack.GuiConstants;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class MainFrameManipulator
{
//	private static Log log = LogFactory.getLog( MainFrameManipulator.class.getName() );

	private final static String MAINFRAME_TITLE = "Component Designer";

	// Menubar
	private final JMenuBar menubar;

	// Content panel
	private final JPanel contentFrame;

	// App Toolbar
	private final ComponentDesignerToolbar componentDesignerToolbar;

	// Tabbed Pane area for racks
	private final MainFrameTabbedPane rackTabbedPane;

	// Rack area
	private final RackModelRenderingComponent rackModelRenderingComponent;

	public MainFrameManipulator( final ComponentDesignerFrontController fc,
			final ComponentImageFactory pcif,
			final ConfigurationService cs,
			final MainFrame mainFrame,
			final MainFrameActions actions )
	{
		menubar = new Menubar( fc, actions );

		contentFrame = new JPanel();
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "flowy" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addColumnConstraint( "[grow, fill]" );
		msh.addRowConstraint( "[][grow 100, fill]" );

		contentFrame.setLayout( msh.createMigLayout() );

		componentDesignerToolbar = new ComponentDesignerToolbar( fc, actions );

		contentFrame.add( componentDesignerToolbar );

		rackTabbedPane = new MainFrameTabbedPane();
		fc.registerRackTabbedPane( rackTabbedPane );
		rackModelRenderingComponent = fc.getGuiRack();
		rackTabbedPane.addTab( "Main Rack", rackModelRenderingComponent.getJComponent() );

		contentFrame.add( rackTabbedPane );


		mainFrame.setTitle( MAINFRAME_TITLE );
		mainFrame.setSize( GuiConstants.GUI_DEFAULT_DIMENSIONS );
		mainFrame.setMinimumSize( GuiConstants.GUI_MINIMUM_DIMENSIONS );
		mainFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		mainFrame.setJMenuBar( menubar );

		mainFrame.setToolbar( componentDesignerToolbar );

		final Container cp = mainFrame.getContentPane();
		cp.add( contentFrame );

		// Register our global keys
		GlobalKeyHelper.setupKeys( menubar, actions );
		GlobalKeyHelper.setupKeys( contentFrame, actions );
	}
}
