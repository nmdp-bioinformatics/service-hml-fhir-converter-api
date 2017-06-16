package org.nmdp.hmlfhirconverterapi.controller;

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


import io.swagger.api.fhir.FhirApi;

import org.apache.log4j.Logger;

import org.nmdp.hmlfhirconverterapi.service.FhirService;
import org.nmdp.hmlfhirconvertermodels.dto.fhir.FhirMessage;
import org.nmdp.kafkaproducer.kafka.KafkaProducerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class FhirController implements FhirApi {

    private static final Logger LOG = Logger.getLogger(FhirController.class);
    private final FhirService fhirService;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public FhirController(FhirService fhirService, KafkaProducerService kafkaProducerService) {
        this.fhirService = fhirService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @RequestMapping(path = "/fhirToHml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Callable<ResponseEntity<Boolean>> convertFhirFileToHml(@RequestBody MultipartFile file) {
        try {
            List<FhirMessage> fhirMessages = fhirService.convertByteArrayToFhirMessages(file.getBytes());
            Map<String, FhirMessage> dbFhirs = fhirService.writeFhirToMongoConversionDb(fhirMessages);
            kafkaProducerService.produceKafkaMessages(dbFhirs, "fhir-hml-conversion", "andrew-mbp");
            return () -> new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error in file upload fhir to hml conversion.", ex);
            return () -> new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(path = "/fhirToHml", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PATCH)
    public Callable<ResponseEntity<Boolean>> convert(@RequestBody FhirMessage fhirMessage) {
        try {
            Map<String, FhirMessage> dbFhirs = fhirService.writeFhirToMongoConversionDb(Arrays.asList(fhirMessage));
            kafkaProducerService.produceKafkaMessages(dbFhirs, "fhir-hml-conversion", "andrew-mbp");
            return () -> new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error in file upload fhir to hml conversion.", ex);
            return () -> new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
