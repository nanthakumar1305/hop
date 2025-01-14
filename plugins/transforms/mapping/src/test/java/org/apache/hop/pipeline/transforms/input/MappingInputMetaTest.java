/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.pipeline.transforms.input;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.metadata.serializer.memory.MemoryMetadataProvider;
import org.apache.hop.metadata.serializer.xml.XmlMetadataUtil;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transforms.loadsave.LoadSaveTester;
import org.apache.hop.pipeline.transforms.loadsave.validator.ArrayLoadSaveValidator;
import org.apache.hop.pipeline.transforms.loadsave.validator.IFieldLoadSaveValidator;
import org.apache.hop.pipeline.transforms.loadsave.validator.NonZeroIntLoadSaveValidator;
import org.apache.hop.pipeline.transforms.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.apache.hop.pipeline.transforms.loadsave.validator.StringLoadSaveValidator;
import org.apache.hop.pipeline.transforms.mapping.SimpleMappingMeta;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MappingInputMetaTest {
  @Before
  public void setUp() throws Exception {
    HopEnvironment.init();
    PluginRegistry.init();
  }

  @Test
  public void clonesCorrectly() throws Exception {
    MappingInputMeta meta = new MappingInputMeta();
    meta.getFields().add(new InputField("f1", "Integer", "1", "3"));
    meta.getFields().add(new InputField("f2", "String", "2", "4"));

    meta.setChanged();

    MappingInputMeta copy = meta.clone();

    assertEquals(meta.getFields().size(), copy.getFields().size());
    for (int i=0;i<meta.getFields().size();i++) {
      InputField metaField = meta.getFields().get(i);
      InputField copyField = copy.getFields().get(i);
      assertEquals(metaField, copyField);
    }
  }

  @Test
  public void testSerialization() throws HopException {
    Document document =
            XmlHandler.loadXmlFile(this.getClass().getResourceAsStream("/mapping-input-transform.xml"));
    Node transformNode = XmlHandler.getSubNode(document, TransformMeta.XML_TAG);
    SimpleMappingMeta meta = new SimpleMappingMeta();
    XmlMetadataUtil.deSerializeFromXml(
            null, transformNode, SimpleMappingMeta.class, meta, new MemoryMetadataProvider());
    String xml =
            XmlHandler.openTag(TransformMeta.XML_TAG)
                    + meta.getXml()
                    + XmlHandler.closeTag(TransformMeta.XML_TAG);

    Document copyDocument = XmlHandler.loadXmlString(xml);
    Node copyNode = XmlHandler.getSubNode(copyDocument, TransformMeta.XML_TAG);
    SimpleMappingMeta copy = new SimpleMappingMeta();
    XmlMetadataUtil.deSerializeFromXml(
            null, copyNode, SimpleMappingMeta.class, copy, new MemoryMetadataProvider());
    Assert.assertEquals(meta.getXml(), copy.getXml());
  }
}
