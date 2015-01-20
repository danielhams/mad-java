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
	private ArrayList<String> layoutConstraints = new ArrayList<String>();
	private ArrayList<String> columnConstraints = new ArrayList<String>();
	private ArrayList<String> rowConstraints = new ArrayList<String>();
	
	public MigLayoutStringHelper()
	{
	}
	
	public void addLayoutConstraint( String constraint )
	{
		layoutConstraints.add( constraint );
	}
	
	public void addColumnConstraint( String constraint )
	{
		columnConstraints.add( constraint );
	}
	
	public void addRowConstraint( String constraint )
	{
		rowConstraints.add( constraint );
	}
	
	public MigLayout createMigLayout()
	{
		String layoutConstraintStr = buildConstraintString( layoutConstraints, true );
		String columnConstraintStr = buildConstraintString( columnConstraints, false );
		String rowConstraintStr = buildConstraintString( rowConstraints, false );
		
		MigLayout retVal = new MigLayout( layoutConstraintStr, columnConstraintStr, rowConstraintStr );
		return retVal;
	}
	
	private String buildConstraintString( ArrayList<String> constraintList, boolean commanSeparated )
	{
		StringBuilder sb = new StringBuilder();

		int curCount = 0;
		
		for( String s : constraintList )
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
