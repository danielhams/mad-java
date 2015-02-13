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
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition.ControlType;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.image.ImageFactory;

public abstract class AbstractConfigurableMadUiDefinition
	<D extends MadDefinition<D,I>, I extends MadInstance<D, I>, U extends AbstractMadUiInstance<D, I>>
 	extends MadUiDefinition<D, I>
{
	private final Class<U> instanceClass;

	public AbstractConfigurableMadUiDefinition( final BufferedImageAllocator bia,
			final ImageFactory cif,
			final String imageRoot,
			final String imagePrefix,
			final D definition,
			final Class<U> instanceClass )
		throws DatastoreException
	{
		super( bia, cif, imageRoot, imagePrefix, definition, true, true );

		this.instanceClass = instanceClass;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AbstractMadUiInstance<D, I> createNewUiInstance( final I instance )
		throws DatastoreException
	{
		AbstractMadUiInstance<D, I> retVal = null;
		try
		{
			// Setup where the channels live

			final MadChannelInstance[] channelInstances = instance.getChannelInstances();

			final MadUiInstanceConfiguration uiInstanceConfiguration = getUiInstanceConfiguration( instance );

			final int[] uiChannelInstanceIndexes = uiInstanceConfiguration.getUiChannelInstanceIndexes();
			final Point[] uiChannelPositions = uiInstanceConfiguration.getUiChannelPositions();

			final MadUiChannelInstance[] uiChannelInstances = new MadUiChannelInstance[ uiChannelInstanceIndexes.length ];

			for( int i = 0 ; i < uiChannelInstanceIndexes.length ; i++ )
			{
				final int uiChannelInstanceIndex = uiChannelInstanceIndexes[ i ];
				final Point uiChannelPosition = uiChannelPositions[ i ];
				final MadChannelInstance chanIns = channelInstances[ uiChannelInstanceIndex ];
				uiChannelInstances[ i ] = new MadUiChannelInstance( uiChannelPosition, chanIns );
			}

			// Now create the instance, as we will add the controls in afterwards (the controls need
			// a reference to the ui instance they live in)
			final Class[] insConParamTypes = new Class[] {
					instance.getClass(),
					this.getClass()
			};
			final Constructor uiInsCons = instanceClass.getConstructor( insConParamTypes );
			final Object[] insComParams = new Object[] {
					instance,
					this,
			};
			final Object oic = uiInsCons.newInstance( insComParams );
			retVal = (AbstractMadUiInstance<D, I>)oic;

			final String[] uiControlNames = uiInstanceConfiguration.getUiControlNames();
			final ControlType[] uiControlTypes = uiInstanceConfiguration.getUiControlTypes();
			final Class<?>[] uiControlClasses = uiInstanceConfiguration.getUiControlClasses();
			final Rectangle[] uiControlBounds = uiInstanceConfiguration.getUiControlBounds();

			// And setup the controls
			final MadUiControlDefinition<?,?,?>[] uiControlDefinitions = new MadUiControlDefinition<?,?,?>[ uiControlClasses.length ];
			final AbstractMadUiControlInstance<?,?,?>[] uiControlInstances = new AbstractMadUiControlInstance<?,?,?>[ uiControlClasses.length ];
			final ArrayList<AbstractMadUiControlInstance<?,?,?>> uiDisplayProcessingControlInstancesArray =
					new ArrayList<AbstractMadUiControlInstance<?,?,?>>();
			for( int i = 0 ; i < uiControlClasses.length ; i++ )
			{
				final String controlName = uiControlNames[ i ];
				final ControlType controlType = uiControlTypes[ i ];
				final Rectangle controlBounds = uiControlBounds[ i ];
				final Class<?> actualUiControlClass = uiControlClasses[ i ];

				final InternalMadUiControlDefinition controlDef = new InternalMadUiControlDefinition( i, controlName, controlType, controlBounds, actualUiControlClass );
				uiControlDefinitions[ i ] = controlDef;
				uiControlInstances[ i ] = controlDef.createInstance( instance, retVal );
				if( uiControlInstances[ i ].needsDisplayProcessing() )
				{
					uiDisplayProcessingControlInstancesArray.add( uiControlInstances[ i ] );
				}
			}
			final AbstractMadUiControlInstance<?,?,?>[] uiDisplayProcessingControlInstances = uiDisplayProcessingControlInstancesArray.toArray(
				new AbstractMadUiControlInstance<?,?,?>[ uiDisplayProcessingControlInstancesArray.size() ] );

			retVal.setUiControlsAndChannels( uiControlInstances, uiDisplayProcessingControlInstances, uiChannelInstances );
		}
		catch(final Exception e)
		{
			final String msg = "Exception caught creating new instance: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		return retVal;
	}

	protected abstract MadUiInstanceConfiguration getUiInstanceConfiguration( I instance );

}
