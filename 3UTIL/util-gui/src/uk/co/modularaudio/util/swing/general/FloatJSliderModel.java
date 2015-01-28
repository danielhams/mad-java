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

package uk.co.modularaudio.util.swing.general;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * Implementation of a slider model that supports a floating point
 * representation. The model is characterised by the floating point
 * values minimum, maximum and interval (i.e the resolution).
 * <p>
 * The actual value should be accessed by the setDoubleValue and
 * getDoubleValue methods. The integer versions are only to support
 * the actual values used by the slider. Naturally default labelling
 * of the slider isn't very useful.
 *
 * @author Peter W. Draper
 * @version $Id: FloatJSliderModel.java,v 1.1 2012/01/07 10:42:22 dan Exp $
 */
public class FloatJSliderModel
    implements BoundedRangeModel
{
    /**
     * Reference to ChangeEvent, set when fireStateChanged invoked.
     */
    protected ChangeEvent changeEvent;

    /**
     * List of listeners for our change events.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * The maximum value used by the JSlider.
     */
    protected int realMaximum = 100;

    /**
     * The minimum value used by the JSlider, always 0.
     */
    protected int realMinimum = 0;

    /**
     * The real value of the slider.
     */
    protected int realValue = 0;

    /**
     * The range of value covered by the slider knob, always zero.
     */
    protected int realExtent = 0;

    /**
     * The maximum value as seen by a user of this model.
     */
    protected double apparentMaximum = 1.0;

    /**
     * The minimum value as seen by a user of this model.
     */
    protected double apparentMinimum = 0.0;

    /**
     * The resolution between discrete values.
     */
    protected double resolution = 0.0E-6;

    /**
     * True when the slider is continuously adjusting.
     */
    protected boolean isAdjusting = false;


    /**
     * Create an instance of the model, with default values.
     */
    public FloatJSliderModel()
    {
        super();
    }

    /**
     * Create an instance of the model, with the given value, range
     * and interval.
     *
     * @param value the initial value.
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param resolution the resolution between discrete values.
     */
    public FloatJSliderModel( final double value, final double minValue,
                              final double maxValue, final double resolution )
    {
        setApparentValues( value, minValue, maxValue, resolution );
    }

    /**
     * Get the current resolution.
     */
    public double getResolution()
    {
        return resolution;
    }

    /**
     * Set the current resolution.
     */
    public void setResolution( final double value )
    {
        this.resolution = value;
        configureRealValues();
        fireStateChanged();
    }

    /**
     * Set the apparent values of the slider.
     *
     * @param value the initial value.
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param resolution the resolution between discrete values.
     */
    public void setApparentValues( final double value, final double minValue,
                                   final double maxValue, final double resolution )
    {
        apparentMinimum = minValue;
        apparentMaximum = maxValue;
        this.resolution = resolution;
        configureRealValues();
        setDoubleValue( value );
    }

    /**
     * Get the real maximum value used by the slider.
     */
    @Override
	public int getMaximum()
    {
        return realMaximum;
    }

    /**
     * Set the real maximum value used by the slider. Does nothing.
     */
    @Override
	public void setMaximum( final int newMaximum )
    {
        //  Do nothing.
    }

    /**
     * Get the real minimum value used by the slider.
     */
    @Override
	public int getMinimum()
    {
        return realMinimum;
    }

    /**
     * Set the real minimum value used by the slider. Does nothing.
     */
    @Override
	public void setMinimum( final int newMinimum )
    {
        // Do nothing.
    }

    /**
     * Get the real value as represented by the slider. Use
     * getDoubleValue instead.
     */
    @Override
	public int getValue()
    {
        return realValue;
    }

    /**
     * Set the real value as shown by the slider. Use getDoubleValue
     * instead.
     */
    @Override
	public void setValue( final int newValue )
    {
        setRangeProperties( newValue, realExtent, realMinimum,
                            realMaximum, isAdjusting );
    }

    /**
     * Get the current value.
     */
    public double getDoubleValue()
    {
        return convertToApparent( realValue );
    }

    /**
     * Set the current value.
     */
    public void setDoubleValue( final double newValue )
    {
        final int newRealValue = convertToReal( newValue );
        setRangeProperties( newRealValue, realExtent, realMinimum,
                            realMaximum, isAdjusting );
    }

    /**
     * Get the real extent of the "knob" (always 0).
     */
    @Override
	public int getExtent()
    {
        return realExtent;
    }

    /**
     * Set the real extent of the "knob". Does nothing.
     */
    @Override
	public void setExtent( final int newExtent )
    {
        // Do nothing.
    }

    /**
     * Return if the value is constantly adjusting.
     */
    @Override
	public boolean getValueIsAdjusting()
    {
        return isAdjusting;
    }

    /**
     * Set if the value is constantly adjusting.
     */
    @Override
	public void setValueIsAdjusting( final boolean b )
    {
        setRangeProperties( realValue, realExtent, realMinimum,
                            realMaximum, b );
    }

    /**
     * Set the state of the model using the given "real" values. After
     * this method is invoked the apparent values may be adjusted.
     */
    @Override
	public void setRangeProperties( final int newValue,
                                    final int newExtent,
                                    final int newMin,
                                    int newMax,
                                    final boolean newAdjusting )
    {
        if ( newMax < realMaximum ) {
            newMax = realMaximum;
        }

        if ( newValue > newMax) {
            newMax = newValue; // Allow range to be incremented by user.
        }

        boolean changeOccurred = false;
        if ( newValue != realValue ) {
            realValue = newValue;
            changeOccurred = true;
        }
        if ( newMax != realMaximum ) {
            realMaximum = newMax;
            apparentMaximum = convertToApparent( realMaximum );
            changeOccurred = true;
        }
        if ( newAdjusting != isAdjusting ) {
            realMaximum = newMax;
            isAdjusting = newAdjusting;
            changeOccurred = true;
        }

        if ( changeOccurred ) {
            fireStateChanged();
        }
    }

    /**
     *  Convert an apparent value into a real (slider) value.
     */
    protected double convertToApparent( final int rValue )
    {
        return (rValue - realMinimum) * resolution + apparentMinimum;
    }

    /**
     *  Convert a real (slider) value into an apparent value.
     */
    protected int convertToReal( final double aValue )
    {
        return (int) ( (aValue - apparentMinimum) / resolution ) + realMinimum;
    }

    /**
     * Configure the real (slider) values to reflect the current
     * range and resolution.
     */
    protected void configureRealValues()
    {
        // The important fact here is that the resolution must equate
        // to a real value of 1 (the real resolution of a slider).
        final int range = (int) ((apparentMaximum - apparentMinimum) / resolution);
        realMaximum = realMinimum + range;
    }

    /*
     * The rest of this is event handling code copied from
     * DefaultBoundedRangeModel.
     */
    @Override
	public void addChangeListener(final ChangeListener l)
    {
        listenerList.add(ChangeListener.class, l);
    }

    @Override
	public void removeChangeListener(final ChangeListener l)
    {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireStateChanged()
    {
        final Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
}
