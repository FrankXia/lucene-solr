/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.metrics;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.codahale.metrics.Counter;
import org.apache.lucene.util.TestUtil;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrInfoMBean;

public final class SolrMetricTestUtils {

  private static final int                    MAX_ITERATIONS = 100;
  private static final SolrInfoMBean.Category CATEGORIES[]   = SolrInfoMBean.Category.values();

  public static String getRandomScope(Random random) {
    return getRandomScope(random, random.nextBoolean());
  }

  public static String getRandomScope(Random random, boolean shouldDefineScope) {
    return shouldDefineScope ? TestUtil.randomSimpleString(random, 1, 10) : null; // must be simple string for JMX publishing
  }

  public static SolrInfoMBean.Category getRandomCategory(Random random) {
    return getRandomCategory(random, random.nextBoolean());
  }

  public static SolrInfoMBean.Category getRandomCategory(Random random, boolean shouldDefineCategory) {
    return shouldDefineCategory ? CATEGORIES[TestUtil.nextInt(random, 0, CATEGORIES.length - 1)] : null;
  }

  public static Map<String, Counter> getRandomMetrics(Random random) {
    return getRandomMetrics(random, random.nextBoolean());
  }

  public static Map<String, Counter> getRandomMetrics(Random random, boolean shouldDefineMetrics) {
    return shouldDefineMetrics ? getRandomMetricsWithReplacements(random, new HashMap<>()) : null;
  }

  public static final String SUFFIX = "_testing";

  public static Map<String, Counter> getRandomMetricsWithReplacements(Random random, Map<String, Counter> existing) {
    HashMap<String, Counter> metrics = new HashMap<>();
    ArrayList<String> existingKeys = new ArrayList<>(existing.keySet());

    int numMetrics = TestUtil.nextInt(random, 1, MAX_ITERATIONS);
    for (int i = 0; i < numMetrics; ++i) {
      boolean shouldReplaceMetric = !existing.isEmpty() && random.nextBoolean();
      String name = shouldReplaceMetric
          ? existingKeys.get(TestUtil.nextInt(random, 0, existingKeys.size() - 1))
          : TestUtil.randomSimpleString(random, 1, 10) + SUFFIX; // must be simple string for JMX publishing

      Counter counter = new Counter();
      counter.inc(random.nextLong());
      metrics.put(name, counter);
    }

    return metrics;
  }

  public static SolrMetricProducer getProducerOf(SolrInfoMBean.Category category, String scope, Map<String, Counter> metrics) {
    return new SolrMetricProducer() {
      @Override
      public Collection<String> initializeMetrics(String registry, String scope) {
        if (metrics == null || metrics.isEmpty()) {
          return Collections.emptyList();
        }
        for (Map.Entry<String, Counter> entry : metrics.entrySet()) {
          SolrMetricManager.getOrCreateCounter(registry, entry.getKey(), category.toString(), scope);
        }
        return metrics.keySet();
      }

      @Override
      public String getName() {
        return scope;
      }

      @Override
      public String getVersion() {
        return "0.0";
      }

      @Override
      public String getDescription() {
        return "foo";
      }

      @Override
      public Category getCategory() {
        return category;
      }

      @Override
      public String getSource() {
        return null;
      }

      @Override
      public URL[] getDocs() {
        return new URL[0];
      }

      @Override
      public NamedList getStatistics() {
        return null;
      }

      @Override
      public String toString() {
        return "SolrMetricProducer.of{" +
            "\ncategory=" + category +
            "\nscope=" + scope +
            "\nmetrics=" + metrics +
            "\n}";
      }
    };
  }
}
