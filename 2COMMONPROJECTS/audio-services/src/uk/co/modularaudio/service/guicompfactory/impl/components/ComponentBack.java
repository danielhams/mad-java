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

import uk.co.modularaudio.service.gui.plugs.GuiAudioChannelPlug;
import uk.co.modularaudio.service.gui.plugs.GuiCVChannelPlug;
import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.gui.plugs.GuiNoteChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;

public class ComponentBack extends JPanel
{
	private static final long serialVersionUID = -896982424604292805L;

//	private static Log log = LogFactory.getLog( ComponentBack.class.getName() );

	private GuiComponentImageCache imageCache;

	private RackComponent rackComponent;

	private BufferedImage composedBackgroundImage;

	private final ComponentNameLabel componentNameLabel;

	private GuiChannelPlug[] plugsToDestroy;

	public ComponentBack( final GuiComponentImageCache imageCache, final RackComponent inComponent )
	{
		this.imageCache = imageCache;
		this.setOpaque( true );
		this.setLayout( null );
		componentNameLabel = new ComponentNameLabel( inComponent );
		this.add( componentNameLabel );

		final MadUiChannelInstance[] chanDefs = inComponent.getUiChannelInstances();
		plugsToDestroy = new GuiChannelPlug[ chanDefs.length ];
		for( int i = 0 ; i < chanDefs.length ; i++ )
		{
			final MadUiChannelInstance cd = chanDefs[ i ];
			GuiChannelPlug plug = null;
			final MadChannelDefinition channelDefinition = cd.getChannelInstance().definition;
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
	}

	@Override
	public void paint(final Graphics g)
	{
		final Dimension curSize = this.getSize();
		if( composedBackgroundImage == null )
		{
			this.composedBackgroundImage = imageCache.getImageForRackComponent( rackComponent, curSize.width, curSize.height, false );
		}

		g.drawImage( composedBackgroundImage, 0, 0, null );

		super.paintChildren( g );
	}

	public GuiChannelPlug getPlugFromPosition(final Point localPoint)
	{
		GuiChannelPlug retVal = null;
		final Component c = this.getComponentAt( localPoint );
		if( c != null )
		{
			if( c instanceof GuiChannelPlug )
			{
				retVal = (GuiChannelPlug)c;
			}
		}
		return retVal;
	}

	public GuiChannelPlug getPlugFromChannelInstance( final MadChannelInstance auChannelInstance )
	{
		GuiChannelPlug retVal = null;
		final Component[] children = this.getComponents();
		boolean foundIt = false;
		for( int i = 0 ; !foundIt && i < children.length ; i++ )
		{
			final Component c = children[i];
			if( c instanceof GuiChannelPlug )
			{
				final GuiChannelPlug gcp = (GuiChannelPlug)c;
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
