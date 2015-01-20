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

import uk.co.modularaudio.util.audio.gui.mad.MadUiControlDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.DatastoreException;

@SuppressWarnings("rawtypes")
public class InternalMadUiControlDefinition extends MadUiControlDefinition
{
//	private static Log log = LogFactory.getLog( InternalMadUiControlDefinition.class.getName() );
	
	private Class actualUiControlClass = null;

	public InternalMadUiControlDefinition( int controlIndex, String controlName, ControlType controlType, Rectangle controlBounds,
			Class actualUiControlClass )
	{
		super( controlIndex, controlName, controlType, controlBounds );
		this.actualUiControlClass = actualUiControlClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MadUiControlInstance createInstance( MadInstance instance,
			MadUiInstance uiInstance )
		throws DatastoreException
	{
		MadUiControlInstance retVal = null;
		try
		{
			// Reflection to fetch the new instance class and instantiate it
			MadDefinition definition = instance.getDefinition();
			Class[] consParamTypes = new Class[] {
					definition.getClass(),
					instance.getClass(),
					uiInstance.getClass(),
					int.class
			};
			Constructor cons = actualUiControlClass.getConstructor( consParamTypes );
			Object[] consParams = new Object[] {
					definition,
					instance,
					uiInstance,
					controlIndex
			};
			
			Object realControlObject = cons.newInstance( consParams );
			
			IMadUiControlInstance realUiControlInstance = (IMadUiControlInstance)realControlObject;
			retVal = new InternalMadUiControlInstance( uiInstance, this, realUiControlInstance );
		}
		catch (Exception e)
		{
			String msg = "Exception caught instantiating real control: " + e.toString();
			throw new DatastoreException( msg, e );
		}
		
		return retVal;
	}

}
