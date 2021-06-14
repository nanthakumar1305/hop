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

package org.apache.hop.pipeline.transforms.metastructure;

import java.util.List;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.w3c.dom.Node;

@Transform(
  id = "TransformMetaStructure",
  name = "i18n::TransformMetaStructure.Transform.Name",
  description = "i18n::TransformMetaStructure.Transform.Description",
  categoryDescription = "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Utility",
  documentationUrl = "https://hop.apache.org/manual/latest/pipeline/transforms/metastructure.html",
  image = "MetaStructure.svg"
)
public class TransformMetaStructureMeta extends BaseTransformMeta implements ITransformMeta<TransformMetaStructure, TransformMetaStructureData> {

  private static Class<?> PKG = TransformMetaStructureMeta.class; // for i18n purposes, needed by Translator2!!

  private boolean includePositionField;
  private String positionFieldname;
  private boolean includeFieldnameField;
  private String fieldFieldname;
  private boolean includeCommentsField;
  private String commentsFieldname;
  private boolean includeTypeField;
  private String typeFieldname;
  private boolean includeLengthField;
  private String lengthFieldname;
  private boolean includePrecisionField;
  private String precisionFieldname;
  private boolean includeOriginField;
  private String originFieldname;

  private boolean outputRowcount;
  private String rowcountField;

  @Override
  public Object clone() {
    Object clone = super.clone();
    return clone;
  }

  @Override
  public String getXml() {
    StringBuilder xml = new StringBuilder( 500 );

    xml.append( "    " ).append( XmlHandler.addTagValue( "outputRowcount", outputRowcount ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "rowcountField", rowcountField ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "includePositionField", includePositionField) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "positionFieldname", positionFieldname ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "includeFieldnameField", includeFieldnameField) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "fieldFieldname", fieldFieldname ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "includeCommentsField", includeCommentsField) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "commentsFieldname", commentsFieldname ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "includeTypeField", includeTypeField) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "typeFieldname", typeFieldname ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "includePrecisionField", includePrecisionField) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "precisionFieldname", precisionFieldname ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "includeLengthField", includeLengthField) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "lengthFieldname", lengthFieldname ) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "includeOriginField", includeOriginField) );
    xml.append( "    " ).append( XmlHandler.addTagValue( "originFieldname", originFieldname ) );

    return xml.toString();
  }

  @Override public void loadXml( Node transformNode, IHopMetadataProvider metadataProvider ) throws HopXmlException {
    try {
      outputRowcount = "Y".equalsIgnoreCase( XmlHandler.getTagValue( transformNode, "outputRowcount" ) );
      rowcountField = XmlHandler.getTagValue( transformNode, "rowcountField" );
      includePositionField = "Y".equalsIgnoreCase( XmlHandler.getTagValue(transformNode, "includePositionField" ));
      positionFieldname = XmlHandler.getTagValue( transformNode, "positionFieldname" );
      includeFieldnameField = "Y".equalsIgnoreCase( XmlHandler.getTagValue(transformNode, "includeFieldnameField" ));
      fieldFieldname = XmlHandler.getTagValue( transformNode, "fieldFieldname" );
      includeTypeField = "Y".equalsIgnoreCase( XmlHandler.getTagValue(transformNode, "includeTypeField" ));
      commentsFieldname = XmlHandler.getTagValue( transformNode, "commentsFieldname" );
      includeLengthField = "Y".equalsIgnoreCase( XmlHandler.getTagValue(transformNode, "includeLengthField" ));
      typeFieldname = XmlHandler.getTagValue( transformNode, "typeFieldname" );
      includePrecisionField = "Y".equalsIgnoreCase( XmlHandler.getTagValue(transformNode, "includePrecisionField" ));
      precisionFieldname = XmlHandler.getTagValue( transformNode, "precisionFieldname" );
      includeCommentsField = "Y".equalsIgnoreCase( XmlHandler.getTagValue(transformNode, "includeCommentsField" ));
      lengthFieldname = XmlHandler.getTagValue( transformNode, "lengthFieldname" );
      includeOriginField = "Y".equalsIgnoreCase( XmlHandler.getTagValue(transformNode, "includeOriginField" ));
      originFieldname = XmlHandler.getTagValue( transformNode, "originFieldname" );
    } catch ( Exception e ) {
      throw new HopXmlException( "Unable to load transform info from Xml", e );
    }
  }


  @Override public ITransform createTransform( TransformMeta transformMeta, TransformMetaStructureData data, int copyNr, PipelineMeta pipelineMeta, Pipeline pipeline ) {
    return new TransformMetaStructure( transformMeta, this, data, copyNr, pipelineMeta, pipeline );
  }

  @Override public TransformMetaStructureData getTransformData() {
    return new TransformMetaStructureData();
  }

  @Override public void check( List<ICheckResult> remarks, PipelineMeta pipelineMeta, TransformMeta transformMeta, IRowMeta prev, String[] input, String[] output, IRowMeta info, IVariables variables,
                               IHopMetadataProvider metadataProvider ) {
    CheckResult cr;
    cr = new CheckResult( ICheckResult.TYPE_RESULT_OK, "Not implemented", transformMeta );
    remarks.add( cr );

  }

  @Override public void getFields( IRowMeta inputRowMeta, String name, IRowMeta[] info, TransformMeta nextTransform, IVariables variables, IHopMetadataProvider metadataProvider )
    throws HopTransformException {
    // we create a new output row structure - clear r
    inputRowMeta.clear();

    this.setDefault();
    // create the new fields
    // Position
    if (includePositionField) {
      IValueMeta positionFieldValue = new ValueMetaInteger(positionFieldname);
      positionFieldValue.setOrigin(name);
      inputRowMeta.addValueMeta(positionFieldValue);
    }
    // field name
    if (includeFieldnameField) {
      IValueMeta nameFieldValue = new ValueMetaString(fieldFieldname);
      nameFieldValue.setOrigin(name);
      inputRowMeta.addValueMeta(nameFieldValue);
    }
    // comments
    if (includeCommentsField) {
      IValueMeta commentsFieldValue = new ValueMetaString(commentsFieldname);
      commentsFieldValue.setOrigin(name);
      inputRowMeta.addValueMeta(commentsFieldValue);
    }
    // Type
    if (includeTypeField) {
      IValueMeta typeFieldValue = new ValueMetaString(typeFieldname);
      typeFieldValue.setOrigin(name);
      inputRowMeta.addValueMeta(typeFieldValue);
    }
    // Length
    if (includeLengthField) {
      IValueMeta lengthFieldValue = new ValueMetaInteger(lengthFieldname);
      lengthFieldValue.setOrigin(name);
      inputRowMeta.addValueMeta(lengthFieldValue);
    }
    // Precision
    if (includePrecisionField) {
      IValueMeta precisionFieldValue = new ValueMetaInteger(precisionFieldname);
      precisionFieldValue.setOrigin(name);
      inputRowMeta.addValueMeta(precisionFieldValue);
    }
    // Origin
    if (includeOriginField) {
      IValueMeta originFieldValue = new ValueMetaString(originFieldname);
      originFieldValue.setOrigin(name);
      inputRowMeta.addValueMeta(originFieldValue);
    }

    if ( isOutputRowcount() ) {
      // RowCount
      IValueMeta v = new ValueMetaInteger( this.getRowcountField() );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }

  }

  @Override
  public void setDefault() {
    positionFieldname = BaseMessages.getString( PKG, "TransformMetaStructureMeta.PositionName" );
    fieldFieldname = BaseMessages.getString( PKG, "TransformMetaStructureMeta.FieldName" );
    commentsFieldname = BaseMessages.getString( PKG, "TransformMetaStructureMeta.CommentsName" );
    typeFieldname = BaseMessages.getString( PKG, "TransformMetaStructureMeta.TypeName" );
    lengthFieldname = BaseMessages.getString( PKG, "TransformMetaStructureMeta.LengthName" );
    precisionFieldname = BaseMessages.getString( PKG, "TransformMetaStructureMeta.PrecisionName" );
    originFieldname = BaseMessages.getString( PKG, "TransformMetaStructureMeta.OriginName" );
  }

  public boolean isOutputRowcount() {
    return outputRowcount;
  }

  public void setOutputRowcount( boolean outputRowcount ) {
    this.outputRowcount = outputRowcount;
  }

  public String getRowcountField() {
    return rowcountField;
  }

  public void setRowcountField( String rowcountField ) {
    this.rowcountField = rowcountField;
  }

  public String getFieldFieldname() {
    return fieldFieldname;
  }

  public void setFieldFieldname(String fieldFieldname) {
    this.fieldFieldname = fieldFieldname;
  }

  public String getCommentsFieldname() {
    return commentsFieldname;
  }

  public void setCommentsFieldname(String commentsFieldname) {
    this.commentsFieldname = commentsFieldname;
  }

  public String getTypeFieldname() {
    return typeFieldname;
  }

  public void setTypeFieldname(String typeFieldname) {
    this.typeFieldname = typeFieldname;
  }

  public String getPositionFieldname() {
    return positionFieldname;
  }

  public void setPositionFieldname(String positionFieldname) {
    this.positionFieldname = positionFieldname;
  }

  public String getLengthFieldname() {
    return lengthFieldname;
  }

  public void setLengthFieldname(String lengthFieldname) {
    this.lengthFieldname = lengthFieldname;
  }

  public String getPrecisionFieldname() {
    return precisionFieldname;
  }

  public void setPrecisionFieldname(String precisionFieldname) {
    this.precisionFieldname = precisionFieldname;
  }

  public String getOriginFieldname() {
    return originFieldname;
  }

  public void setOriginFieldname(String originFieldname) {
    this.originFieldname = originFieldname;
  }

  public boolean isIncludePositionField() {
    return includePositionField;
  }

  public void setIncludePositionField(boolean includePositionField) {
    this.includePositionField = includePositionField;
  }

  public boolean isIncludeFieldnameField() {
    return includeFieldnameField;
  }

  public void setIncludeFieldnameField(boolean includeFieldnameField) {
    this.includeFieldnameField = includeFieldnameField;
  }

  public boolean isIncludeCommentsField() {
    return includeCommentsField;
  }

  public void setIncludeCommentsField(boolean includeCommentsField) {
    this.includeCommentsField = includeCommentsField;
  }

  public boolean isIncludeTypeField() {
    return includeTypeField;
  }

  public void setIncludeTypeField(boolean includeTypeField) {
    this.includeTypeField = includeTypeField;
  }

  public boolean isIncludeLengthField() {
    return includeLengthField;
  }

  public void setIncludeLengthField(boolean includeLengthField) {
    this.includeLengthField = includeLengthField;
  }

  public boolean isIncludePrecisionField() {
    return includePrecisionField;
  }

  public void setIncludePrecisionField(boolean includePrecisionField) {
    this.includePrecisionField = includePrecisionField;
  }

  public boolean isIncludeOriginField() {
    return includeOriginField;
  }

  public void setIncludeOriginField(boolean includeOriginField) {
    this.includeOriginField = includeOriginField;
  }
}
