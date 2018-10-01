/**
 * Copyright (c) 2015-2017 Obeo, Inria
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors:
 * - William Piers <william.piers@obeo.fr>
 * - Philippe Merle <philippe.merle@inria.fr>
 * - Faiez Zalila <faiez.zalila@inria.fr>
 */
package org.modmacao.mongodb;

import org.eclipse.cmf.occi.core.MixinBase;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Cluster</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.modmacao.mongodb.MongodbPackage#getCluster()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='OneOrMoreRouters OneOrMoreShards OneOrMoreConfigServers'"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore/OCL/Pivot OneOrMoreRouters='true' OneOrMoreShards='true' OneOrMoreConfigServers='true'"
 * @generated
 */
public interface Cluster extends modmacao.Cluster, MixinBase {
} // Cluster