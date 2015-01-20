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

package uk.co.modularaudio.util.audio.gui.mad.helper;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;
import uk.co.modularaudio.util.table.Span;

public abstract class AbstractNonConfigurableMadUiDefinition<MD extends MadDefinition<MD,MI>,
	MI extends MadInstance<MD, MI>, 
	MUI extends MadUiInstance<MD, MI>>
 	extends MadUiDefinition<MD, MI>
{
	private Span span = null;
	
	protected MadUiControlDefinition<?,?,?>[] controls = null;
	
	private Class<MUI> instanceClass = null;
	private int[] uiChannelInstanceIndexes = null;
	private Point[] uiChannelPositions = null;
	
//	private String[] uiControlNames = null;
//	private ControlType[] uiControlTypes = null;
//	private Class<?>[] uiControlClasses = null;
//	private Rectangle[] uiControlBounds = null;

//	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AbstractNonConfigurableMadUiDefinition( BufferedImageAllocator bia,
			MD definition,
			ImageFactory cif,
			String imageRoot,
			Span span,
			Class<MUI> instanceClass,
			int[] uiChannelInstanceIndexes,
			Point[] uiChannelPositions,
			String[] uiControlNames,
			ControlType[] uiControlTypes,
			Class<?>[] uiControlClasses,
			Rectangle[] uiControlBounds )
		throws DatastoreException
	{
		// Use default (is draggable, is not configurable)
		super( bia, definition );

		this.span = span;
		
		frontBufferedImage = cif.getBufferedImage( imageRoot,
				definition.getId() + "_front.png" );
		
		backBufferedImage = cif.getBufferedImage( imageRoot,
				definition.getId() + "_back.png");
		
		this.instanceClass = instanceClass;
		this.uiChannelInstanceIndexes = uiChannelInstanceIndexes;
		this.uiChannelPositions = uiChannelPositions;
		
//		this.uiControlNames = uiControlNames;
//		this.uiControlTypes = uiControlTypes;
//		this.uiControlClasses = uiControlClasses;
//		this.uiControlBounds = uiControlBounds;
		
		// Now add the control definitions into the list
		try
		{
			controls = new MadUiControlDefinition<?,?,?>[ uiControlClasses.length ];
			for( int i = 0 ; i < uiControlClasses.length ; i++ )
			{
				String controlName = uiControlNames[ i ];
				ControlType controlType = uiControlTypes[ i ];
				Rectangle controlBounds = uiControlBounds[ i ];
				Class<?> actualUiControlClass = uiControlClasses[ i ];

				InternalMadUiControlDefinition controlDef = new InternalMadUiControlDefinition( i, controlName,
						controlType,
						controlBounds,
						actualUiControlClass );
				controls[ i ] = controlDef;
			}
		}
		catch (Exception e)
		{
			String msg = "Exception caught instantiating control defs: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}

	public BufferedImage getFrontBufferedImage()
	{
		return frontBufferedImage;
	}

	public BufferedImage getBackBufferedImage()
	{
		return backBufferedImage;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MadUiInstance<MD, MI> createNewUiInstance( MI instance )
		throws DatastoreException
	{
		MadUiInstance<MD, MI> retVal = null;
		try
		{
			// Setup where the channels live
			ArrayList<MadUiChannelInstance> uiChannelInstances = new ArrayList<MadUiChannelInstance>();
			MadChannelInstance[] channelInstances = instance.getChannelInstances();

			for( int i = 0 ; i < uiChannelInstanceIndexes.length ; i++ )
			{
				int uiChannelInstanceIndex = uiChannelInstanceIndexes[ i ];
				Point uiChannelPosition = uiChannelPositions[ i ];
				MadChannelInstance chanIns = channelInstances[ uiChannelInstanceIndex ];
				uiChannelInstances.add( new MadUiChannelInstance( uiChannelPosition, chanIns ) );
			}
			
			// Now create the instance, as we will add the controls in afterwards (the controls need
			// a reference to the ui instance they live in)
			Class[] insConParamTypes = new Class[] {
					instance.getClass(),
					this.getClass()
			};
			Constructor uiInsCons = instanceClass.getConstructor( insConParamTypes );
			Object[] insComParams = new Object[] {
					instance,
					this
			};
			Object oic = uiInsCons.newInstance( insComParams );
			retVal = (MadUiInstance<MD, MI>)oic;
			
			// And setup the controls
			ArrayList<MadUiControlInstance<?,?,?>> uiControlInstances = new ArrayList<MadUiControlInstance<?,?,?>>();
			ArrayList<MadUiControlInstance<?,?,?>> uiDisplayProcessingControlInstances = new ArrayList<MadUiControlInstance<?,?,?>>();
	
			// Controls
			for( MadUiControlDefinition controlDef : controls )
			{
				MadUiControlInstance<?,?,?> uici = controlDef.createInstance( instance, retVal );
				uiControlInstances.add( uici );
				if( uici.needsDisplayProcessing() )
				{
					uiDisplayProcessingControlInstances.add( uici );
				}
			}
			
			retVal.setUiControlsAndChannels( uiControlInstances.toArray( new MadUiControlInstance<?,?,?>[ uiControlInstances.size() ] ),
					uiDisplayProcessingControlInstances.toArray( new MadUiControlInstance<?,?,?>[ uiDisplayProcessingControlInstances.size() ] ),
					uiChannelInstances.toArray( new MadUiChannelInstance[ uiChannelInstances.size() ] ) );
		}
		catch(Exception e)
		{
			String msg = "Exception caught creating new instance: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		return retVal;
	}
	
	public Span getCellSpan()
	{
		return span;
	}

}
