package org.nmdp.hmlfhirconverterapi.controller;

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

import org.nmdp.hmlfhirconverterapi.service.HmlService;
import org.nmdp.hmlfhirconvertermodels.dto.hml.Hml;
import org.nmdp.kafkaproducer.kafka.KafkaProducerService;

import org.nmdp.kafkaproducer.util.ConvertToKafkaMessage;
import org.nmdp.servicekafkaproducermodel.models.KafkaMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.swagger.api.hml.HmlApi;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/hml")
@CrossOrigin
public class HmlController implements HmlApi {

    private static final Logger LOG = Logger.getLogger(HmlController.class);
    private final HmlService hmlService;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public HmlController(HmlService hmlService, KafkaProducerService kafkaProducerService) {
        this.hmlService = hmlService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PATCH)
    public Callable<ResponseEntity<Boolean>> convert(@RequestBody String xml) {
        try {
            List<Hml> hmls = hmlService.convertStringToHmls(xml, "ns2:");
            Map<String, Hml> dbHmls = hmlService.writeHmlToMongoConversionDb(hmls);
            List<KafkaMessage> kafkaMessages = ConvertToKafkaMessage.transform(dbHmls, "key");
            kafkaProducerService.produceKafkaMessages(kafkaMessages, "hml-fhir-conversion", "andrew-mbp");
            return () -> new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error in file upload hml to fhir conversion.", ex);
            return () -> new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Callable<ResponseEntity<Boolean>> convertHmlFileToFhir(@RequestBody MultipartFile file) {
        try {
            List<Hml> hmls = hmlService.convertByteArrayToHmls(file.getBytes(), "ns2:");
            Map<String, Hml> dbHmls = hmlService.writeHmlToMongoConversionDb(hmls);
            List<KafkaMessage> kafkaMessages = ConvertToKafkaMessage.transform(dbHmls, "key");
            kafkaProducerService.produceKafkaMessages(kafkaMessages, "hml-fhir-conversion", "andrew-mbp");
            return () -> new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error in file upload hml to fhir conversion.", ex);
            return () -> new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
