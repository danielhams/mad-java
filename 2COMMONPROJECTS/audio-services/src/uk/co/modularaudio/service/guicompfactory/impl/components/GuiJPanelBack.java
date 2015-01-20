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

package uk.co.modularaudio.service.guicompfactory.impl.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.valueobjects.plugs.GuiAudioChannelPlug;
import uk.co.modularaudio.service.gui.valueobjects.plugs.GuiCVChannelPlug;
import uk.co.modularaudio.service.gui.valueobjects.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.gui.valueobjects.plugs.GuiNoteChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;

public class GuiJPanelBack extends JPanel
{
	private static final long serialVersionUID = -896982424604292805L;

//	private static Log log = LogFactory.getLog( GuiJPanelBack.class.getName() );

	private GuiComponentImageCache imageCache = null;
	
	private RackComponent rackComponent = null;
	
	private BufferedImage composedBackgroundImage = null;
	
	private ComponentNameLabel componentNameLabel = null;
	
	private GuiChannelPlug[] plugsToDestroy = null;

	public GuiJPanelBack( GuiComponentImageCache imageCache, RackComponent inComponent )
	{
		this.imageCache = imageCache;
		this.setOpaque( true );
		this.setLayout( null );
		componentNameLabel = new ComponentNameLabel( inComponent );
		this.add( componentNameLabel );
		
		MadUiChannelInstance[] chanDefs = inComponent.getUiChannelInstances();
		plugsToDestroy = new GuiChannelPlug[ chanDefs.length ];
		for( int i = 0 ; i < chanDefs.length ; i++ )
		{
			MadUiChannelInstance cd = chanDefs[ i ];
			GuiChannelPlug plug = null;
			MadChannelDefinition channelDefinition = cd.getChannelInstance().definition;
			switch( channelDefinition.type )
			{
				case AUDIO:
				{
					plug = new GuiAudioChannelPlug( cd );
					break;
				}
				case CV:
				{
					plug = new GuiCVChannelPlug( cd );
					break;
				}
				case NOTE:
				{
					plug = new GuiNoteChannelPlug( cd );
					break;
				}
			}
			this.add( plug );
			plugsToDestroy[ i ] = plug;
		}

		this.rackComponent = inComponent;
//		this.backgroundImage = rackComponent.getBackBufferedImage();
	}

	@Override
	public void paint(Graphics g)
	{
		Dimension curSize = this.getSize();
		if( composedBackgroundImage == null )
		{
			this.composedBackgroundImage = imageCache.getImageForRackComponent( rackComponent, curSize.width, curSize.height, false );
		}

		g.drawImage( composedBackgroundImage, 0, 0, null );

		super.paintChildren( g );
	}
	
	public GuiChannelPlug getPlugFromPosition(Point localPoint)
	{
		GuiChannelPlug retVal = null;
		Component c = this.getComponentAt( localPoint );
		if( c != null )
		{
			if( c instanceof GuiChannelPlug )
			{
				retVal = (GuiChannelPlug)c;
			}
		}
		return retVal;
	}

	public GuiChannelPlug getPlugFromChannelInstance( MadChannelInstance auChannelInstance )
	{
		GuiChannelPlug retVal = null;
		Component[] children = this.getComponents();
		boolean foundIt = false;
		for( int i = 0 ; !foundIt && i < children.length ; i++ )
		{
			Component c = children[i];
			if( c instanceof GuiChannelPlug )
			{
				GuiChannelPlug gcp = (GuiChannelPlug)c;
				if( gcp.getUiChannelInstance().getChannelInstance() == auChannelInstance )
				{
					retVal = gcp;
					foundIt = true;
				}
			}
		}
		return retVal;
	}

	public void destroy()
	{
//		log.debug("GuiJPanelBack destroy called");
		this.removeAll();
		for( int i = 0 ; i < plugsToDestroy.length ; i++ )
		{
			plugsToDestroy[ i ].destroy();
			plugsToDestroy[ i ] = null;
		}
		plugsToDestroy = null;
		rackComponent = null;
		composedBackgroundImage = null;
		imageCache = null;
	}
}
