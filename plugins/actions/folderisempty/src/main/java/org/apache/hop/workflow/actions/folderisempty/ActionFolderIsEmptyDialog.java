/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.workflow.actions.folderisempty;

import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.workflow.action.ActionDialog;
import org.apache.hop.ui.workflow.dialog.WorkflowDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.action.IActionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** This dialog allows you to edit the Create Folder action settings. */
public class ActionFolderIsEmptyDialog extends ActionDialog implements IActionDialog {
  private static final Class<?> PKG = ActionFolderIsEmpty.class; // For Translator

  private Text wName;

  private TextVar wFoldername;

  private Button wIncludeSubFolders;

  private Button wSpecifyWildcard;

  private Label wlWildcard;
  private TextVar wWildcard;

  private ActionFolderIsEmpty action;

  private boolean changed;

  public ActionFolderIsEmptyDialog(
      Shell parent, IAction action, WorkflowMeta workflowMeta, IVariables variables) {
    super(parent, workflowMeta, variables);
    this.action = (ActionFolderIsEmpty) action;
    if (this.action.getName() == null) {
      this.action.setName(BaseMessages.getString(PKG, "ActionFolderIsEmpty.Name.Default"));
    }
  }

  @Override
  public IAction open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
    PropsUi.setLook(shell);
    WorkflowDialog.setShellImage(shell, action);

    ModifyListener lsMod = e -> action.setChanged();
    changed = action.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "ActionFolderIsEmpty.Title"));

    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    // Foldername line
    Label wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "ActionFolderIsEmpty.Name.Label"));
    PropsUi.setLook(wlName);
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.right = new FormAttachment(middle, -margin);
    fdlName.top = new FormAttachment(0, margin);
    wlName.setLayoutData(fdlName);
    wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wName);
    wName.addModifyListener(lsMod);
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(middle, 0);
    fdName.top = new FormAttachment(0, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    // Foldername line
    Label wlFoldername = new Label(shell, SWT.RIGHT);
    wlFoldername.setText(BaseMessages.getString(PKG, "ActionFolderIsEmpty.Foldername.Label"));
    PropsUi.setLook(wlFoldername);
    FormData fdlFoldername = new FormData();
    fdlFoldername.left = new FormAttachment(0, 0);
    fdlFoldername.top = new FormAttachment(wName, margin);
    fdlFoldername.right = new FormAttachment(middle, -margin);
    wlFoldername.setLayoutData(fdlFoldername);

    Button wbFoldername = new Button(shell, SWT.PUSH | SWT.CENTER);
    PropsUi.setLook(wbFoldername);
    wbFoldername.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
    FormData fdbFoldername = new FormData();
    fdbFoldername.right = new FormAttachment(100, 0);
    fdbFoldername.top = new FormAttachment(wName, 0);
    wbFoldername.setLayoutData(fdbFoldername);

    wFoldername = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wFoldername);
    wFoldername.addModifyListener(lsMod);
    FormData fdFoldername = new FormData();
    fdFoldername.left = new FormAttachment(middle, 0);
    fdFoldername.top = new FormAttachment(wName, margin);
    fdFoldername.right = new FormAttachment(wbFoldername, -margin);
    wFoldername.setLayoutData(fdFoldername);

    // Include sub folders?
    Label wlIncludeSubFolders = new Label(shell, SWT.RIGHT);
    wlIncludeSubFolders.setText(
        BaseMessages.getString(PKG, "ActionFolderIsEmpty.IncludeSubFolders.Label"));
    PropsUi.setLook(wlIncludeSubFolders);
    FormData fdlIncludeSubFolders = new FormData();
    fdlIncludeSubFolders.left = new FormAttachment(0, 0);
    fdlIncludeSubFolders.top = new FormAttachment(wFoldername, margin);
    fdlIncludeSubFolders.right = new FormAttachment(middle, -margin);
    wlIncludeSubFolders.setLayoutData(fdlIncludeSubFolders);
    wIncludeSubFolders = new Button(shell, SWT.CHECK);
    PropsUi.setLook(wIncludeSubFolders);
    wIncludeSubFolders.setToolTipText(
        BaseMessages.getString(PKG, "ActionFolderIsEmpty.IncludeSubFolders.Tooltip"));
    FormData fdIncludeSubFolders = new FormData();
    fdIncludeSubFolders.left = new FormAttachment(middle, 0);
    fdIncludeSubFolders.top = new FormAttachment(wlIncludeSubFolders, 0, SWT.CENTER);
    fdIncludeSubFolders.right = new FormAttachment(100, 0);
    wIncludeSubFolders.setLayoutData(fdIncludeSubFolders);
    wIncludeSubFolders.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            action.setChanged();
          }
        });

    // Specify wildcard?
    Label wlSpecifyWildcard = new Label(shell, SWT.RIGHT);
    wlSpecifyWildcard.setText(
        BaseMessages.getString(PKG, "ActionFolderIsEmpty.SpecifyWildcard.Label"));
    PropsUi.setLook(wlSpecifyWildcard);
    FormData fdlSpecifyWildcard = new FormData();
    fdlSpecifyWildcard.left = new FormAttachment(0, 0);
    fdlSpecifyWildcard.top = new FormAttachment(wlIncludeSubFolders, 2 * margin);
    fdlSpecifyWildcard.right = new FormAttachment(middle, -margin);
    wlSpecifyWildcard.setLayoutData(fdlSpecifyWildcard);
    wSpecifyWildcard = new Button(shell, SWT.CHECK);
    PropsUi.setLook(wSpecifyWildcard);
    wSpecifyWildcard.setToolTipText(
        BaseMessages.getString(PKG, "ActionFolderIsEmpty.SpecifyWildcard.Tooltip"));
    FormData fdSpecifyWildcard = new FormData();
    fdSpecifyWildcard.left = new FormAttachment(middle, 0);
    fdSpecifyWildcard.top = new FormAttachment(wlSpecifyWildcard, 0, SWT.CENTER);
    fdSpecifyWildcard.right = new FormAttachment(100, 0);
    wSpecifyWildcard.setLayoutData(fdSpecifyWildcard);
    wSpecifyWildcard.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            action.setChanged();
            checkLimitSearch();
          }
        });

    // Wildcard line
    wlWildcard = new Label(shell, SWT.RIGHT);
    wlWildcard.setText(BaseMessages.getString(PKG, "ActionFolderIsEmpty.Wildcard.Label"));
    PropsUi.setLook(wlWildcard);
    FormData fdlWildcard = new FormData();
    fdlWildcard.left = new FormAttachment(0, 0);
    fdlWildcard.top = new FormAttachment(wSpecifyWildcard, margin);
    fdlWildcard.right = new FormAttachment(middle, -margin);
    wlWildcard.setLayoutData(fdlWildcard);
    wWildcard = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wWildcard);
    wWildcard.addModifyListener(lsMod);
    FormData fdWildcard = new FormData();
    fdWildcard.left = new FormAttachment(middle, 0);
    fdWildcard.top = new FormAttachment(wSpecifyWildcard, margin);
    fdWildcard.right = new FormAttachment(100, -margin);
    wWildcard.setLayoutData(fdWildcard);

    // Whenever something changes, set the tooltip to the expanded version:
    wFoldername.addModifyListener(
        e -> wFoldername.setToolTipText(variables.resolve(wFoldername.getText())));
    // Whenever something changes, set the tooltip to the expanded version:
    wWildcard.addModifyListener(
        e -> wWildcard.setToolTipText(variables.resolve(wWildcard.getText())));

    wbFoldername.addListener(
        SWT.Selection, e -> BaseDialog.presentDirectoryDialog(shell, wFoldername, variables));

    // Buttons go at the very bottom
    //
    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, e -> ok());
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, e -> cancel());
    BaseTransformDialog.positionBottomButtons(
        shell, new Button[] {wOk, wCancel}, margin, wWildcard);

    getData();
    checkLimitSearch();

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return action;
  }

  private void checkLimitSearch() {
    wlWildcard.setEnabled(wSpecifyWildcard.getSelection());
    wWildcard.setEnabled(wSpecifyWildcard.getSelection());
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    if (action.getName() != null) {
      wName.setText(action.getName());
    }
    if (action.getFoldername() != null) {
      wFoldername.setText(action.getFoldername());
    }
    wIncludeSubFolders.setSelection(action.isIncludeSubFolders());
    wSpecifyWildcard.setSelection(action.isSpecifyWildcard());
    if (action.getWildcard() != null) {
      wWildcard.setText(action.getWildcard());
    }

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    action.setChanged(changed);
    action = null;
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wName.getText())) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setText(BaseMessages.getString(PKG, "System.TransformActionNameMissing.Title"));
      mb.setMessage(BaseMessages.getString(PKG, "System.ActionNameMissing.Msg"));
      mb.open();
      return;
    }
    action.setName(wName.getText());
    action.setFoldername(wFoldername.getText());
    action.setIncludeSubFolders(wIncludeSubFolders.getSelection());
    action.setSpecifyWildcard(wSpecifyWildcard.getSelection());
    action.setWildcard(wWildcard.getText());

    dispose();
  }
}
