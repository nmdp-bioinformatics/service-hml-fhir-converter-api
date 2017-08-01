package org.nmdp.hmlfhirconverterapi.service;

/**
 * Created by Andrew S. Brown, Ph.D., <andrew@nmdp.org>, on 7/31/17.
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

import org.nmdp.kafkaproducer.kafka.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger LOG = Logger.getLogger(SubmissionServiceImpl.class);
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public SubmissionServiceImpl(@Qualifier("kafkaProducerService") KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public boolean produceSubmissionMessage(String statusId) {

    }
}
