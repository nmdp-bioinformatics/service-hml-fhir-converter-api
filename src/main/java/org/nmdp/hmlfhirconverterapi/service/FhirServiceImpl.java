package org.nmdp.hmlfhirconverterapi.service;

/**
 * Created by Andrew S. Brown, Ph.D., <andrew@nmdp.org>, on 6/8/17.
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
import org.nmdp.hmlfhir.ConvertFhirToHml;
import org.nmdp.hmlfhir.ConvertFhirToHmlImpl;
import org.nmdp.hmlfhirconvertermodels.domain.fhir.FhirMessage;
import org.nmdp.hmlfhirmongo.config.MongoConfiguration;
import org.nmdp.hmlfhirmongo.models.ConversionStatus;
import org.nmdp.hmlfhirmongo.models.Status;
import org.nmdp.hmlfhirmongo.mongo.MongoConversionStatusDatabase;
import org.nmdp.hmlfhirmongo.mongo.MongoFhirDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FhirServiceImpl {

    private final MongoConfiguration mongoConfiguration;
    private final Yaml yaml;
    private static final Logger LOG = Logger.getLogger(FhirServiceImpl.class);

    @Autowired
    public FhirServiceImpl(@Qualifier("mongoConfiguration") MongoConfiguration mongoConfiguration) {
        this.mongoConfiguration = mongoConfiguration;
        this.yaml = new Yaml();
    }

    public Map<String, FhirMessage> writeFhirToMongoConversionDb(List<FhirMessage> fhirMessages) {
        List<FhirMessage> ids = new ArrayList<>();
        org.nmdp.hmlfhirmongo.config.MongoConfiguration config = null;

        try {
            URL url = new URL("file:." + "/src/main/resources/mongo-configuration.yaml");

            try (InputStream is = url.openStream()) {
                config = yaml.loadAs(is, org.nmdp.hmlfhirmongo.config.MongoConfiguration.class);
            }

            final MongoFhirDatabase database = new MongoFhirDatabase(config);

            for (FhirMessage fhirMessage : fhirMessages) {
                ids.add(database.save(fhirMessage));
            }
        } catch (Exception ex) {
            LOG.error("Error writing Fhir to Mongo.", ex);
        }

        return writeConversionStatusToMongo(config, ids);
    }

    public List<FhirMessage> convertByteArrayToFhirMessages(byte[] bytes) throws Exception {
        try {
            ConvertFhirToHml converter = new ConvertFhirToHmlImpl();
            List<FhirMessage> fhirMessages = new ArrayList<>();
            fhirMessages.add(converter.toDto(new String(bytes), null));

            return fhirMessages;
        } catch (Exception ex) {
            LOG.error("Error converting file to FhirMessage.", ex);
            throw ex;
        }
    }

    private Map<String, FhirMessage> writeConversionStatusToMongo(
            org.nmdp.hmlfhirmongo.config.MongoConfiguration config, List<FhirMessage> fhirMessages) {
        Map<String, FhirMessage> ids = new HashMap<>();

        try {
            final MongoConversionStatusDatabase database = new MongoConversionStatusDatabase(config);

            for (FhirMessage fhirMessage : fhirMessages) {
                ConversionStatus status = new ConversionStatus(fhirMessage.getId(), Status.QUEUED, 0);
                ids.put(database.save(status).getId(), fhirMessage);
            }
        } catch (Exception ex) {
            LOG.error("Error writing status to Mongo.", ex);
        }

        return ids;
    }
}
