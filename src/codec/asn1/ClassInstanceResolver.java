/* ========================================================================
 *
 *  This file is part of CODEC, which is a Java package for encoding
 *  and decoding ASN.1 data structures.
 *
 *  Author: Fraunhofer Institute for Computer Graphics Research IGD
 *          Department A8: Security Technology
 *          Fraunhoferstr. 5, 64283 Darmstadt, Germany
 *
 *  Rights: Copyright (c) 2004 by Fraunhofer-Gesellschaft 
 *          zur Foerderung der angewandten Forschung e.V.
 *          Hansastr. 27c, 80686 Munich, Germany.
 *
 * ------------------------------------------------------------------------
 *
 *  The software package is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation; either version 2.1 of the 
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public 
 *  License along with this software package; if not, write to the Free 
 *  Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 *  MA 02110-1301, USA or obtain a copy of the license at 
 *  http://www.fsf.org/licensing/licenses/lgpl.txt.
 *
 * ------------------------------------------------------------------------
 *
 *  The CODEC library can solely be used and distributed according to 
 *  the terms and conditions of the GNU Lesser General Public License for 
 *  non-commercial research purposes and shall not be embedded in any 
 *  products or services of any user or of any third party and shall not 
 *  be linked with any products or services of any user or of any third 
 *  party that will be commercially exploited.
 *
 *  The CODEC library has not been tested for the use or application 
 *  for a determined purpose. It is a developing version that can 
 *  possibly contain errors. Therefore, Fraunhofer-Gesellschaft zur 
 *  Foerderung der angewandten Forschung e.V. does not warrant that the 
 *  operation of the CODEC library will be uninterrupted or error-free. 
 *  Neither does Fraunhofer-Gesellschaft zur Foerderung der angewandten 
 *  Forschung e.V. warrant that the CODEC library will operate and 
 *  interact in an uninterrupted or error-free way together with the 
 *  computer program libraries of third parties which the CODEC library 
 *  accesses and which are distributed together with the CODEC library.
 *
 *  Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. 
 *  does not warrant that the operation of the third parties's computer 
 *  program libraries themselves which the CODEC library accesses will 
 *  be uninterrupted or error-free.
 *
 *  Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. 
 *  shall not be liable for any errors or direct, indirect, special, 
 *  incidental or consequential damages, including lost profits resulting 
 *  from the combination of the CODEC library with software of any user 
 *  or of any third party or resulting from the implementation of the 
 *  CODEC library in any products, systems or services of any user or 
 *  of any third party.
 *
 *  Fraunhofer-Gesellschaft zur Foerderung der angewandten Forschung e.V. 
 *  does not provide any warranty nor any liability that utilization of 
 *  the CODEC library will not interfere with third party intellectual 
 *  property rights or with any other protected third party rights or will 
 *  cause damage to third parties. Fraunhofer Gesellschaft zur Foerderung 
 *  der angewandten Forschung e.V. is currently not aware of any such 
 *  rights.
 *
 *  The CODEC library is supplied without any accompanying services.
 *
 * ========================================================================
 */
package codec.asn1;

/**
 * Resolves requests for instances based on a class object. New instances are
 * generated by invoking <code>newInstance()
 * </code> on the wrapped class
 * object. The class must provide a no-arg default constructor.
 * 
 * @author Volker Roth
 * @version "$Id: ClassInstanceResolver.java,v 1.2 2000/12/06 17:47:27 vroth Exp $"
 */
public class ClassInstanceResolver extends Object implements Resolver {
    /**
     * The class that is used as a factory.
     */
    private Class factory_;

    /**
     * Creates an instance with the given factory class.
     * 
     * @param factory
     *                The factory class.
     * @throws IllegalArgumentException
     *                 if <code>factory</code> has no default constructor, or
     *                 does not implement <code>
     *   ASN1Type</code>.
     */
    public ClassInstanceResolver(Class factory) {
	if (factory == null) {
	    throw new NullPointerException("Need a factory class!");
	}
	try {
	    factory.getConstructor(new Class[0]);
	} catch (NoSuchMethodException e) {
	    throw new IllegalArgumentException("Class " + factory.getName()
		    + " has no default constructor!");
	}
	if (!ASN1Type.class.isAssignableFrom(factory)) {
	    throw new IllegalArgumentException("Class " + factory.getName()
		    + " is not an ASN1Type!");
	}
	factory_ = factory;
    }

    /**
     * Returns the class object of the factory class.
     * 
     * @return The class object.
     */
    public Class getFactoryClass() {
	return factory_;
    }

    /**
     * Resolves the call by returning a new instance of the the factory class.
     * The factory class must have been specified at the time of constructing
     * this instance.
     * 
     * @param caller
     *                The caller. The caller is ignored.
     * @throws ResolverException
     *                 if for some reason the call cannot be resolved.
     */
    public ASN1Type resolve(ASN1Type caller) throws ResolverException {
	try {
	    return (ASN1Type) factory_.newInstance();
	} catch (Exception e) {
	    throw new ResolverException("Caught " + e.getClass().getName()
		    + "(\"" + e.getMessage() + "\")");
	}
    }

}
