-- *******************************************************************************
-- Copyright (c) 2017 Eurotech and/or its affiliates and others
--
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Eurotech - initial API and implementation
-- *******************************************************************************

-- liquibase formatted sql

-- changeset atht_credential-expiration:1

ALTER TABLE atht_credential
  ADD expiration_date             	TIMESTAMP(3);
ALTER TABLE atht_credential
  ADD credential_status             VARCHAR(64)	  NOT NULL DEFAULT 'ENABLED';