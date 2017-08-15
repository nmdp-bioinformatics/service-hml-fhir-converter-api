package org.nmdp.hmlfhirconverterapi.service;

/**
 * Created by Andrew S. Brown, Ph.D., <andrew@nmdp.org>, on 6/20/17.
 * <p>
 * service-hml-fhir-converter-api
 * Copyright (c) 2012-2017 National Marrow Donor Program (NMDP)
 * <p>
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library;  if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.
 * <p>
 * > http://www.fsf.org/licensing/licenses/lgpl.html
 * > http://www.opensource.org/licenses/lgpl-license.php
 */

import org.apache.log4j.Logger;

import org.nmdp.hmlfhirconverterapi.dao.StatusRepository;
import org.nmdp.hmlfhirconverterapi.dao.custom.StatusCustomRepository;
import org.nmdp.hmlfhirmongo.models.ConversionStatus;
import org.nmdp.hmlfhirmongo.models.Status;
import org.nmdp.hmlfhirmongo.mongo.MongoConversionStatusDatabase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.yaml.snakeyaml.Yaml;

import org.bson.Document;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Service
public class StatusServiceImpl extends BaseService implements StatusService {

    private final Yaml yaml;
    private static final Logger LOG = Logger.getLogger(StatusServiceImpl.class);
    private final StatusCustomRepository customRepository;
    private final StatusRepository repository;
    private final MongoConversionStatusDatabase database;

    @Autowired
    public StatusServiceImpl(@Qualifier("statusCustomRepository") StatusCustomRepository customRepository,
        @Qualifier("statusRepository") StatusRepository repository) {
        this.yaml = new Yaml();
        this.customRepository = customRepository;
        this.repository = repository;
        org.nmdp.hmlfhirmongo.config.MongoConfiguration config = null;

        try {
            URL url = new URL("file:." + "/src/main/resources/mongo-configuration.yaml");
            try (InputStream is = url.openStream()) {
                config = yaml.loadAs(is, org.nmdp.hmlfhirmongo.config.MongoConfiguration.class);
            }
        } catch(Exception ex) {
            LOG.error("Error instantiating ConversionStatus database.", ex);
        } finally {
            this.database = new MongoConversionStatusDatabase(config);
        }
    }

    @Override
    public List<Document> getStatuses(Integer maxReturn) throws Exception {
        try {
            return handleMongoId(database.getMany(maxReturn));
        } catch (Exception ex) {
            LOG.error("Error reading Statuses from Mongo.", ex);
            throw ex;
        }
    }

    @Override
    public Document getStatus(String id) throws Exception {
        try {
            return convertId(database.get(id));
        } catch (Exception ex) {
            LOG.error("Error reading Status from Mongo.", ex);
            throw ex;
        }
    }

    @Override
    public void updateStatusStatus(Status status, Document statusRow) throws Exception {
        try {
            Object id = statusRow.remove("_id");
            database.update(id.toString(), status);
        } catch (Exception ex) {
            LOG.error("Error updating Status in Mongo.", ex);
            throw ex;
        }
    }
}
