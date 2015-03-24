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

import java.awt.Rectangle;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.DatastoreException;

@SuppressWarnings("rawtypes")
public class InternalMadUiControlDefinition extends MadUiControlDefinition
{
	private static Log log = LogFactory.getLog( InternalMadUiControlDefinition.class.getName() );

	private final Class actualUiControlClass;

	public InternalMadUiControlDefinition( final int controlIndex, final String controlName, final ControlType controlType, final Rectangle controlBounds,
			final Class actualUiControlClass )
	{
		super( controlIndex, controlName, controlType, controlBounds );
		this.actualUiControlClass = actualUiControlClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractMadUiControlInstance createInstance( final MadInstance instance,
			final AbstractMadUiInstance uiInstance )
		throws DatastoreException
	{
		try
		{
			// Reflection to fetch the new instance class and instantiate it
			final MadDefinition definition = instance.getDefinition();
			final Class[] consParamTypes = new Class[] {
					definition.getClass(),
					instance.getClass(),
					uiInstance.getClass(),
					int.class
			};
			final Constructor cons = actualUiControlClass.getConstructor( consParamTypes );
			final Object[] consParams = new Object[] {
					definition,
					instance,
					uiInstance,
					controlIndex
			};

			final Object realControlObject = cons.newInstance( consParams );

			final IMadUiControlInstance realUiControlInstance = (IMadUiControlInstance)realControlObject;
			final AbstractMadUiControlInstance retVal = new InternalMadUiControlInstance( uiInstance, this, realUiControlInstance );
			return retVal;
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught instantiating real control: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

}
