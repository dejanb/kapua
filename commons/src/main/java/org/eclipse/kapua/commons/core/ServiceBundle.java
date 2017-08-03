/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.core;

import org.eclipse.kapua.KapuaException;

/**
 * Bundle manages the lifecycle of the services
 * 
 * @since 0.3.0
 */
public interface ServiceBundle {
    
    public void start() throws KapuaException;
    public void stop() throws KapuaException;
}
