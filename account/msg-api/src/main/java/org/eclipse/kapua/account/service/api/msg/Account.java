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
package org.eclipse.kapua.account.service.api.msg;

import io.vertx.core.json.JsonObject;

public class Account {

    public Account() {  
    }

    public Account(Account user) {  
    }

    public Account(JsonObject user) {   
    }

    public JsonObject toJson() {
        return (JsonObject)null;
    }
}
