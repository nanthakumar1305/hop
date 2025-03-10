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

package org.apache.hop.pipeline.transforms.dimensionlookup;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.DbCache;
import org.apache.hop.core.Props;
import org.apache.hop.core.SqlStatement;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.database.dialog.DatabaseExplorerDialog;
import org.apache.hop.ui.core.database.dialog.SqlEditor;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.EnterSelectionDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.MetaSelectionLine;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DimensionLookupDialog extends BaseTransformDialog implements ITransformDialog {
  private static final Class<?> PKG = DimensionLookupMeta.class; // For Translator

  private CTabFolder wTabFolder;

  private CTabItem wFieldsTab;

  private CTabItem wVersioningTab;

  private MetaSelectionLine<DatabaseMeta> wConnection;

  private TextVar wSchema;

  private TextVar wTable;

  private Label wlCommit;
  private Text wCommit;

  private Button wUseCache;

  private Label wlPreloadCache;
  private Button wPreloadCache;

  private Label wlCacheSize;
  private Text wCacheSize;

  private CCombo wTk;

  private Label wlTkRename;
  private Text wTkRename;

  private Button wAutoinc;

  private Button wTableMax;

  private Button wSeqButton;
  private Text wSeq;

  private Label wlVersion;
  private CCombo wVersion;

  private CCombo wDatefield;

  private CCombo wFromdate;

  private Button wUseAltStartDate;
  private CCombo wAltStartDate;
  private CCombo wAltStartDateField;

  private Label wlMinyear;
  private Text wMinyear;

  private CCombo wTodate;

  private Label wlMaxyear;
  private Text wMaxyear;

  private Button wUpdate;

  private TableView wKey;

  private TableView wUpIns;

  private Button wCreate;

  private final DimensionLookupMeta input;
  private boolean backupUpdate;
  private boolean backupAutoInc;

  private DatabaseMeta ci;

  private ColumnInfo[] ciUpIns;

  private ColumnInfo[] ciKey;

  private final Map<String, Integer> inputFields;

  private boolean gotPreviousFields = false;

  private boolean gotTableFields = false;

  /** List of ColumnInfo that should have the field names of the selected database table */
  private final List<ColumnInfo> tableFieldColumns = new ArrayList<>();

  public DimensionLookupDialog(
      Shell parent, IVariables variables, Object in, PipelineMeta tr, String sname) {
    super(parent, variables, (BaseTransformMeta) in, tr, sname);
    input = (DimensionLookupMeta) in;
    inputFields = new HashMap<>();
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    PropsUi.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = e -> input.setChanged();

    FocusListener lsConnectionFocus =
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent event) {
            input.setChanged();
            setTableFieldCombo();
          }
        };

    ModifyListener lsTableMod =
        arg0 -> {
          input.setChanged();
          setTableFieldCombo();
        };

    backupChanged = input.hasChanged();
    backupUpdate = input.isUpdate();
    backupAutoInc = input.isAutoIncrement();
    ci = input.getDatabaseMeta();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = props.getMargin();

    Composite mainComposite = shell;
    PropsUi.setLook(mainComposite);

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    mainComposite.setLayout(fileLayout);

    // TransformName line
    wlTransformName = new Label(mainComposite, SWT.RIGHT);
    wlTransformName.setText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.TransformName.Label"));
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.right = new FormAttachment(middle, -margin);
    fdlTransformName.top = new FormAttachment(0, margin);
    wlTransformName.setLayoutData(fdlTransformName);
    wTransformName = new Text(mainComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
    wTransformName.addModifyListener(lsMod);
    fdTransformName = new FormData();
    fdTransformName.left = new FormAttachment(middle, 0);
    fdTransformName.top = new FormAttachment(0, margin);
    fdTransformName.right = new FormAttachment(100, 0);
    wTransformName.setLayoutData(fdTransformName);

    // Update the dimension?
    Label wlUpdate = new Label(mainComposite, SWT.RIGHT);
    wlUpdate.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Update.Label"));
    PropsUi.setLook(wlUpdate);
    FormData fdlUpdate = new FormData();
    fdlUpdate.left = new FormAttachment(0, 0);
    fdlUpdate.right = new FormAttachment(middle, -margin);
    fdlUpdate.top = new FormAttachment(wTransformName, margin);
    wlUpdate.setLayoutData(fdlUpdate);
    wUpdate = new Button(mainComposite, SWT.CHECK);
    PropsUi.setLook(wUpdate);
    FormData fdUpdate = new FormData();
    fdUpdate.left = new FormAttachment(middle, 0);
    fdUpdate.top = new FormAttachment(wlUpdate, 0, SWT.CENTER);
    fdUpdate.right = new FormAttachment(100, 0);
    wUpdate.setLayoutData(fdUpdate);

    // Clicking on update changes the options in the update combo boxes!
    wUpdate.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            input.setUpdate(!input.isUpdate());
            input.setChanged();

            setFlags();
          }
        });

    // Connection line
    wConnection = addConnectionLine(mainComposite, wUpdate, input.getDatabaseMeta(), lsMod);
    wConnection.addFocusListener(lsConnectionFocus);
    wConnection.addModifyListener(
        e -> {
          // We have new content: change ci connection:
          ci = pipelineMeta.findDatabase(wConnection.getText());
          setFlags();
        });

    // Schema line...
    Label wlSchema = new Label(mainComposite, SWT.RIGHT);
    wlSchema.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.TargetSchema.Label"));
    PropsUi.setLook(wlSchema);
    FormData fdlSchema = new FormData();
    fdlSchema.left = new FormAttachment(0, 0);
    fdlSchema.right = new FormAttachment(middle, -margin);
    fdlSchema.top = new FormAttachment(wConnection, margin);
    wlSchema.setLayoutData(fdlSchema);

    Button wbSchema = new Button(mainComposite, SWT.PUSH | SWT.CENTER);
    PropsUi.setLook(wbSchema);
    wbSchema.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
    FormData fdbSchema = new FormData();
    fdbSchema.top = new FormAttachment(wConnection, margin);
    fdbSchema.right = new FormAttachment(100, 0);
    wbSchema.setLayoutData(fdbSchema);

    wSchema = new TextVar(variables, mainComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wSchema);
    wSchema.addModifyListener(lsTableMod);
    FormData fdSchema = new FormData();
    fdSchema.left = new FormAttachment(middle, 0);
    fdSchema.top = new FormAttachment(wConnection, margin);
    fdSchema.right = new FormAttachment(wbSchema, -margin);
    wSchema.setLayoutData(fdSchema);

    // Table line...
    Label wlTable = new Label(mainComposite, SWT.RIGHT);
    wlTable.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.TargeTable.Label"));
    PropsUi.setLook(wlTable);
    FormData fdlTable = new FormData();
    fdlTable.left = new FormAttachment(0, 0);
    fdlTable.right = new FormAttachment(middle, -margin);
    fdlTable.top = new FormAttachment(wbSchema, margin);
    wlTable.setLayoutData(fdlTable);

    Button wbTable = new Button(mainComposite, SWT.PUSH | SWT.CENTER);
    PropsUi.setLook(wbTable);
    wbTable.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Browse.Button"));
    FormData fdbTable = new FormData();
    fdbTable.right = new FormAttachment(100, 0);
    fdbTable.top = new FormAttachment(wbSchema, margin);
    wbTable.setLayoutData(fdbTable);

    wTable = new TextVar(variables, mainComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wTable);
    wTable.addModifyListener(lsTableMod);
    FormData fdTable = new FormData();
    fdTable.left = new FormAttachment(middle, 0);
    fdTable.top = new FormAttachment(wbSchema, margin);
    fdTable.right = new FormAttachment(wbTable, 0);
    wTable.setLayoutData(fdTable);

    // Commit size ...
    wlCommit = new Label(mainComposite, SWT.RIGHT);
    wlCommit.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Commit.Label"));
    PropsUi.setLook(wlCommit);
    FormData fdlCommit = new FormData();
    fdlCommit.left = new FormAttachment(0, 0);
    fdlCommit.right = new FormAttachment(middle, -margin);
    fdlCommit.top = new FormAttachment(wTable, margin);
    wlCommit.setLayoutData(fdlCommit);
    wCommit = new Text(mainComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wCommit);
    wCommit.addModifyListener(lsMod);
    FormData fdCommit = new FormData();
    fdCommit.left = new FormAttachment(middle, 0);
    fdCommit.top = new FormAttachment(wTable, margin);
    fdCommit.right = new FormAttachment(100, 0);
    wCommit.setLayoutData(fdCommit);

    // Use Cache?
    Label wlUseCache = new Label(mainComposite, SWT.RIGHT);
    wlUseCache.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.UseCache.Label"));
    PropsUi.setLook(wlUseCache);
    FormData fdlUseCache = new FormData();
    fdlUseCache.left = new FormAttachment(0, 0);
    fdlUseCache.right = new FormAttachment(middle, -margin);
    fdlUseCache.top = new FormAttachment(wCommit, margin);
    wlUseCache.setLayoutData(fdlUseCache);
    wUseCache = new Button(mainComposite, SWT.CHECK);
    PropsUi.setLook(wUseCache);
    wUseCache.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent arg0) {
            setFlags();
            input.setChanged();
          }
        });
    FormData fdUseCache = new FormData();
    fdUseCache.left = new FormAttachment(middle, 0);
    fdUseCache.top = new FormAttachment(wlUseCache, 0, SWT.CENTER);
    fdUseCache.right = new FormAttachment(100, 0);
    wUseCache.setLayoutData(fdUseCache);

    // Preload cache?
    wlPreloadCache = new Label(mainComposite, SWT.RIGHT);
    wlPreloadCache.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.PreloadCache.Label"));
    PropsUi.setLook(wlPreloadCache);
    FormData fdlPreloadCache = new FormData();
    fdlPreloadCache.left = new FormAttachment(0, 0);
    fdlPreloadCache.right = new FormAttachment(middle, -margin);
    fdlPreloadCache.top = new FormAttachment(wUseCache, margin);
    wlPreloadCache.setLayoutData(fdlPreloadCache);
    wPreloadCache = new Button(mainComposite, SWT.CHECK);
    PropsUi.setLook(wPreloadCache);
    wPreloadCache.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent arg0) {
            setFlags();
            input.setChanged();
          }
        });
    FormData fdPreloadCache = new FormData();
    fdPreloadCache.left = new FormAttachment(middle, 0);
    fdPreloadCache.top = new FormAttachment(wlPreloadCache, 0, SWT.CENTER);
    fdPreloadCache.right = new FormAttachment(100, 0);
    wPreloadCache.setLayoutData(fdPreloadCache);

    // Cache size ...
    wlCacheSize = new Label(mainComposite, SWT.RIGHT);
    wlCacheSize.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.CacheSize.Label"));
    PropsUi.setLook(wlCacheSize);
    FormData fdlCacheSize = new FormData();
    fdlCacheSize.left = new FormAttachment(0, 0);
    fdlCacheSize.right = new FormAttachment(middle, -margin);
    fdlCacheSize.top = new FormAttachment(wPreloadCache, margin);
    wlCacheSize.setLayoutData(fdlCacheSize);
    wCacheSize = new Text(mainComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wCacheSize);
    wCacheSize.addModifyListener(lsMod);
    FormData fdCacheSize = new FormData();
    fdCacheSize.left = new FormAttachment(middle, 0);
    fdCacheSize.top = new FormAttachment(wPreloadCache, margin);
    fdCacheSize.right = new FormAttachment(100, 0);
    wCacheSize.setLayoutData(fdCacheSize);

    // THE BOTTOM BUTTONS
    wOk = new Button(mainComposite, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, e -> ok());
    Button wGet = new Button(mainComposite, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.GetFields.Button"));
    wGet.addListener(SWT.Selection, e -> get());
    wCreate = new Button(mainComposite, SWT.PUSH);
    wCreate.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.SQL.Button"));
    wCreate.addListener(SWT.Selection, e -> create());
    wCancel = new Button(mainComposite, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, e -> cancel());
    setButtonPositions(new Button[] {wOk, wGet, wCreate, wCancel}, margin, null);


    wTabFolder = new CTabFolder(mainComposite, SWT.BORDER);
    PropsUi.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    addKeyTab(margin, middle, lsMod);
    addFieldsTab(margin, lsMod);
    addTechnicalKeyTab(margin, middle, lsMod);
    addVersioningTab(margin, middle, lsMod);

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.top = new FormAttachment(wCacheSize, margin);
    fdTabFolder.bottom = new FormAttachment(wOk, -margin);
    wTabFolder.setLayoutData(fdTabFolder);

    FormData fdComp = new FormData();
    fdComp.left = new FormAttachment(0, 0);
    fdComp.top = new FormAttachment(0, 0);
    fdComp.right = new FormAttachment(100, 0);
    fdComp.bottom = new FormAttachment(100, 0);
    mainComposite.setLayoutData(fdComp);

    mainComposite.pack();

    setTableMax();
    setSequence();
    setAutoincUse();

    wbSchema.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            getSchemaNames();
          }
        });
    wbTable.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            getTableName();
          }
        });

    wTabFolder.setSelection(0);

    getData();
    setTableFieldCombo();

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  public void addKeyTab(int margin, int middle, ModifyListener lsMod) {
    // ////////////////////////
    // START OF KEY TAB ///
    // /
    CTabItem wKeyTab = new CTabItem(wTabFolder, SWT.NONE);
    wKeyTab.setFont(GuiResource.getInstance().getFontDefault());
    wKeyTab.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.KeyTab.CTabItem"));

    FormLayout keyLayout = new FormLayout();
    keyLayout.marginWidth = 3;
    keyLayout.marginHeight = 3;

    Composite wKeyComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wKeyComp);
    wKeyComp.setLayout(keyLayout);

    //
    // The Lookup fields: usually the key
    //
    Label wlKey = new Label(wKeyComp, SWT.NONE);
    wlKey.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.KeyFields.Label"));
    PropsUi.setLook(wlKey);
    FormData fdlKey = new FormData();
    fdlKey.left = new FormAttachment(0, 0);
    fdlKey.top = new FormAttachment(0, margin);
    fdlKey.right = new FormAttachment(100, 0);
    wlKey.setLayoutData(fdlKey);

    int nrKeyCols = 2;
    int nrKeyRows = (input.getKeyStream() != null ? input.getKeyStream().length : 1);

    ciKey = new ColumnInfo[nrKeyCols];
    ciKey[0] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "DimensionLookupDialog.ColumnInfo.DimensionField"),
            ColumnInfo.COLUMN_TYPE_CCOMBO,
            new String[] {""},
            false);
    ciKey[1] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "DimensionLookupDialog.ColumnInfo.FieldInStream"),
            ColumnInfo.COLUMN_TYPE_CCOMBO,
            new String[] {""},
            false);
    tableFieldColumns.add(ciKey[0]);
    wKey =
        new TableView(
            variables,
            wKeyComp,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
            ciKey,
            nrKeyRows,
            lsMod,
            props);

    FormData fdKey = new FormData();
    fdKey.left = new FormAttachment(0, 0);
    fdKey.top = new FormAttachment(wlKey, margin);
    fdKey.right = new FormAttachment(100, 0);
    fdKey.bottom = new FormAttachment(100, 0);
    wKey.setLayoutData(fdKey);

    FormData fdKeyComp = new FormData();
    fdKeyComp.left = new FormAttachment(0, 0);
    fdKeyComp.top = new FormAttachment(0, 0);
    fdKeyComp.right = new FormAttachment(100, 0);
    fdKeyComp.bottom = new FormAttachment(100, 0);
    wKeyComp.setLayoutData(fdKeyComp);

    wKeyComp.layout();
    wKeyTab.setControl(wKeyComp);
  }

  public void addTechnicalKeyTab(int margin, int middle, ModifyListener lsMod) {
    CTabItem wTechnicalKeyTab = new CTabItem(wTabFolder, SWT.NONE);
    wTechnicalKeyTab.setFont(GuiResource.getInstance().getFontDefault());
    wTechnicalKeyTab.setText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.TechnicalKeyTab.CTabItem"));

    FormLayout technicalKeyLayout = new FormLayout();
    technicalKeyLayout.marginWidth = 3;
    technicalKeyLayout.marginHeight = 3;

    Composite wTechnicalKeyComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wTechnicalKeyComp);
    wTechnicalKeyComp.setLayout(technicalKeyLayout);

    // Technical key field:
    Label wlTk = new Label(wTechnicalKeyComp, SWT.RIGHT);
    wlTk.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.TechnicalKeyField.Label"));
    PropsUi.setLook(wlTk);
    FormData fdlTk = new FormData();
    fdlTk.left = new FormAttachment(0, margin);
    fdlTk.top = new FormAttachment(0, 3 * margin);
    wlTk.setLayoutData(fdlTk);

    wTk = new CCombo(wTechnicalKeyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wTk);
    wTk.addModifyListener(lsMod);
    FormData fdTk = new FormData();
    fdTk.left = new FormAttachment(wlTk, margin);
    fdTk.top = new FormAttachment(0, 2 * margin);
    fdTk.right = new FormAttachment(30 + middle / 2, 0);
    wTk.setLayoutData(fdTk);
    wTk.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do not trigger focusLost
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            getFieldsFromTable();
            shell.setCursor(null);
            busy.dispose();
          }
        });

    wlTkRename = new Label(wTechnicalKeyComp, SWT.RIGHT);
    wlTkRename.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.NewName.Label"));
    PropsUi.setLook(wlTkRename);
    FormData fdlTkRename = new FormData();
    fdlTkRename.left = new FormAttachment(wTk, margin);
    fdlTkRename.top = new FormAttachment(0, 3 * margin);
    wlTkRename.setLayoutData(fdlTkRename);

    wTkRename = new Text(wTechnicalKeyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wTkRename);
    wTkRename.addModifyListener(lsMod);
    FormData fdTkRename = new FormData();
    fdTkRename.left = new FormAttachment(wlTkRename, margin);
    fdTkRename.top = new FormAttachment(0, 2 * margin);
    fdTkRename.right = new FormAttachment(100, -margin);
    wTkRename.setLayoutData(fdTkRename);

    Group gTechGroup = new Group(wTechnicalKeyComp, SWT.SHADOW_ETCHED_IN);
    gTechGroup.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.TechGroup.Label"));

    FormLayout groupLayout = new FormLayout();
    groupLayout.marginHeight = 10;
    groupLayout.marginWidth = 10;
    gTechGroup.setLayout(groupLayout);
    PropsUi.setLook(gTechGroup);
    FormData fdTechGroup = new FormData();
    fdTechGroup.top = new FormAttachment(wTkRename, margin);
    fdTechGroup.left = new FormAttachment(0, margin);
    fdTechGroup.right = new FormAttachment(100, -margin);
    gTechGroup.setBackground(shell.getBackground()); // the default looks ugly
    gTechGroup.setLayoutData(fdTechGroup);

    // Use maximum of table + 1
    wTableMax = new Button(gTechGroup, SWT.RADIO);
    PropsUi.setLook(wTableMax);
    wTableMax.setSelection(false);
    FormData fdTableMax = new FormData();
    fdTableMax.left = new FormAttachment(0, 0);
    fdTableMax.top = new FormAttachment(wTkRename, margin);
    wTableMax.setLayoutData(fdTableMax);
    wTableMax.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.TableMaximum.Label"));
    wTableMax.setToolTipText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.TableMaximum.Tooltip", Const.CR));

    // Sequence Check Button
    wSeqButton = new Button(gTechGroup, SWT.RADIO);
    PropsUi.setLook(wSeqButton);
    wSeqButton.setSelection(false);
    FormData fdSeqButton = new FormData();
    fdSeqButton.left = new FormAttachment(0, 0);
    fdSeqButton.top = new FormAttachment(wTableMax, margin);
    wSeqButton.setLayoutData(fdSeqButton);
    wSeqButton.setToolTipText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.Sequence.Tooltip", Const.CR));
    wSeqButton.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Sequence.Label"));

    wSeq = new Text(gTechGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wSeq);
    wSeq.addModifyListener(lsMod);
    FormData fdSeq = new FormData();
    fdSeq.left = new FormAttachment(wSeqButton, margin);
    fdSeq.top = new FormAttachment(wSeqButton, 0, SWT.CENTER);
    fdSeq.right = new FormAttachment(100, 0);
    wSeq.setLayoutData(fdSeq);
    wSeq.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent arg0) {
            input.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_SEQUENCE);
            wSeqButton.setSelection(true);
            wAutoinc.setSelection(false);
            wTableMax.setSelection(false);
          }

          @Override
          public void focusLost(FocusEvent arg0) {
            // Do not trigger focusLost
          }
        });

    // Use an autoincrement field?
    wAutoinc = new Button(gTechGroup, SWT.RADIO);
    PropsUi.setLook(wAutoinc);
    wAutoinc.setSelection(false);
    FormData fdAutoinc = new FormData();
    fdAutoinc.left = new FormAttachment(0, 0);
    fdAutoinc.top = new FormAttachment(wSeq, margin);
    wAutoinc.setLayoutData(fdAutoinc);
    wAutoinc.setToolTipText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.AutoincButton.Tooltip", Const.CR));
    wAutoinc.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Autoincrement.Label"));

    FormData fdTechnicalKeyComp = new FormData();
    fdTechnicalKeyComp.left = new FormAttachment(0, 0);
    fdTechnicalKeyComp.top = new FormAttachment(0, 0);
    fdTechnicalKeyComp.right = new FormAttachment(100, 0);
    fdTechnicalKeyComp.bottom = new FormAttachment(100, 0);
    wTechnicalKeyComp.setLayoutData(fdTechnicalKeyComp);

    wTechnicalKeyComp.layout();
    wTechnicalKeyTab.setControl(wTechnicalKeyComp);
  }

  public void addFieldsTab(int margin, ModifyListener lsMod) {

    wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
    wFieldsTab.setFont(GuiResource.getInstance().getFontDefault());
    wFieldsTab.setText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.FieldsTab.CTabItem.Title"));

    Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wFieldsComp);

    FormLayout fieldsCompLayout = new FormLayout();
    fieldsCompLayout.marginWidth = PropsUi.getFormMargin();
    fieldsCompLayout.marginHeight = PropsUi.getFormMargin();
    wFieldsComp.setLayout(fieldsCompLayout);

    // THE UPDATE/INSERT TABLE
    Label wlUpIns = new Label(wFieldsComp, SWT.NONE);
    wlUpIns.setText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.UpdateOrInsertFields.Label"));
    PropsUi.setLook(wlUpIns);
    FormData fdlUpIns = new FormData();
    fdlUpIns.left = new FormAttachment(0, 0);
    fdlUpIns.top = new FormAttachment(0, margin);
    wlUpIns.setLayoutData(fdlUpIns);

    int upInsCols = 3;
    int upInsRows = (input.getFieldStream() != null ? input.getFieldStream().length : 1);

    ciUpIns = new ColumnInfo[upInsCols];
    ciUpIns[0] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "DimensionLookupDialog.ColumnInfo.DimensionField"),
            ColumnInfo.COLUMN_TYPE_CCOMBO,
            new String[] {""},
            false);
    ciUpIns[1] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "DimensionLookupDialog.ColumnInfo.StreamField"),
            ColumnInfo.COLUMN_TYPE_CCOMBO,
            new String[] {""},
            false);
    ciUpIns[2] =
        new ColumnInfo(
            BaseMessages.getString(PKG, "DimensionLookupDialog.ColumnInfo.TypeOfDimensionUpdate"),
            ColumnInfo.COLUMN_TYPE_CCOMBO,
            input.isUpdate() ? DimensionLookupMeta.typeDesc : DimensionLookupMeta.typeDescLookup);
    tableFieldColumns.add(ciUpIns[0]);
    wUpIns =
        new TableView(
            variables,
            wFieldsComp,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
            ciUpIns,
            upInsRows,
            lsMod,
            props);

    FormData fdUpIns = new FormData();
    fdUpIns.left = new FormAttachment(0, 0);
    fdUpIns.top = new FormAttachment(wlUpIns, margin);
    fdUpIns.right = new FormAttachment(100, 0);
    fdUpIns.bottom = new FormAttachment(100, 0);
    wUpIns.setLayoutData(fdUpIns);

    //
    // Search the fields in the background
    //

    final Runnable runnable =
        () -> {
          TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
          if (transformMeta != null) {
            try {
              IRowMeta row = pipelineMeta.getPrevTransformFields(variables, transformMeta);

              // Remember these fields...
              for (int i = 0; i < row.size(); i++) {
                inputFields.put(row.getValueMeta(i).getName(), i);
              }

              setComboBoxes();
            } catch (HopException e) {
              logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
            }
          }
        };
    new Thread(runnable).start();

    FormData fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment(0, 0);
    fdFieldsComp.top = new FormAttachment(0, 0);
    fdFieldsComp.right = new FormAttachment(100, 0);
    fdFieldsComp.bottom = new FormAttachment(100, 0);
    wFieldsComp.setLayoutData(fdFieldsComp);

    wFieldsComp.layout();
    wFieldsTab.setControl(wFieldsComp);
  }

  public void addVersioningTab(int margin, int middle, ModifyListener lsMod) {

    wVersioningTab = new CTabItem(wTabFolder, SWT.NONE);
    wVersioningTab.setFont(GuiResource.getInstance().getFontDefault());
    wVersioningTab.setText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.VersioningTab.CTabItem"));

    Composite wVersioningComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wVersioningComp);

    FormLayout fieldsCompLayout = new FormLayout();
    fieldsCompLayout.marginWidth = PropsUi.getFormMargin();
    fieldsCompLayout.marginHeight = PropsUi.getFormMargin();
    wVersioningComp.setLayout(fieldsCompLayout);

    // Version key field:
    wlVersion = new Label(wVersioningComp, SWT.RIGHT);
    wlVersion.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Version.Label"));
    PropsUi.setLook(wlVersion);
    FormData fdlVersion = new FormData();
    fdlVersion.left = new FormAttachment(0, 0);
    fdlVersion.right = new FormAttachment(middle, -margin);
    fdlVersion.top = new FormAttachment(0, 2 * margin);
    wlVersion.setLayoutData(fdlVersion);
    wVersion = new CCombo(wVersioningComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wVersion);
    wVersion.addModifyListener(lsMod);
    FormData fdVersion = new FormData();
    fdVersion.left = new FormAttachment(middle, 0);
    fdVersion.top = new FormAttachment(0, 2 * margin);
    fdVersion.right = new FormAttachment(100, 0);
    wVersion.setLayoutData(fdVersion);
    wVersion.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do not trigger focusLost
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            getFieldsFromTable();
            shell.setCursor(null);
            busy.dispose();
          }
        });

    // Datefield line
    Label wlDatefield = new Label(wVersioningComp, SWT.RIGHT);
    wlDatefield.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Datefield.Label"));
    PropsUi.setLook(wlDatefield);
    FormData fdlDatefield = new FormData();
    fdlDatefield.left = new FormAttachment(0, 0);
    fdlDatefield.right = new FormAttachment(middle, -margin);
    fdlDatefield.top = new FormAttachment(wVersion, 2 * margin);
    wlDatefield.setLayoutData(fdlDatefield);
    wDatefield = new CCombo(wVersioningComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wDatefield);
    wDatefield.addModifyListener(lsMod);
    FormData fdDatefield = new FormData();
    fdDatefield.left = new FormAttachment(middle, 0);
    fdDatefield.top = new FormAttachment(wVersion, 2 * margin);
    fdDatefield.right = new FormAttachment(100, 0);
    wDatefield.setLayoutData(fdDatefield);
    wDatefield.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do not trigger focusLost
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            getFields();
            shell.setCursor(null);
            busy.dispose();
          }
        });

    // Fromdate line
    //
    // 0 [wlFromdate] middle [wFromdate] (100-middle)/3 [wlMinyear]
    // 2*(100-middle)/3 [wMinyear] 100%
    //
    Label wlFromdate = new Label(wVersioningComp, SWT.RIGHT);
    wlFromdate.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Fromdate.Label"));
    PropsUi.setLook(wlFromdate);
    FormData fdlFromdate = new FormData();
    fdlFromdate.left = new FormAttachment(0, 0);
    fdlFromdate.right = new FormAttachment(middle, -margin);
    fdlFromdate.top = new FormAttachment(wDatefield, 2 * margin);
    wlFromdate.setLayoutData(fdlFromdate);
    wFromdate = new CCombo(wVersioningComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wFromdate);
    wFromdate.addModifyListener(lsMod);
    FormData fdFromdate = new FormData();
    fdFromdate.left = new FormAttachment(middle, 0);
    fdFromdate.right = new FormAttachment(middle + (100 - middle) / 3, -margin);
    fdFromdate.top = new FormAttachment(wDatefield, 2 * margin);
    wFromdate.setLayoutData(fdFromdate);
    wFromdate.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do not trigger focusLost
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            getFieldsFromTable();
            shell.setCursor(null);
            busy.dispose();
          }
        });

    // Minyear line
    wlMinyear = new Label(wVersioningComp, SWT.RIGHT);
    wlMinyear.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Minyear.Label"));
    PropsUi.setLook(wlMinyear);
    FormData fdlMinyear = new FormData();
    fdlMinyear.left = new FormAttachment(wFromdate, margin);
    fdlMinyear.right = new FormAttachment(middle + 2 * (100 - middle) / 3, -margin);
    fdlMinyear.top = new FormAttachment(wDatefield, 2 * margin);
    wlMinyear.setLayoutData(fdlMinyear);
    wMinyear = new Text(wVersioningComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wMinyear);
    wMinyear.addModifyListener(lsMod);
    FormData fdMinyear = new FormData();
    fdMinyear.left = new FormAttachment(wlMinyear, margin);
    fdMinyear.right = new FormAttachment(100, 0);
    fdMinyear.top = new FormAttachment(wDatefield, 2 * margin);
    wMinyear.setLayoutData(fdMinyear);
    wMinyear.setToolTipText(BaseMessages.getString(PKG, "DimensionLookupDialog.Minyear.ToolTip"));

    // Add a line with an option to specify an alternative start date...
    //
    Label wlUseAltStartDate = new Label(wVersioningComp, SWT.RIGHT);
    wlUseAltStartDate.setText(
        BaseMessages.getString(PKG, "DimensionLookupDialog.UseAlternativeStartDate.Label"));
    PropsUi.setLook(wlUseAltStartDate);
    FormData fdlUseAltStartDate = new FormData();
    fdlUseAltStartDate.left = new FormAttachment(0, 0);
    fdlUseAltStartDate.right = new FormAttachment(middle, -margin);
    fdlUseAltStartDate.top = new FormAttachment(wFromdate, margin);
    wlUseAltStartDate.setLayoutData(fdlUseAltStartDate);
    wUseAltStartDate = new Button(wVersioningComp, SWT.CHECK);
    PropsUi.setLook(wUseAltStartDate);
    wUseAltStartDate.setToolTipText(
        BaseMessages.getString(
            PKG, "DimensionLookupDialog.UseAlternativeStartDate.Tooltip", Const.CR));
    FormData fdUseAltStartDate = new FormData();
    fdUseAltStartDate.left = new FormAttachment(middle, 0);
    fdUseAltStartDate.top = new FormAttachment(wlUseAltStartDate, 0, SWT.CENTER);
    wUseAltStartDate.setLayoutData(fdUseAltStartDate);
    wUseAltStartDate.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            setFlags();
            input.setChanged();
          }
        });

    // The choice...
    //
    wAltStartDate = new CCombo(wVersioningComp, SWT.BORDER);
    PropsUi.setLook(wAltStartDate);
    // All options except for "No alternative"...
    wAltStartDate.removeAll();
    for (int i = 1; i < DimensionLookupMeta.getStartDateAlternativeDescriptions().length; i++) {
      wAltStartDate.add(DimensionLookupMeta.getStartDateAlternativeDescriptions()[i]);
    }
    wAltStartDate.setText(
        BaseMessages.getString(
            PKG, "DimensionLookupDialog.AlternativeStartDate.SelectItemDefault"));
    wAltStartDate.setToolTipText(
        BaseMessages.getString(
            PKG, "DimensionLookupDialog.AlternativeStartDate.Tooltip", Const.CR));
    FormData fdAltStartDate = new FormData();
    fdAltStartDate.left = new FormAttachment(wUseAltStartDate, 2 * margin);
    fdAltStartDate.right = new FormAttachment(wUseAltStartDate, 200);
    fdAltStartDate.top = new FormAttachment(wFromdate, margin);
    wAltStartDate.setLayoutData(fdAltStartDate);
    wAltStartDate.addModifyListener(
        arg0 -> {
          setFlags();
          input.setChanged();
        });
    wAltStartDateField = new CCombo(wVersioningComp, SWT.SINGLE | SWT.BORDER);
    PropsUi.setLook(wAltStartDateField);
    wAltStartDateField.setToolTipText(
        BaseMessages.getString(
            PKG, "DimensionLookupDialog.AlternativeStartDateField.Tooltip", Const.CR));
    FormData fdAltStartDateField = new FormData();
    fdAltStartDateField.left = new FormAttachment(wAltStartDate, 2 * margin);
    fdAltStartDateField.right = new FormAttachment(100, 0);
    fdAltStartDateField.top = new FormAttachment(wFromdate, margin);
    wAltStartDateField.setLayoutData(fdAltStartDateField);
    wAltStartDateField.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do not trigger focusLost
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            getFieldsFromTable();
            shell.setCursor(null);
            busy.dispose();
          }
        });

    // Todate line
    Label wlTodate = new Label(wVersioningComp, SWT.RIGHT);
    wlTodate.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Todate.Label"));
    PropsUi.setLook(wlTodate);
    FormData fdlTodate = new FormData();
    fdlTodate.left = new FormAttachment(0, 0);
    fdlTodate.right = new FormAttachment(middle, -margin);
    fdlTodate.top = new FormAttachment(wAltStartDate, margin);
    wlTodate.setLayoutData(fdlTodate);
    wTodate = new CCombo(wVersioningComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wTodate);
    wTodate.addModifyListener(lsMod);
    FormData fdTodate = new FormData();
    fdTodate.left = new FormAttachment(middle, 0);
    fdTodate.right = new FormAttachment(middle + (100 - middle) / 3, -margin);
    fdTodate.top = new FormAttachment(wAltStartDate, margin);
    wTodate.setLayoutData(fdTodate);
    wTodate.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do not trigger focusLost
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            getFieldsFromTable();
            shell.setCursor(null);
            busy.dispose();
          }
        });

    // Maxyear line
    wlMaxyear = new Label(wVersioningComp, SWT.RIGHT);
    wlMaxyear.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.Maxyear.Label"));
    PropsUi.setLook(wlMaxyear);
    FormData fdlMaxyear = new FormData();
    fdlMaxyear.left = new FormAttachment(wTodate, margin);
    fdlMaxyear.right = new FormAttachment(middle + 2 * (100 - middle) / 3, -margin);
    fdlMaxyear.top = new FormAttachment(wAltStartDate, margin);
    wlMaxyear.setLayoutData(fdlMaxyear);
    wMaxyear = new Text(wVersioningComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wMaxyear);
    wMaxyear.addModifyListener(lsMod);
    FormData fdMaxyear = new FormData();
    fdMaxyear.left = new FormAttachment(wlMaxyear, margin);
    fdMaxyear.right = new FormAttachment(100, 0);
    fdMaxyear.top = new FormAttachment(wAltStartDate, margin);
    wMaxyear.setLayoutData(fdMaxyear);
    wMaxyear.setToolTipText(BaseMessages.getString(PKG, "DimensionLookupDialog.Maxyear.ToolTip"));

    FormData fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment(0, 0);
    fdFieldsComp.top = new FormAttachment(0, 0);
    fdFieldsComp.right = new FormAttachment(100, 0);
    fdFieldsComp.bottom = new FormAttachment(100, 0);
    wVersioningComp.setLayoutData(fdFieldsComp);

    wVersioningComp.layout();
    wVersioningTab.setControl(wVersioningComp);
  }

  public void setFlags() {
    ColumnInfo colinf =
        new ColumnInfo(
            BaseMessages.getString(PKG, "DimensionLookupDialog.ColumnInfo.Type"),
            ColumnInfo.COLUMN_TYPE_CCOMBO,
            input.isUpdate() ? DimensionLookupMeta.typeDesc : DimensionLookupMeta.typeDescLookup);
    wUpIns.setColumnInfo(2, colinf);

    if (input.isUpdate()) {
      wUpIns.setColumnText(
          2,
          BaseMessages.getString(
              PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnText.SteamFieldToCompare"));
      wUpIns.setColumnText(
          3,
          BaseMessages.getString(
              PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnTextTypeOfDimensionUpdate"));
      wUpIns.setColumnToolTip(
          2,
          BaseMessages.getString(PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnToolTip")
              + Const.CR
              + "Punch Through: Kimball Type I"
              + Const.CR
              + "Update: Correct error in last version");
    } else {
      wUpIns.setColumnText(
          2,
          BaseMessages.getString(
              PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnText.NewNameOfOutputField"));
      wUpIns.setColumnText(
          3,
          BaseMessages.getString(
              PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnText.TypeOfReturnField"));
      wUpIns.setColumnToolTip(
          2,
          BaseMessages.getString(PKG, "DimensionLookupDialog.UpdateOrInsertFields.ColumnToolTip2"));
    }
    wUpIns.optWidth(true);

    // In case of lookup: disable commitsize, etc.
    boolean update = wUpdate.getSelection();
    wlCommit.setEnabled(update);
    wCommit.setEnabled(update);
    wlMinyear.setEnabled(update);
    wMinyear.setEnabled(update);
    wlMaxyear.setEnabled(update);
    wMaxyear.setEnabled(update);
    wlMinyear.setEnabled(update);
    wMinyear.setEnabled(update);
    wlVersion.setEnabled(update);
    wVersion.setEnabled(update);
    wlTkRename.setEnabled(!update);
    wTkRename.setEnabled(!update);

    wCreate.setEnabled(update);

    // Set the technical creation key fields correct... then disable
    // depending on update or not. Then reset if we're updating. It makes
    // sure that the disabled options because of database restrictions
    // will always be properly grayed out.
    setAutoincUse();
    setSequence();
    setTableMax();

    // Surpisingly we can't disable these fields as they influence the
    // calculation of the "Unknown" key
    // If we have a MySQL database with Auto-increment for example, the
    // "unknown" is 1.
    // If we have a MySQL database with Table-max the "unknown" is 0.
    //

    if (update) {
      setAutoincUse();
      setSequence();
      setTableMax();
    }

    // The alternative start date
    //
    wAltStartDate.setEnabled(wUseAltStartDate.getSelection());
    int alternative = DimensionLookupMeta.getStartDateAlternative(wAltStartDate.getText());
    wAltStartDateField.setEnabled(
        alternative == DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE);

    // Caching...
    //
    wlPreloadCache.setEnabled(wUseCache.getSelection() && !wUpdate.getSelection());
    wPreloadCache.setEnabled(wUseCache.getSelection() && !wUpdate.getSelection());

    wlCacheSize.setEnabled(wUseCache.getSelection() && !wPreloadCache.getSelection());
    wCacheSize.setEnabled(wUseCache.getSelection() && !wPreloadCache.getSelection());
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<>();

    // Add the currentMeta fields...
    fields.putAll(inputFields);

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<>(keySet);

    String[] fieldNames = entries.toArray(new String[entries.size()]);
    Const.sortStrings(fieldNames);
    ciKey[1].setComboValues(fieldNames);
    ciUpIns[1].setComboValues(fieldNames);
  }

  public void setAutoincUse() {
    boolean enable = (ci == null) || (ci.supportsAutoinc() && ci.supportsAutoGeneratedKeys());

    wAutoinc.setEnabled(enable);
    if (!enable && wAutoinc.getSelection()) {
      wAutoinc.setSelection(false);
      wSeqButton.setSelection(false);
      wTableMax.setSelection(true);
    }
  }

  public void setTableMax() {
    wTableMax.setEnabled(true);
  }

  public void setSequence() {
    boolean seq = (ci == null) || ci.supportsSequences();
    wSeq.setEnabled(seq);
    wSeqButton.setEnabled(seq);
    if (!seq && wSeqButton.getSelection()) {
      wAutoinc.setSelection(false);
      wSeqButton.setSelection(false);
      wTableMax.setSelection(true);
    }
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    if (log.isDebug()) {
      logDebug(BaseMessages.getString(PKG, "DimensionLookupDialog.Log.GettingKeyInfo"));
    }

    if (input.getKeyStream() != null) {
      for (int i = 0; i < input.getKeyStream().length; i++) {
        TableItem item = wKey.table.getItem(i);
        if (input.getKeyLookup()[i] != null) {
          item.setText(1, input.getKeyLookup()[i]);
        }
        if (input.getKeyStream()[i] != null) {
          item.setText(2, input.getKeyStream()[i]);
        }
      }
    }

    if (input.getFieldStream() != null) {
      for (int i = 0; i < input.getFieldStream().length; i++) {
        TableItem item = wUpIns.table.getItem(i);
        if (input.getFieldLookup()[i] != null) {
          item.setText(1, input.getFieldLookup()[i]);
        }
        if (input.getFieldStream()[i] != null) {
          item.setText(2, input.getFieldStream()[i]);
        }
        item.setText(
            3, DimensionLookupMeta.getUpdateType(input.isUpdate(), input.getFieldUpdate()[i]));
      }
    }

    wUpdate.setSelection(input.isUpdate());

    if (input.getSchemaName() != null) {
      wSchema.setText(input.getSchemaName());
    }
    if (input.getTableName() != null) {
      wTable.setText(input.getTableName());
    }
    if (input.getKeyField() != null) {
      wTk.setText(input.getKeyField());
    }
    if (input.getKeyRename() != null) {
      wTkRename.setText(input.getKeyRename());
    }

    wAutoinc.setSelection(input.isAutoIncrement());

    if (input.getVersionField() != null) {
      wVersion.setText(input.getVersionField());
    }
    if (input.getSequenceName() != null) {
      wSeq.setText(input.getSequenceName());
    }
    if (input.getDatabaseMeta() != null) {
      wConnection.setText(input.getDatabaseMeta().getName());
    }
    if (input.getDateField() != null) {
      wDatefield.setText(input.getDateField());
    }
    if (input.getDateFrom() != null) {
      wFromdate.setText(input.getDateFrom());
    }
    if (input.getDateTo() != null) {
      wTodate.setText(input.getDateTo());
    }

    String techKeyCreation = input.getTechKeyCreation();
    if (techKeyCreation == null) {
      // Determine the creation of the technical key for
      // backwards compatibility. Can probably be removed at
      // version 3.x or so (Sven Boden).
      DatabaseMeta database = input.getDatabaseMeta();
      if (database == null || !database.supportsAutoinc()) {
        input.setAutoIncrement(false);
      }
      wAutoinc.setSelection(input.isAutoIncrement());

      wSeqButton.setSelection(
          input.getSequenceName() != null && input.getSequenceName().length() > 0);
      if (!input.isAutoIncrement()
          && (input.getSequenceName() == null || input.getSequenceName().length() <= 0)) {
        wTableMax.setSelection(true);
      }

      if (database != null && database.supportsSequences() && input.getSequenceName() != null) {
        wSeq.setText(input.getSequenceName());
        input.setAutoIncrement(false);
        wTableMax.setSelection(false);
      }
    } else {
      // HOP post 2.2 version:
      // The "creation" field now determines the behaviour of the
      // key creation.
      if (DimensionLookupMeta.CREATION_METHOD_AUTOINC.equals(techKeyCreation)) {
        wAutoinc.setSelection(true);
        wSeqButton.setSelection(false);
        wTableMax.setSelection(false);
      } else if ((DimensionLookupMeta.CREATION_METHOD_SEQUENCE.equals(techKeyCreation))) {
        wSeqButton.setSelection(true);
        wAutoinc.setSelection(false);
        wTableMax.setSelection(false);
      } else { // the rest
        wTableMax.setSelection(true);
        wAutoinc.setSelection(false);
        wSeqButton.setSelection(false);
        input.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_TABLEMAX);
      }
      if (input.getSequenceName() != null) {
        wSeq.setText(input.getSequenceName());
      }
    }

    wCommit.setText("" + input.getCommitSize());

    wUseCache.setSelection(input.getCacheSize() >= 0);
    wPreloadCache.setSelection(input.isPreloadingCache());
    if (input.getCacheSize() >= 0) {
      wCacheSize.setText("" + input.getCacheSize());
    }

    wMinyear.setText("" + input.getMinYear());
    wMaxyear.setText("" + input.getMaxYear());

    wUpIns.removeEmptyRows();
    wUpIns.setRowNums();
    wUpIns.optWidth(true);
    wKey.removeEmptyRows();
    wKey.setRowNums();
    wKey.optWidth(true);

    ci = pipelineMeta.findDatabase(wConnection.getText());

    // The alternative start date...
    //
    wUseAltStartDate.setSelection(input.isUsingStartDateAlternative());
    if (input.isUsingStartDateAlternative()) {
      wAltStartDate.setText(
          DimensionLookupMeta.getStartDateAlternativeDesc(input.getStartDateAlternative()));
    }
    wAltStartDateField.setText(Const.NVL(input.getStartDateFieldName(), ""));

    setFlags();

    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void cancel() {
    transformName = null;
    input.setChanged(backupChanged);
    input.setUpdate(backupUpdate);
    input.setAutoIncrement(backupAutoInc);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }

    getInfo(input);

    transformName = wTransformName.getText(); // return value

    if (input.getDatabaseMeta() == null) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(
          BaseMessages.getString(PKG, "DimensionLookupDialog.InvalidConnection.DialogMessage"));
      mb.setText(
          BaseMessages.getString(PKG, "DimensionLookupDialog.InvalidConnection.DialogTitle"));
      mb.open();
      return;
    }

    dispose();
  }

  private void getInfo(DimensionLookupMeta in) {
    in.setUpdate(wUpdate.getSelection());

    int nrkeys = wKey.nrNonEmpty();
    int nrFields = wUpIns.nrNonEmpty();

    in.allocate(nrkeys, nrFields);

    logDebug(
        BaseMessages.getString(PKG, "DimensionLookupDialog.Log.FoundKeys", String.valueOf(nrkeys)));
    // CHECKSTYLE:Indentation:OFF
    for (int i = 0; i < nrkeys; i++) {
      TableItem item = wKey.getNonEmpty(i);
      in.getKeyLookup()[i] = item.getText(1);
      in.getKeyStream()[i] = item.getText(2);
    }

    if (log.isDebug()) {
      logDebug(
          BaseMessages.getString(
              PKG, "DimensionLookupDialog.Log.FoundFields", String.valueOf(nrFields)));
    }
    // CHECKSTYLE:Indentation:OFF
    for (int i = 0; i < nrFields; i++) {
      TableItem item = wUpIns.getNonEmpty(i);
      in.getFieldLookup()[i] = item.getText(1);
      in.getFieldStream()[i] = item.getText(2);

      if (wUpdate.getSelection())
        in.getFieldUpdate()[i] = DimensionLookupMeta.getUpdateType(in.isUpdate(), item.getText(3));
      else
        in.getReturnType()[i] = DimensionLookupMeta.getUpdateType(in.isUpdate(), item.getText(3));
    }

    in.setSchemaName(wSchema.getText());
    in.setTableName(wTable.getText());
    in.setKeyField(wTk.getText());
    in.setKeyRename(wTkRename.getText());
    if (wAutoinc.getSelection()) {
      in.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_AUTOINC);
      in.setAutoIncrement(true); // for downwards compatibility
      in.setSequenceName(null);
    } else if (wSeqButton.getSelection()) {
      in.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_SEQUENCE);
      in.setAutoIncrement(false);
      in.setSequenceName(wSeq.getText());
    } else { // all the rest
      in.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_TABLEMAX);
      in.setAutoIncrement(false);
      in.setSequenceName(null);
    }

    in.setAutoIncrement(wAutoinc.getSelection());

    if (in.getKeyRename() != null && in.getKeyRename().equalsIgnoreCase(in.getKeyField())) {
      in.setKeyRename(null); // Don't waste variables&time if it's the same
    }

    in.setVersionField(wVersion.getText());
    in.setDatabaseMeta(pipelineMeta.findDatabase(wConnection.getText()));
    in.setDateField(wDatefield.getText());
    in.setDateFrom(wFromdate.getText());
    in.setDateTo(wTodate.getText());

    in.setCommitSize(Const.toInt(wCommit.getText(), 0));

    if (wUseCache.getSelection()) {
      in.setCacheSize(Const.toInt(wCacheSize.getText(), -1));
    } else {
      in.setCacheSize(-1);
    }
    in.setPreloadingCache(wPreloadCache.getSelection());
    if (wPreloadCache.getSelection()) {
      in.setCacheSize(0);
    }

    in.setMinYear(Const.toInt(wMinyear.getText(), Const.MIN_YEAR));
    in.setMaxYear(Const.toInt(wMaxyear.getText(), Const.MAX_YEAR));

    in.setUsingStartDateAlternative(wUseAltStartDate.getSelection());
    in.setStartDateAlternative(
        DimensionLookupMeta.getStartDateAlternative(wAltStartDate.getText()));
    in.setStartDateFieldName(wAltStartDateField.getText());
  }

  private void getTableName() {
    String connectionName = wConnection.getText();
    if (StringUtils.isEmpty(connectionName)) {
      return;
    }
    DatabaseMeta databaseMeta = pipelineMeta.findDatabase(connectionName);
    if (databaseMeta == null) {
      return;
    }
    logDebug(
        BaseMessages.getString(PKG, "DimensionLookupDialog.Log.LookingAtConnection")
            + databaseMeta.toString());

    DatabaseExplorerDialog std =
        new DatabaseExplorerDialog(
            shell, SWT.NONE, variables, databaseMeta, pipelineMeta.getDatabases());
    std.setSelectedSchemaAndTable(wSchema.getText(), wTable.getText());
    if (std.open()) {
      wSchema.setText(Const.NVL(std.getSchemaName(), ""));
      wTable.setText(Const.NVL(std.getTableName(), ""));
      setTableFieldCombo();
    }
  }

  private void get() {
    if (wTabFolder.getSelection() == wFieldsTab) {
      if (input.isUpdate()) {
        getUpdate();
      } else {
        getLookup();
      }
    } else {
      getKeys();
    }
  }

  /**
   * Get the fields from the previous transform and use them as "update fields". Only get the the
   * fields which are not yet in use as key, or in the field table. Also ignore technical key,
   * version, fromdate, todate.
   */
  private void getUpdate() {
    try {
      IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
      if (r != null && !r.isEmpty()) {
        BaseTransformDialog.getFieldsFromPrevious(
            r,
            wUpIns,
            2,
            new int[] {1, 2},
            new int[] {},
            -1,
            -1,
            (tableItem, v) -> {
              tableItem.setText(
                  3, BaseMessages.getString(PKG, "DimensionLookupDialog.TableItem.Insert.Label"));

              int idx = wKey.indexOfString(v.getName(), 2);
              return idx < 0
                  && !v.getName().equalsIgnoreCase(wTk.getText())
                  && !v.getName().equalsIgnoreCase(wVersion.getText())
                  && !v.getName().equalsIgnoreCase(wFromdate.getText())
                  && !v.getName().equalsIgnoreCase(wTodate.getText());
            });
      }
    } catch (HopException ke) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "DimensionLookupDialog.FailedToGetFields.DialogTitle"),
          BaseMessages.getString(PKG, "DimensionLookupDialog.FailedToGetFields.DialogMessage"),
          ke);
    }
  }

  // Set table "dimension field" and "technical key" drop downs
  private void setTableFieldCombo() {

    Runnable fieldLoader =
        () -> {
          if (!wTable.isDisposed() && !wConnection.isDisposed() && !wSchema.isDisposed()) {
            final String tableName = wTable.getText();
            final String connectionName = wConnection.getText();
            final String schemaName = wSchema.getText();

            // clear
            for (ColumnInfo colInfo : tableFieldColumns) {
              colInfo.setComboValues(new String[] {});
            }
            // Ensure other table field dropdowns are refreshed fields when they
            // next get focus
            gotTableFields = false;
            if (!Utils.isEmpty(tableName)) {
              DatabaseMeta databaseMeta = pipelineMeta.findDatabase(connectionName);
              if (databaseMeta != null) {
                Database db = new Database(loggingObject, variables, databaseMeta);
                try {
                  db.connect();

                  IRowMeta r =
                      db.getTableFieldsMeta(
                          variables.resolve(schemaName), variables.resolve(tableName));
                  if (null != r) {
                    String[] fieldNames = r.getFieldNames();
                    if (null != fieldNames) {
                      for (ColumnInfo colInfo : tableFieldColumns) {
                        colInfo.setComboValues(fieldNames);
                      }
                      wTk.setItems(fieldNames);
                    }
                  }
                } catch (Exception e) {
                  for (ColumnInfo colInfo : tableFieldColumns) {
                    colInfo.setComboValues(new String[] {});
                  }
                  // ignore any errors here. drop downs will not be
                  // filled, but no problem for the user
                } finally {
                  try {
                    if (db != null) {
                      db.disconnect();
                    }
                  } catch (Exception ignored) {
                    // ignore any errors here.
                    db = null;
                  }
                }
              }
            }
          }
        };
    shell.getDisplay().asyncExec(fieldLoader);
  }

  /**
   * Get the fields from the table in the database and use them as lookup keys. Only get the the
   * fields which are not yet in use as key, or in the field table. Also ignore technical key,
   * version, fromdate, todate.
   */
  private void getLookup() {
    DatabaseMeta databaseMeta = pipelineMeta.findDatabase(wConnection.getText());
    if (databaseMeta != null) {
      Database db = new Database(loggingObject, variables, databaseMeta);
      try {
        db.connect();
        IRowMeta r = db.getTableFieldsMeta(wSchema.getText(), wTable.getText());
        if (r != null && !r.isEmpty()) {
          BaseTransformDialog.getFieldsFromPrevious(
              r,
              wUpIns,
              2,
              new int[] {1, 2},
              new int[] {3},
              -1,
              -1,
              (tableItem, v) -> {
                int idx = wKey.indexOfString(v.getName(), 2);
                return idx < 0
                    && !v.getName().equalsIgnoreCase(wTk.getText())
                    && !v.getName().equalsIgnoreCase(wVersion.getText())
                    && !v.getName().equalsIgnoreCase(wFromdate.getText())
                    && !v.getName().equalsIgnoreCase(wTodate.getText());
              });
        }
      } catch (HopException e) {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.ErrorOccurred.DialogTitle"));
        mb.setMessage(
            BaseMessages.getString(PKG, "DimensionLookupDialog.ErrorOccurred.DialogMessage")
                + Const.CR
                + e.getMessage());
        mb.open();
      } finally {
        db.disconnect();
      }
    }
  }

  private void getFields() {
    if (!gotPreviousFields) {
      try {
        String field = wDatefield.getText();
        IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
        if (r != null) {
          wDatefield.setItems(r.getFieldNames());
        }
        if (field != null) {
          wDatefield.setText(field);
        }
      } catch (HopException ke) {
        new ErrorDialog(
            shell,
            BaseMessages.getString(PKG, "DimensionLookupDialog.ErrorGettingFields.Title"),
            BaseMessages.getString(PKG, "DimensionLookupDialog.ErrorGettingFields.Message"),
            ke);
      }
      gotPreviousFields = true;
    }
  }

  private void getFieldsFromTable() {
    if (!gotTableFields) {
      if (!Utils.isEmpty(wTable.getText())) {
        DatabaseMeta databaseMeta = pipelineMeta.findDatabase(wConnection.getText());
        if (databaseMeta != null) {
          Database db = new Database(loggingObject, variables, databaseMeta);
          try {
            db.connect();
            IRowMeta r =
                db.getTableFieldsMeta(
                    variables.resolve(wSchema.getText()), variables.resolve(wTable.getText()));
            if (null != r) {
              String[] fieldNames = r.getFieldNames();
              if (null != fieldNames) {
                // Version
                String version = wVersion.getText();
                wVersion.setItems(fieldNames);
                if (version != null) {
                  wVersion.setText(version);
                }
                // from date
                String fromdate = wFromdate.getText();
                wFromdate.setItems(fieldNames);
                if (fromdate != null) {
                  wFromdate.setText(fromdate);
                }
                // to date
                String todate = wTodate.getText();
                wTodate.setItems(fieldNames);
                if (todate != null) {
                  wTodate.setText(todate);
                }
                // tk
                String tk = wTk.getText();
                wTk.setItems(fieldNames);
                if (tk != null) {
                  wTk.setText(tk);
                }
                // AltStartDateField
                String sd = wAltStartDateField.getText();
                wAltStartDateField.setItems(fieldNames);
                if (sd != null) {
                  wAltStartDateField.setText(sd);
                }
              }
            }
          } catch (Exception e) {

            // ignore any errors here. drop downs will not be
            // filled, but no problem for the user
          }
        }
      }
      gotTableFields = true;
    }
  }

  /**
   * Get the fields from the previous transform and use them as "keys". Only get the the fields
   * which are not yet in use as key, or in the field table. Also ignore technical key, version,
   * fromdate, todate.
   */
  private void getKeys() {
    try {
      IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
      if (r != null && !r.isEmpty()) {
        BaseTransformDialog.getFieldsFromPrevious(
            r,
            wKey,
            2,
            new int[] {1, 2},
            new int[] {3},
            -1,
            -1,
            (tableItem, v) -> {
              int idx = wKey.indexOfString(v.getName(), 2);
              return idx < 0
                  && !v.getName().equalsIgnoreCase(wTk.getText())
                  && !v.getName().equalsIgnoreCase(wVersion.getText())
                  && !v.getName().equalsIgnoreCase(wFromdate.getText())
                  && !v.getName().equalsIgnoreCase(wTodate.getText());
            });

        Table table = wKey.table;
        for (int i = 0; i < r.size(); i++) {
          IValueMeta v = r.getValueMeta(i);
          int idx = wKey.indexOfString(v.getName(), 2);
          int idy = wUpIns.indexOfString(v.getName(), 2);
          if (idx < 0
              && idy < 0
              && !v.getName().equalsIgnoreCase(wTk.getText())
              && !v.getName().equalsIgnoreCase(wVersion.getText())
              && !v.getName().equalsIgnoreCase(wFromdate.getText())
              && !v.getName().equalsIgnoreCase(wTodate.getText())) {
            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setText(1, v.getName());
            ti.setText(2, v.getName());
            ti.setText(3, v.getTypeDesc());
          }
        }
        wKey.removeEmptyRows();
        wKey.setRowNums();
        wKey.optWidth(true);
      }
    } catch (HopException ke) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "DimensionLookupDialog.FailedToGetFields.DialogTitle"),
          BaseMessages.getString(PKG, "DimensionLookupDialog.FailedToGetFields.DialogMessage"),
          ke);
    }
  }

  // Generate code for create table...
  // Conversions done by Database
  // For Sybase ASE: don't keep everything in lowercase!
  private void create() {
    try {
      DimensionLookupMeta info = new DimensionLookupMeta();
      getInfo(info);

      String name = transformName; // new name might not yet be linked to other
      // transforms!
      TransformMeta transforminfo =
          new TransformMeta(
              BaseMessages.getString(PKG, "DimensionLookupDialog.Transforminfo.Title"), name, info);
      IRowMeta prev = pipelineMeta.getPrevTransformFields(variables, transformName);

      String message = null;
      if (Utils.isEmpty(info.getKeyField())) {
        message =
            BaseMessages.getString(PKG, "DimensionLookupDialog.Error.NoTechnicalKeySpecified");
      }
      if (Utils.isEmpty(info.getTableName())) {
        message = BaseMessages.getString(PKG, "DimensionLookupDialog.Error.NoTableNameSpecified");
      }

      if (message == null) {
        SqlStatement sql =
            info.getSqlStatements(variables, pipelineMeta, transforminfo, prev, metadataProvider);
        if (!sql.hasError()) {
          if (sql.hasSql()) {
            SqlEditor sqledit =
                new SqlEditor(
                    shell,
                    SWT.NONE,
                    variables,
                    info.getDatabaseMeta(),
                    DbCache.getInstance(),
                    sql.getSql());
            sqledit.open();
          } else {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(
                BaseMessages.getString(PKG, "DimensionLookupDialog.NoSQLNeeds.DialogMessage"));
            mb.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.NoSQLNeeds.DialogTitle"));
            mb.open();
          }
        } else {
          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
          mb.setMessage(sql.getError());
          mb.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.SQLError.DialogTitle"));
          mb.open();
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setMessage(message);
        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
        mb.open();
      }
    } catch (HopException ke) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "DimensionLookupDialog.UnableToBuildSQLError.DialogMessage"),
          BaseMessages.getString(PKG, "DimensionLookupDialog.UnableToBuildSQLError.DialogTitle"),
          ke);
    }
  }

  private void getSchemaNames() {
    DatabaseMeta databaseMeta = pipelineMeta.findDatabase(wConnection.getText());
    if (databaseMeta != null) {
      Database database = new Database(loggingObject, variables, databaseMeta);
      try {
        database.connect();
        String[] schemas = database.getSchemas();

        if (null != schemas && schemas.length > 0) {
          schemas = Const.sortStrings(schemas);
          EnterSelectionDialog dialog =
              new EnterSelectionDialog(
                  shell,
                  schemas,
                  BaseMessages.getString(
                      PKG, "DimensionLookupDialog.AvailableSchemas.Title", wConnection.getText()),
                  BaseMessages.getString(
                      PKG,
                      "DimensionLookupDialog.AvailableSchemas.Message",
                      wConnection.getText()));
          String d = dialog.open();
          if (d != null) {
            wSchema.setText(Const.NVL(d, ""));
            setTableFieldCombo();
          }

        } else {
          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
          mb.setMessage(BaseMessages.getString(PKG, "DimensionLookupDialog.NoSchema.Error"));
          mb.setText(BaseMessages.getString(PKG, "DimensionLookupDialog.GetSchemas.Error"));
          mb.open();
        }
      } catch (Exception e) {
        new ErrorDialog(
            shell,
            BaseMessages.getString(PKG, "System.Dialog.Error.Title"),
            BaseMessages.getString(PKG, "DimensionLookupDialog.ErrorGettingSchemas"),
            e);
      } finally {
        database.disconnect();
      }
    }
  }
}
