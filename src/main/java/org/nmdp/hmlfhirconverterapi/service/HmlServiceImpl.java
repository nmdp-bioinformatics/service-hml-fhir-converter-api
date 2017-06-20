package org.nmdp.hmlfhirconverterapi.service;

/**
 * Created by Andrew S. Brown, Ph.D., <andrew@nmdp.org>, on 5/26/17.
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
import org.nmdp.hmlfhir.ConvertHmlToFhir;
import org.nmdp.hmlfhir.ConvertHmlToFhirImpl;
import org.nmdp.hmlfhir.deserialization.HmlXmlDeserializerHyphenatedProperties;
import org.nmdp.hmlfhirconverterapi.dao.HmlRepository;
import org.nmdp.hmlfhirconverterapi.dao.custom.HmlCustomRepository;
import org.nmdp.hmlfhirconvertermodels.dto.hml.Hml;
import org.nmdp.hmlfhirmongo.models.ConversionStatus;
import org.nmdp.hmlfhirmongo.models.Status;
import org.nmdp.hmlfhirmongo.mongo.MongoConversionStatusDatabase;
import org.nmdp.hmlfhirmongo.mongo.MongoHmlDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HmlServiceImpl implements HmlService {

    private final static Logger LOG = Logger.getLogger(HmlServiceImpl.class);
    private final HmlCustomRepository customRepository;
    private final HmlRepository repository;
    private final Yaml yaml;

    @Autowired
    public HmlServiceImpl(@Qualifier("hmlRepository") HmlRepository repository, @Qualifier("hmlCustomRepository")HmlCustomRepository customRepository) {
        this.repository = repository;
        this.customRepository = customRepository;
        this.yaml = new Yaml();
    }

    @Override
    public List<Hml> convertByteArrayToHmls(byte[] bytes, String xmlPrefix) throws Exception {
        try {
            HmlXmlDeserializerHyphenatedProperties deserializer = new HmlXmlDeserializerHyphenatedProperties();
            ConvertHmlToFhir converter = new ConvertHmlToFhirImpl(deserializer);
            List<org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> hmls = new ArrayList<>();
            hmls.add(converter.convertToDto(new String(bytes), xmlPrefix));

            return hmls;
        } catch (Exception ex) {
            LOG.error("Error converting file to HML.", ex);
            throw ex;
        }
    }

    @Override
    public Map<String, Hml> writeHmlToMongoConversionDb(List<org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> hmls) {
        List<org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> ids = new ArrayList<>();
        org.nmdp.hmlfhirmongo.config.MongoConfiguration config = null;

        try {
            URL url = new URL("file:." + "/src/main/resources/mongo-configuration.yaml");

            try (InputStream is = url.openStream()) {
                config = yaml.loadAs(is, org.nmdp.hmlfhirmongo.config.MongoConfiguration.class);
            }

            final MongoHmlDatabase database = new MongoHmlDatabase(config);

            for (org.nmdp.hmlfhirconvertermodels.dto.hml.Hml hml : hmls) {
                ids.add(database.save(hml));
            }
        } catch (Exception ex) {
            LOG.error("Error writing Hml to Mongo.", ex);
        }

        return writeConversionStatusToMongo(ids, config);
    }

    @Override
    public List<org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> convertStringToHmls(String xml, String xmlPrefix) throws Exception {
        try {
            HmlXmlDeserializerHyphenatedProperties deserializer = new HmlXmlDeserializerHyphenatedProperties();
            ConvertHmlToFhir converter = new ConvertHmlToFhirImpl(deserializer);
            List<org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> hmls = new ArrayList<>();
            hmls.add(converter.convertToDto(xml, xmlPrefix));

            return hmls;
        } catch (Exception ex) {
            LOG.error("Error converting file to HML.", ex);
            throw ex;
        }
    }

    private Map<String, org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> writeConversionStatusToMongo(
            List<org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> hmls, org.nmdp.hmlfhirmongo.config.MongoConfiguration config) {
        Map<String, org.nmdp.hmlfhirconvertermodels.dto.hml.Hml> ids = new HashMap<>();

        try {
            final MongoConversionStatusDatabase database = new MongoConversionStatusDatabase(config);

            for (org.nmdp.hmlfhirconvertermodels.dto.hml.Hml hml : hmls) {
                ConversionStatus status = new ConversionStatus(hml.getId(), Status.QUEUED, 0);
                ids.put(database.save(status).getId(), hml);
            }
        } catch (Exception ex) {
            LOG.error("Error writing status to Mongo.", ex);
        }

        return ids;
    }
}
