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

package uk.co.modularaudio.util.swing.general.astgui;

import java.text.ParseException;

import javax.swing.JTextField;

/**
 * DecimalField extends JTextField to force the entry of valid decimal
 * (i.e. floating point and integer) numbers. The representation and
 * exact format of the numbers is defined by a ScientificFormat object.
 *
 * @since $Date: 2012/02/09 13:05:27 $
 * @since 26-OCT-2000
 * @author Peter W. Draper
 * @version $Id: DecimalField.java,v 1.2 2012/02/09 13:05:27 dan Exp $
 */
public class DecimalField extends JTextField
{
	private static final long serialVersionUID = 7569162081377336915L;
	
	/**
     * Reference to object that describes the locale specific number
     * format.
     */
    protected ScientificFormat scientificFormat;

    /**
     * Create an instance, requires the initial value (as a double),
     * the number of columns to show and a ScientificFormat object to use
     * when checking and formatting the accepted values. Uses a
     * DecimalDocument to check that any typed values are valid for
     * this format.
     */
    public DecimalField( double value, int columns,
                         ScientificFormat format )
    {
        super( columns );
        setDocument( new DecimalDocument( format ) );
        scientificFormat = format;
        setDoubleValue( value );
    }

    /**
     * Create an instance, requires the initial value (as an int),
     * the number of columns to show and a ScientificFormat object to use
     * when checking and formatting the accepted values. Uses a
     * DecimalDocument to check that any typed values are valid for
     * this format.
     */
    public DecimalField( int value, int columns, ScientificFormat format )
    {
        super( columns );
        setDocument( new DecimalDocument( format ) );
        scientificFormat = format;
        setIntValue( value );
    }

    /**
     * Get the current value as double precision. If this fails then
     * 0.0 is returned.
     */
    public double getDoubleValue()
    {
        double retVal = 0.0;
        try {
            retVal = scientificFormat.parse( getText() ).doubleValue();
        } 
        catch ( ParseException e ) {
            // Return default value.
        }
        return retVal;
    }

    /**
     * Get the current value as an integer. If this fails then 0 is
     * returned.
     */
    public int getIntValue()
    {
        int retVal = 0;
        try {
            retVal = scientificFormat.parse( getText() ).intValue();
        } 
        catch ( ParseException e ) {
            // Return default value.
        }
        return retVal;
    }

    /**
     * Set the current value.
     */
    public void setDoubleValue( double value )
    {
        setText( scientificFormat.format( value ) );
    }

    /**
     * Set the current value.
     */
    public void setIntValue( int value )
    {
        setText( scientificFormat.format( value ) );
    }
}
