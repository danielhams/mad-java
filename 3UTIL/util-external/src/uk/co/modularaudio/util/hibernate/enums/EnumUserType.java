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

package uk.co.modularaudio.util.hibernate.enums;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

public class EnumUserType implements UserType
{

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	public EnumUserType()
	{
		super();
	}

	public int[] sqlTypes()
	{
		return SQL_TYPES;
	}

	public Class<?> returnedClass()
	{
		return Enum.class;
	}

	public boolean equals(Object obj1, Object obj2) throws HibernateException
	{
		if (obj1 == obj2)
			return true;
		if (obj1 == null || obj2 == null)
			return false;
		return ((Enum<?>) obj1).equals(((Enum<?>) obj2));
	}

	public int hashCode(Object object) throws HibernateException
	{
		return ((Enum<?>) object).hashCode();
	}

	private String getRepresentation(Object object)
	{
		return object.getClass().getName() + " " + ((Enum<?>) object).name();
	}

	private Object getObject(String representation) throws HibernateException
	{
		try
		{
			String[] parts = representation.split(" ");
			Class<?> c = Class.forName(parts[0]);
			Field field = c.getField(parts[1]);
			return field.get(null);
		}
		catch (Exception e)
		{
			throw new HibernateException(e);
		}
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException,
			SQLException
	{
		if (resultSet.wasNull())
			return null;
		String value = resultSet.getString(names[0]);
		return getObject(value);
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException,
			SQLException
	{
		if (value == null)
			statement.setNull(index, Types.VARCHAR);
		else
		{
			String representation = getRepresentation(value);
			statement.setString(index, representation);
		}
	}

	public Object deepCopy(Object value) throws HibernateException
	{
		return value;
	}

	public boolean isMutable()
	{
		return false;
	}

	public Serializable disassemble(Object object) throws HibernateException
	{
		return getRepresentation(object);
	}

	public Object assemble(Serializable serializable, Object owner) throws HibernateException
	{
		return getObject((String) serializable);
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException
	{
		return original;
	}

	@Override
	public Object nullSafeGet( ResultSet arg0, String[] arg1, SessionImplementor arg2, Object arg3 )
			throws HibernateException, SQLException
	{
		return null;
	}

	@Override
	public void nullSafeSet( PreparedStatement arg0, Object arg1, int arg2, SessionImplementor arg3 )
			throws HibernateException, SQLException
	{
	}

}
