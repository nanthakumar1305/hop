/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.pipeline.transforms.pipelineexecutor;

import org.apache.hop.core.RowMetaAndData;
import org.apache.hop.core.RowSet;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.TransformDataInterface;

import java.util.List;

/**
 * @author Matt
 * @since 18-mar-2013
 */
public class PipelineExecutorData extends BaseTransformData implements TransformDataInterface {
  private Pipeline executorPipeline;
  private PipelineMeta executorPipelineMeta;

  private RowMetaInterface inputRowMeta;

  private RowMetaInterface executorTransformOutputRowMeta;
  private RowMetaInterface resultRowsOutputRowMeta;
  private RowMetaInterface executionResultsOutputRowMeta;
  private RowMetaInterface resultFilesOutputRowMeta;

  public List<RowMetaAndData> groupBuffer;
  public int groupSize;
  public int groupTime;
  public long groupTimeStart;
  public String groupField;
  public int groupFieldIndex;
  public ValueMetaInterface groupFieldMeta;

  public Object prevGroupFieldData;

  private RowSet executorTransformOutputRowSet;
  private RowSet resultRowsRowSet;
  private RowSet resultFilesRowSet;
  private RowSet executionResultRowSet;

  public PipelineExecutorData() {
    super();
  }

  public Pipeline getExecutorPipeline() {
    return executorPipeline;
  }

  public void setExecutorPipeline( Pipeline executorPipeline ) {
    this.executorPipeline = executorPipeline;
  }

  public PipelineMeta getExecutorPipelineMeta() {
    return executorPipelineMeta;
  }

  public void setExecutorPipelineMeta( PipelineMeta executorPipelineMeta ) {
    this.executorPipelineMeta = executorPipelineMeta;
  }

  public RowMetaInterface getInputRowMeta() {
    return inputRowMeta;
  }

  public void setInputRowMeta( RowMetaInterface inputRowMeta ) {
    this.inputRowMeta = inputRowMeta;
  }

  public RowMetaInterface getExecutorTransformOutputRowMeta() {
    return executorTransformOutputRowMeta;
  }

  public void setExecutorTransformOutputRowMeta( RowMetaInterface executorTransformOutputRowMeta ) {
    this.executorTransformOutputRowMeta = executorTransformOutputRowMeta;
  }

  public RowMetaInterface getResultRowsOutputRowMeta() {
    return resultRowsOutputRowMeta;
  }

  public void setResultRowsOutputRowMeta( RowMetaInterface resultRowsOutputRowMeta ) {
    this.resultRowsOutputRowMeta = resultRowsOutputRowMeta;
  }

  public RowMetaInterface getExecutionResultsOutputRowMeta() {
    return executionResultsOutputRowMeta;
  }

  public void setExecutionResultsOutputRowMeta( RowMetaInterface executionResultsOutputRowMeta ) {
    this.executionResultsOutputRowMeta = executionResultsOutputRowMeta;
  }

  public RowMetaInterface getResultFilesOutputRowMeta() {
    return resultFilesOutputRowMeta;
  }

  public void setResultFilesOutputRowMeta( RowMetaInterface resultFilesOutputRowMeta ) {
    this.resultFilesOutputRowMeta = resultFilesOutputRowMeta;
  }

  public RowSet getExecutorTransformOutputRowSet() {
    return executorTransformOutputRowSet;
  }

  public void setExecutorTransformOutputRowSet( RowSet executorTransformOutputRowSet ) {
    this.executorTransformOutputRowSet = executorTransformOutputRowSet;
  }

  public RowSet getResultRowsRowSet() {
    return resultRowsRowSet;
  }

  public void setResultRowsRowSet( RowSet resultRowsRowSet ) {
    this.resultRowsRowSet = resultRowsRowSet;
  }

  public RowSet getResultFilesRowSet() {
    return resultFilesRowSet;
  }

  public void setResultFilesRowSet( RowSet resultFilesRowSet ) {
    this.resultFilesRowSet = resultFilesRowSet;
  }

  public RowSet getExecutionResultRowSet() {
    return executionResultRowSet;
  }

  public void setExecutionResultRowSet( RowSet executionResultRowSet ) {
    this.executionResultRowSet = executionResultRowSet;
  }
}