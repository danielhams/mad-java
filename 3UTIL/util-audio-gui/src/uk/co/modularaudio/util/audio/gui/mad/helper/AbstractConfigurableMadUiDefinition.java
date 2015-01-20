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
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;

public abstract class AbstractConfigurableMadUiDefinition
	<MD extends MadDefinition<MD,MI>, MI extends MadInstance<MD, MI>, MUI extends MadUiInstance<MD, MI>>
 	extends MadUiDefinition<MD, MI>
{
	private Class<MUI> instanceClass = null;
	
	public AbstractConfigurableMadUiDefinition( BufferedImageAllocator bia,
			MD definition,
			ImageFactory cif,
			String imageRoot,
			Class<MUI> instanceClass )
		throws DatastoreException
	{
		super( bia, definition, true, true );
		
		frontBufferedImage = cif.getBufferedImage( imageRoot,
				definition.getId() + "_front.png" );
		
		backBufferedImage = cif.getBufferedImage( imageRoot,
				definition.getId() + "_back.png");
		
		this.instanceClass = instanceClass;
		
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

			MadChannelInstance[] channelInstances = instance.getChannelInstances();
			
			MadUiInstanceConfiguration uiInstanceConfiguration = getUiInstanceConfiguration( instance );
			
			int[] uiChannelInstanceIndexes = uiInstanceConfiguration.getUiChannelInstanceIndexes();
			Point[] uiChannelPositions = uiInstanceConfiguration.getUiChannelPositions();
	
			MadUiChannelInstance[] uiChannelInstances = new MadUiChannelInstance[ uiChannelInstanceIndexes.length ];
			
			for( int i = 0 ; i < uiChannelInstanceIndexes.length ; i++ )
			{
				int uiChannelInstanceIndex = uiChannelInstanceIndexes[ i ];
				Point uiChannelPosition = uiChannelPositions[ i ];
				MadChannelInstance chanIns = channelInstances[ uiChannelInstanceIndex ];
				uiChannelInstances[ i ] = new MadUiChannelInstance( uiChannelPosition, chanIns );
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
					this,
			};
			Object oic = uiInsCons.newInstance( insComParams );
			retVal = (MadUiInstance<MD, MI>)oic;
			
			String[] uiControlNames = uiInstanceConfiguration.getUiControlNames();
			ControlType[] uiControlTypes = uiInstanceConfiguration.getUiControlTypes();
			Class<?>[] uiControlClasses = uiInstanceConfiguration.getUiControlClasses();
			Rectangle[] uiControlBounds = uiInstanceConfiguration.getUiControlBounds();
			
			// And setup the controls
			MadUiControlDefinition<?,?,?>[] uiControlDefinitions = new MadUiControlDefinition<?,?,?>[ uiControlClasses.length ];
			MadUiControlInstance<?,?,?>[] uiControlInstances = new MadUiControlInstance<?,?,?>[ uiControlClasses.length ];
			ArrayList<MadUiControlInstance<?,?,?>> uiDisplayProcessingControlInstancesArray =
					new ArrayList<MadUiControlInstance<?,?,?>>();
			for( int i = 0 ; i < uiControlClasses.length ; i++ )
			{
				String controlName = uiControlNames[ i ];
				ControlType controlType = uiControlTypes[ i ];
				Rectangle controlBounds = uiControlBounds[ i ];
				Class<?> actualUiControlClass = uiControlClasses[ i ];
	
				InternalMadUiControlDefinition controlDef = new InternalMadUiControlDefinition( i, controlName, controlType, controlBounds, actualUiControlClass );
				uiControlDefinitions[ i ] = controlDef;
				uiControlInstances[ i ] = controlDef.createInstance( instance, retVal );
				if( uiControlInstances[ i ].needsDisplayProcessing() )
				{
					uiDisplayProcessingControlInstancesArray.add( uiControlInstances[ i ] );
				}
			}
			MadUiControlInstance<?,?,?>[] uiDisplayProcessingControlInstances = uiDisplayProcessingControlInstancesArray.toArray(
				new MadUiControlInstance<?,?,?>[ uiDisplayProcessingControlInstancesArray.size() ] );
			
			retVal.setUiControlsAndChannels( uiControlInstances, uiDisplayProcessingControlInstances, uiChannelInstances );
		}
		catch(Exception e)
		{
			String msg = "Exception caught creating new instance: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		return retVal;
	}
	
	protected abstract MadUiInstanceConfiguration getUiInstanceConfiguration( MI instance );

}
