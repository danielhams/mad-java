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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;

public class Menubar extends JMenuBar
{

	private final JMenu fileMenu;
	private final JMenu editMenu;
	private final JMenu windowMenu;

	private final JMenuItem editShowPreferencesItem;

	private final JMenuItem fileNewItem;
	private final JMenuItem fileOpenItem;
	private final JMenuItem fileRevertItem;
	private final JMenuItem fileSaveItem;
	private final JMenuItem fileSaveAsItem;
	private final JMenuItem fileExitItem;

	private final JMenuItem windowShowProfilingItem;
	private final JMenuItem windowAboutItem;

	/**
	 *
	 */
	private static final long serialVersionUID = 2889045004230755253L;

	public Menubar( final ComponentDesignerFrontController fc, final MainFrameActions actions )
	{
		fileMenu = new JMenu();
		fileMenu.setText( "File" );
		fileNewItem = new JMenuItem( actions.getNewFileAction() );
		fileMenu.add( fileNewItem );
		fileOpenItem = new JMenuItem( actions.getOpenFileAction() );
		fileMenu.add( fileOpenItem );
		fileRevertItem = new JMenuItem( actions.getRevertFileAction() );
		fileMenu.add( fileRevertItem );
		fileSaveItem = new JMenuItem( actions.getSaveFileAction() );
		fileMenu.add( fileSaveItem );
		fileSaveAsItem = new JMenuItem( actions.getSaveAsFileAction() );
		fileMenu.add( fileSaveAsItem );
		fileExitItem = new JMenuItem( actions.getExitAction() );
		fileMenu.add( fileExitItem );
		this.add( fileMenu );

		editMenu = new JMenu();
		editMenu.setText( "Edit" );
		editShowPreferencesItem = new JMenuItem( actions.getShowPreferencesAction() );
		editMenu.add( editShowPreferencesItem );

		this.add( editMenu );

		windowMenu = new JMenu();
		windowMenu.setText( "Window" );

		windowShowProfilingItem = new JMenuItem( actions.getWindowShowProfilingAction() );
		windowMenu.add( windowShowProfilingItem );

		windowAboutItem = new JMenuItem( actions.getWindowAboutAction() );
		windowMenu.add( windowAboutItem );

		this.add( windowMenu );
	}
}
