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
	
	private JMenu fileMenu = null;
	private JMenu editMenu = null;
	private JMenu windowMenu = null;
	
	private JMenuItem editShowPreferencesItem = null;
	
	private JMenuItem fileNewItem = null;
	private JMenuItem fileOpenItem = null;
	private JMenuItem fileRevertItem = null;
	private JMenuItem fileSaveItem = null;
	private JMenuItem fileSaveAsItem = null;
	private JMenuItem fileExitItem = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2889045004230755253L;
	
	private ComponentDesignerFrontController fc = null;
	private MainFrameActions actions = null;
	
	public Menubar( ComponentDesignerFrontController fc, MainFrameActions actions )
	{
		this.fc = fc;
		this.actions = actions;
		this.add( getFileMenu() );
		this.add( getEditMenu() );
		this.add( getWindowMenu() );
	}
	
	public JMenu getFileMenu()
	{
		if( fileMenu == null )
		{
			fileMenu = new JMenu();
			fileMenu.setText( "File" );
			fileMenu.add( getFileNewItem() );
			fileMenu.add( getFileOpenItem() );
			fileMenu.add( getFileRevertItem() );
			fileMenu.add( getFileSaveItem() );
			fileMenu.add( getFileSaveAsItem() );
			fileMenu.add( getFileExitItem() );
		}
		return fileMenu;
	}
	
	public JMenuItem getFileNewItem()
	{
		if( fileNewItem == null )
		{
			fileNewItem = new JMenuItem( actions.getNewFileAction() );
		}
		return fileNewItem;
	}
	
	public JMenuItem getFileOpenItem()
	{
		if( fileOpenItem == null )
		{
			fileOpenItem = new JMenuItem( actions.getOpenFileAction() );
		}
		return fileOpenItem;
	}
	
	public JMenuItem getFileRevertItem()
	{
		if( fileRevertItem == null )
		{
			fileRevertItem = new JMenuItem( actions.getRevertFileAction() );
		}
		return fileRevertItem;
	}
	
	public JMenuItem getFileSaveItem()
	{
		if( fileSaveItem == null )
		{
			fileSaveItem = new JMenuItem( actions.getSaveFileAction() );
		}
		return fileSaveItem;
	}
	
	public JMenuItem getFileSaveAsItem()
	{
		if( fileSaveAsItem == null )
		{
			fileSaveAsItem = new JMenuItem( actions.getSaveAsFileAction() );
		}
		return fileSaveAsItem;
	}
	
	public JMenuItem getFileExitItem()
	{
		if( fileExitItem == null )
		{
			fileExitItem = new JMenuItem( actions.getExitAction() );
		}
		return fileExitItem;
	}
	
	public JMenu getEditMenu()
	{
		if( editMenu == null )
		{
			editMenu = new JMenu();
			editMenu.setText( "Edit" );
			editMenu.add( getEditShowPreferencesItem( fc ) );
		}
		return editMenu;
	}
	
	public JMenuItem getEditShowPreferencesItem( ComponentDesignerFrontController fc )
	{
		if( editShowPreferencesItem == null )
		{
			editShowPreferencesItem = new JMenuItem( actions.getShowPreferencesAction() );
		}
		return editShowPreferencesItem;
	}
	
	public JMenu getWindowMenu()
	{
		if( windowMenu == null )
		{
			windowMenu = new JMenu();
			windowMenu.setText( "Window" );
			windowMenu.add( new JMenuItem( "About" ) );
		}
		return windowMenu;
	}

}
