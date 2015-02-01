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

package uk.co.modularaudio.util.hibernate;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.hibernate.QueryException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.hql.internal.ast.QuerySyntaxException;
import org.hibernate.id.IdentifierGenerationException;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;

public class HibernateExceptionHandler
{

	public static final int ALL_ARE_ERRORS = 0x11111111;

	public static final int JDBC_IS_ERROR = 0x00000001;

	public static final int CONSTRAINT_IS_ERROR = 0x00000010;

	public static void rethrowAsDatastoreLogAll( final HibernateException he, final Log log, final String message )
			throws DatastoreException
	{
		final String internalMess = concMess( message, he );
		if (he instanceof JDBCConnectionException || he instanceof GenericJDBCException
				|| he instanceof SQLGrammarException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof ConstraintViolationException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof LockAcquisitionException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof QuerySyntaxException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof QueryException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
	}

	public static void rethrowJdbcAsDatastore( final HibernateException he, final Log log, final String message, final int logErrorMask ) // NOPMD by dan on 01/02/15 07:21
			throws DatastoreException
	{
		final String internalMess = concMess( message, he );
		if (he instanceof JDBCConnectionException || he instanceof GenericJDBCException
				|| he instanceof SQLGrammarException)
		{
			if ((logErrorMask & JDBC_IS_ERROR) != 0)
			{
				log.error( internalMess, he );
			}
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof ConstraintViolationException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof LockAcquisitionException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof QuerySyntaxException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
	}

	public static void rethrowJdbcAsDatastoreAndConstraintAsItself( final HibernateException he, final Log log, final String message,
			final int logErrorMask ) throws DatastoreException, MAConstraintViolationException
	{
		final String internalMess = concMess( message, he );
		if (he instanceof JDBCConnectionException || he instanceof GenericJDBCException
				|| he instanceof SQLGrammarException)
		{
			if ((logErrorMask & JDBC_IS_ERROR) != 0)
			{
				log.error( internalMess, he );
			}
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof ConstraintViolationException)
		{
			if ((logErrorMask & CONSTRAINT_IS_ERROR) != 0)
			{
				log.error( internalMess, he );
			}
			throw new MAConstraintViolationException( internalMess, he );
		}
		else if (he instanceof LockAcquisitionException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof QuerySyntaxException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof IdentifierGenerationException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else if (he instanceof PropertyValueException)
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
		else
		{
			log.error( internalMess, he );
			throw new DatastoreException( internalMess, he );
		}
	}

	private static String concMess( final String message, final HibernateException he )
	{
		return (message + ": " + he.getMessage());
	}
}
