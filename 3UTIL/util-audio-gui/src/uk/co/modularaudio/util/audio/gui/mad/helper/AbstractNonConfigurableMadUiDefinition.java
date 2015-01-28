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

public abstract class AbstractNonConfigurableMadUiDefinition<D extends MadDefinition<D,I>,
	I extends MadInstance<D, I>,
	U extends MadUiInstance<D, I>>
 	extends MadUiDefinition<D, I>
{
	private final Span span;

	protected final MadUiControlDefinition<?,?,?>[] controls;

	private final Class<U> instanceClass;
	private final int[] uiChannelInstanceIndexes;
	private final Point[] uiChannelPositions;

	public AbstractNonConfigurableMadUiDefinition( final BufferedImageAllocator bia,
			final D definition,
			final ImageFactory cif,
			final String imageRoot,
			final Span span,
			final Class<U> instanceClass,
			final int[] uiChannelInstanceIndexes,
			final Point[] uiChannelPositions,
			final String[] uiControlNames,
			final ControlType[] uiControlTypes,
			final Class<?>[] uiControlClasses,
			final Rectangle[] uiControlBounds )
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
				final String controlName = uiControlNames[ i ];
				final ControlType controlType = uiControlTypes[ i ];
				final Rectangle controlBounds = uiControlBounds[ i ];
				final Class<?> actualUiControlClass = uiControlClasses[ i ];

				final InternalMadUiControlDefinition controlDef = new InternalMadUiControlDefinition( i, controlName,
						controlType,
						controlBounds,
						actualUiControlClass );
				controls[ i ] = controlDef;
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught instantiating control defs: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public BufferedImage getFrontBufferedImage()
	{
		return frontBufferedImage;
	}

	@Override
	public BufferedImage getBackBufferedImage()
	{
		return backBufferedImage;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MadUiInstance<D, I> createNewUiInstance( final I instance )
		throws DatastoreException
	{
		MadUiInstance<D, I> retVal = null;
		try
		{
			// Setup where the channels live
			final ArrayList<MadUiChannelInstance> uiChannelInstances = new ArrayList<MadUiChannelInstance>();
			final MadChannelInstance[] channelInstances = instance.getChannelInstances();

			for( int i = 0 ; i < uiChannelInstanceIndexes.length ; i++ )
			{
				final int uiChannelInstanceIndex = uiChannelInstanceIndexes[ i ];
				final Point uiChannelPosition = uiChannelPositions[ i ];
				final MadChannelInstance chanIns = channelInstances[ uiChannelInstanceIndex ];
				uiChannelInstances.add( new MadUiChannelInstance( uiChannelPosition, chanIns ) );
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
					this
			};
			final Object oic = uiInsCons.newInstance( insComParams );
			retVal = (MadUiInstance<D, I>)oic;

			// And setup the controls
			final ArrayList<MadUiControlInstance<?,?,?>> uiControlInstances = new ArrayList<MadUiControlInstance<?,?,?>>();
			final ArrayList<MadUiControlInstance<?,?,?>> uiDisplayProcessingControlInstances = new ArrayList<MadUiControlInstance<?,?,?>>();

			// Controls
			for( final MadUiControlDefinition controlDef : controls )
			{
				final MadUiControlInstance<?,?,?> uici = controlDef.createInstance( instance, retVal );
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
		catch(final Exception e)
		{
			final String msg = "Exception caught creating new instance: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		return retVal;
	}

	@Override
	public Span getCellSpan()
	{
		return span;
	}

}
