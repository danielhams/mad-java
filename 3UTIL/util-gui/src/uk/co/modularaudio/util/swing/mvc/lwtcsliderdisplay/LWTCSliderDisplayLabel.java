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

package uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay;

import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class LWTCSliderDisplayLabel extends LWTCLabel
{
//	private static Log log = LogFactory.getLog( SliderDisplayLabel.class.getName() );

	private static final long serialVersionUID = 476235141676357358L;

	public LWTCSliderDisplayLabel( final LWTCSliderViewColors colours,
			final String startLabelText,
			final boolean opaque )
	{
		super( startLabelText );
		this.setOpaque( opaque );
		this.setFont( LWTCControlConstants.LABEL_FONT );
		this.setBorder( null );
		this.setBackground( colours.bgColor );
		this.setForeground( colours.labelColor );
	}
}
