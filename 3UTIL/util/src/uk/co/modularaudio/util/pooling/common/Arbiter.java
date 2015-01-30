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

package uk.co.modularaudio.util.pooling.common;

/**
 * <P>Used by the resource pool to execute client defined code at various stages
 * in the resource lifecycle.</P>
 * <P>The resource pool can have any number of arbiters set at various stages.
 * They are:
 * </P>
 * <UL>
 * <LI>Creation - The arbiter is called after the resource is created.</LI>
 * <LI>PreUse   - The arbiter is called before the resource is retrieved from the pool.</LI>
 * <LI>PostUse  - The arbiter is called before the resource is returned to the client.</LI>
 * <LI>PreRelease - The arbiter is called before the resource is returned to the pool.</LI>
 * <LI>PostRelease - The arbiter is called after the resource is returned to the pool.</LI>
 * <LI>Expiration - The arbiter is called to determine if a particular resource has expired.</LI>
 * <LI>Removal - The arbiter is called before the resource is removed entirely from the pool.</LI>
 * </UL>
 * <P>The arbiters are stored in a chain for each of the above scenarios, and the return value from the arbiter 'arbitrateResource' method determines whether any further arbiters in the list are executed.</P>
 * <P>The return values and meanings for 'arbitrateResource' are as follows:
 * </P>
 * <UL>
 * <LI>CONTINUE - Continue to process all further arbiters in this chain.</LI>
 * <LI>FINISH - Do not process further arbiters in this chain, but continue with the operation.</LI>
 * <LI>FAIL - Do not process further arbiters in this chain, the operation should fail.</LI>
 * </UL>
 * <P><font color="#ff0000">An arbiter is <B>guaranteed</B> to have exclusive locked access to the pool during its arbitration.</font></P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.Pool
 **/
public interface Arbiter
{
  int arbitrateOnResource(Pool pool, PoolStructure data, Resource res);

  public static int CONTINUE = 0;
  public static int FINISH   = 1;
  public static int FAIL     = 2;
}
