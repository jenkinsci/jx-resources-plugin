/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.jx.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 */
public class KubernetesUtilsDateFormatterTest {

    private static final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTimeNoMillis();

    /**
     * Old date format (replaced in fc577e17d53de846f2763470f6c1f6c98f7777b3)
     * @param timestamp
     * @return date as string
     */
    private static String formatTimestamp(long timestamp) {
        return dateFormatter.print(new DateTime(timestamp));
    }

    @Test
    public void testFormatTimestamp() throws Exception {
        Instant now = Instant.now();
        long testTimeInMillis = 0;
        String expected = null;
        String actual = null;
        // hours
        for (int i = 0; i < 25; i++) {
            Duration duration = Duration.ofHours(1 * i);
            testTimeInMillis = now.plus(duration).toEpochMilli();
            expected = formatTimestamp(testTimeInMillis);
            actual = KubernetesUtils.formatTimestamp(testTimeInMillis);
            assertEquals(expected, actual);
        }
        // days
        for (int i = 0; i < 32; i++) {
            Duration duration = Duration.ofDays(1 * i);
            testTimeInMillis = now.plus(duration).toEpochMilli();
            expected = formatTimestamp(testTimeInMillis);
            actual = KubernetesUtils.formatTimestamp(testTimeInMillis);
            assertEquals(expected, actual);
        }
        // months-ish (days * 30)
        for (int i = 0; i < 13; i++) {
            Duration duration = Duration.ofDays(30 * i);
            testTimeInMillis = now.plus(duration).toEpochMilli();
            expected = formatTimestamp(testTimeInMillis);
            actual = KubernetesUtils.formatTimestamp(testTimeInMillis);
            assertEquals(expected, actual);
        }
    }
}
