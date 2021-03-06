/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Original Work: Apache License, Version 2.0, Copyright 2017 Hans-Peter Grahsl.
 */
package io.ctsi.tenet.kafka.mongodb.sink.processor;


import io.ctsi.tenet.kafka.mongodb.sink.MongoSinkTopicConfig;
import io.ctsi.tenet.kafka.mongodb.util.ClassHelper;
import io.ctsi.tenet.kafka.mongodb.util.ConnectConfigException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

public final class PostProcessors {
  private final List<PostProcessor> postProcessorList;

  public PostProcessors(final MongoSinkTopicConfig config, final List<String> classes) {
    List<PostProcessor> postProcessors = new ArrayList<>();
    boolean hasDocumentIdAdder = false;

    for (String c : classes) {
      if (c.equals(DocumentIdAdder.class.getName())) {
        hasDocumentIdAdder = true;
      }

      try {
        postProcessors.add(
            ClassHelper.createInstance(
                MongoSinkTopicConfig.POST_PROCESSOR_CHAIN_CONFIG,
                c,
                PostProcessor.class,
                singletonList(MongoSinkTopicConfig.class),
                singletonList(config)));
      } catch (ConnectConfigException e) {
        throw new ConnectConfigException(e.getName(), classes, e.getOriginalMessage());
      }
    }

    if (!hasDocumentIdAdder) {
      postProcessors.add(0, new DocumentIdAdder(config));
    }

    this.postProcessorList = unmodifiableList(postProcessors);
  }

  public List<PostProcessor> getPostProcessorList() {
    return postProcessorList;
  }
}
