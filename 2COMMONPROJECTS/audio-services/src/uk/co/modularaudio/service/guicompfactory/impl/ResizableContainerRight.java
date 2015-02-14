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

package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Dimension;

import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableContainerRight extends JPanel
{
	private static final long serialVersionUID = 5700599707006370407L;

//	private static Log log = LogFactory.getLog( ResizableBackContainerMiddle.class.getName() );

	private final FixedSizeTransparentCorner topCorner;
	private final FixedXTransparentBorder rBorder;
	private final FixedSizeTransparentCorner bottomCorner;

	public ResizableContainerRight( final ContainerImages ci )
	{
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "inset 0" );
		msh.addLayoutConstraint( "gap 0" );
//		msh.addLayoutConstraint( "debug" );

		msh.addRowConstraint( "[][grow][]" );

		setLayout( msh.createMigLayout() );

		topCorner = new FixedSizeTransparentCorner( ci.rtbi );
		rBorder = new FixedXTransparentBorder( ci.ribi );
		bottomCorner = new FixedSizeTransparentCorner( ci.rbbi );

		this.add( topCorner, "growx, wrap" );
		this.add( rBorder, "grow, wrap" );
		this.add( bottomCorner, "growx" );

		final Dimension size = new Dimension( ci.ltbi.getWidth(), ci.ltbi.getHeight() + ci.lbbi.getHeight() + 1 );
		this.setPreferredSize( size );
	}
}
