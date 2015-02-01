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

	@Override
	public int[] sqlTypes()
	{
		return SQL_TYPES;
	}

	@Override
	public Class<?> returnedClass()
	{
		return Enum.class;
	}

	@Override
	public boolean equals(final Object obj1, final Object obj2) throws HibernateException // NOPMD by dan on 01/02/15 07:29
	{
		if (obj1 == obj2)
			return true;
		if (obj1 == null || obj2 == null)
			return false;
		return ((Enum<?>) obj1).equals((obj2));
	}

	@Override
	public int hashCode(final Object object) throws HibernateException
	{
		return ((Enum<?>) object).hashCode();
	}

	private String getRepresentation(final Object object)
	{
		return object.getClass().getName() + " " + ((Enum<?>) object).name();
	}

	private Object getObject(final String representation) throws HibernateException
	{
		try
		{
			final String[] parts = representation.split(" ");
			final Class<?> c = Class.forName(parts[0]);
			final Field field = c.getField(parts[1]);
			return field.get(null);
		}
		catch (final Exception e)
		{
			throw new HibernateException(e);
		}
	}

	public Object nullSafeGet(final ResultSet resultSet, final String[] names, final Object owner) throws HibernateException,
			SQLException
	{
		if (resultSet.wasNull())
			return null;
		final String value = resultSet.getString(names[0]);
		return getObject(value);
	}

	public void nullSafeSet(final PreparedStatement statement, final Object value, final int index) throws HibernateException,
			SQLException
	{
		if (value == null)
			statement.setNull(index, Types.VARCHAR);
		else
		{
			final String representation = getRepresentation(value);
			statement.setString(index, representation);
		}
	}

	@Override
	public Object deepCopy(final Object value) throws HibernateException
	{
		return value;
	}

	@Override
	public boolean isMutable()
	{
		return false;
	}

	@Override
	public Serializable disassemble(final Object object) throws HibernateException
	{
		return getRepresentation(object);
	}

	@Override
	public Object assemble(final Serializable serializable, final Object owner) throws HibernateException
	{
		return getObject((String) serializable);
	}

	@Override
	public Object replace(final Object original, final Object target, final Object owner) throws HibernateException
	{
		return original;
	}

	@Override
	public Object nullSafeGet( final ResultSet arg0, final String[] arg1, final SessionImplementor arg2, final Object arg3 )
			throws HibernateException, SQLException
	{
		return null;
	}

	@Override
	public void nullSafeSet( final PreparedStatement arg0, final Object arg1, final int arg2, final SessionImplementor arg3 )
			throws HibernateException, SQLException
	{
		// do nothing, we don't use this bit
	}

}
