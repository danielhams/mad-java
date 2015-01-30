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

import java.util.ArrayList;

import net.miginfocom.swing.MigLayout;

public class MigLayoutStringHelper
{
	private final ArrayList<String> layoutConstraints = new ArrayList<String>();
	private final ArrayList<String> columnConstraints = new ArrayList<String>();
	private final ArrayList<String> rowConstraints = new ArrayList<String>();

	public MigLayoutStringHelper()
	{
	}

	public void addLayoutConstraint( final String constraint )
	{
		layoutConstraints.add( constraint );
	}

	public void addColumnConstraint( final String constraint )
	{
		columnConstraints.add( constraint );
	}

	public void addRowConstraint( final String constraint )
	{
		rowConstraints.add( constraint );
	}

	public MigLayout createMigLayout()
	{
		final String layoutConstraintStr = buildConstraintString( layoutConstraints, true );
		final String columnConstraintStr = buildConstraintString( columnConstraints, false );
		final String rowConstraintStr = buildConstraintString( rowConstraints, false );

		final MigLayout retVal = new MigLayout( layoutConstraintStr, columnConstraintStr, rowConstraintStr );
		return retVal;
	}

	private String buildConstraintString( final ArrayList<String> constraintList, final boolean commanSeparated )
	{
		final StringBuilder sb = new StringBuilder();

		int curCount = 0;

		for( final String s : constraintList )
		{
			if( curCount > 0 && commanSeparated )
			{
				sb.append( ", " );
			}
			sb.append( s );
			curCount++;
		}
		return sb.toString();
	}
}
