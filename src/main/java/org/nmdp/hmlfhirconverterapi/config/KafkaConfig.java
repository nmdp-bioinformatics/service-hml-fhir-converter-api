package org.nmdp.hmlfhirconverterapi.config;

/**
 * Created by Andrew S. Brown, Ph.D., <andrew@nmdp.org>, on 6/22/17.
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
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;

public class KafkaConfig {

    private static final Logger LOG = Logger.getLogger(KafkaConfig.class);

    private String topic;
    private String messageKey;
    private String key;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static KafkaConfig getConfig() {
        Yaml yaml = new Yaml();
        KafkaConfig config = new KafkaConfig();

        try {
            URL url = new URL("file:." + "/src/main/resources/kafka-configuration.yaml");

            try (InputStream is = url.openStream()) {
                config = yaml.loadAs(is, KafkaConfig.class);
            }
        } catch (Exception ex) {
            LOG.error(ex);
        } finally {
            return config;
        }
    }
}
