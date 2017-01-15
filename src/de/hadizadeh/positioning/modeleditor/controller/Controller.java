package de.hadizadeh.positioning.modeleditor.controller;

import de.hadizadeh.positioning.modeleditor.DialogTemplate;
import de.hadizadeh.positioning.modeleditor.EditorMap;
import de.hadizadeh.positioning.roommodel.FileManager;
import de.hadizadeh.positioning.roommodel.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

/**
 * Controller for the menu and the communication between the content and room model controllers
 */
public class Controller implements Initializable {
    private static final String SETTINGS_FILE = System.getProperty("user.home") + File.separator + ".Room-Model-Editor" + File.separator + "settings.properties";
    private static final String DEFAULT_WEBSERVICE_URL = "http://hadizadeh.de:8080/indoor-positioning/";
    private static final String DEFAULT_AUTH_TOKEN = "74dc2f347d73ef4fbec38de0429b1357fcb5687f1879495c6e2bf289f6d3411eb391bf4bd2608176749040478bdac6fb5566543cedc9b963114a3b4cc98a032f";
    private static final String DEFAULT_PROJECT_NAME = "PositioningProject";

    @FXML
    GridPane rootGridPane;
    @FXML
    MenuBar mainMb;
    @FXML
    MenuItem menuNewMi;
    @FXML
    MenuItem menuOpenMi;
    @FXML
    MenuItem menuSaveMi;
    @FXML
    MenuItem menuSaveAsMi;
    @FXML
    MenuItem menuZoomIn;
    @FXML
    MenuItem menuZoomOut;
    @FXML
    MenuItem menuUpload;
    @FXML
    TabPane mainTp;
    @FXML
    RoomModelController roomModelController;
    @FXML
    EditorContentController editorContentController;
    @FXML
    Label statusLabel;
    @FXML
    VBox loadingVb;
    @FXML
    Label loadingTextLb;
    @FXML
    Label loadingProgressTextLb;
    @FXML
    ProgressBar loadingProgressBar;

    private String workingDirectory;
    private File workingFile;
    private ResourceBundle resourceBundle;
    private boolean saving = false;
    private boolean savingQueueActive = false;
    private boolean contentTabActive;
    private boolean pendingUpload;
    private boolean pendingDownload;
    private File pendingDownloadFile;

    private Image createImage;
    private Image createImageDisabled;
    private Image removeImage;
    private Image removeImageDisabled;

    private String settingsWebserviceUrl;
    private String settingsAuthToken;
    private String settingsProjectName;


//    private void test() {
//        Parser parser = ParserBuilder.createDefaultParser();
//        try {
//
//            //parse
//            parser.parse(new FileInputStream("C:\\Users\\fisch\\Desktop\\test.dxf"), DXFParser.DEFAULT_ENCODING);
//
//            //get the documnet and the layer
//            DXFDocument doc = parser.getDocument();
//            Iterator layerIterator = doc.getDXFLayerIterator();
//
//            while(layerIterator.hasNext()) {
//                //System.out.println("LAYER");
//                DXFLayer layer = (DXFLayer)layerIterator.next();
//
//                double startX = layer.getBounds().getMinimumX();
//                double startY = layer.getBounds().getMinimumY();
//                double startZ = layer.getBounds().getMinimumZ();
//
//                List plines = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
//
//
//                Iterator entityTypeIterator = layer.getDXFEntityTypeIterator();
//                while(entityTypeIterator.hasNext()) {
//                    String entityType = (String)entityTypeIterator.next();
//                    System.out.println("LINE: "+entityType);
//                }
//                //System.out.println("LINE: "+plines);
//
//
//                if(plines != null) {
//                    System.out.println("HAS PLINES");
//                    for (DXFPolyline pline : (List<DXFPolyline>)plines) {
//                        System.out.println("PLINES");
//
//                        //System.out.println("COLUMNS: " + pline.getBounds().getMinimumX() + ", "+pline.getBounds().getMinimumY());
//                        for (int i = 0; i < pline.getVertexCount(); i++) {
//
//                            //System.out.println("ROWS: " + pline.getRows());
//
//                            DXFVertex vertex = pline.getVertex(i);
//                            System.out.println((vertex.getX()-startX) + ", " + (vertex.getY()-startY) + ", " + (vertex.getZ()-startZ));
//                        }
//                    }
//                }
//            }
//
//            //DXFLayer layer = doc.getDXFLayer(layerid);
//
//            //get all polylines from the layer
//
//
//            //work with the first polyline
//            //doSomething((DXFPolyline) plines.get(0));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Initializes the controller and loads needed resources
     *
     * @param url            url (not needed)
     * @param resourceBundle resource bundle for data
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //test();
        FileManager.getTmpName("");
        this.resourceBundle = resourceBundle;
        mainTp.setVisible(false);
        roomModelController.setEditorContentController(editorContentController);
        editorContentController.setRoomModelController(roomModelController);
        roomModelController.load(resourceBundle, statusLabel);
        editorContentController.load(resourceBundle, statusLabel);

        createImage = new Image(Controller.class.getResourceAsStream("/res/img/create.png"));
        createImageDisabled = new Image(Controller.class.getResourceAsStream("/res/img/create_disabled.png"));
        removeImage = new Image(Controller.class.getResourceAsStream("/res/img/delete.png"));
        removeImageDisabled = new Image(Controller.class.getResourceAsStream("/res/img/delete_disabled.png"));

        menuNewMi.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        menuOpenMi.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        menuSaveMi.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuZoomIn.setAccelerator(new KeyCodeCombination(KeyCode.PLUS));
        menuZoomOut.setAccelerator(new KeyCodeCombination(KeyCode.MINUS));

        menuSaveMi.setDisable(true);
        menuSaveAsMi.setDisable(true);
        menuZoomIn.setDisable(true);
        menuZoomOut.setDisable(true);
        menuUpload.setDisable(true);

        loadSettings();

        mainTp.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (!contentTabActive && key.getCode().equals(KeyCode.ADD)) {
                    actionMenuZoomIn();
                } else if (!contentTabActive && key.getCode().equals(KeyCode.SUBTRACT)) {
                    actionMenuZoomOut();
                } else if (key.isControlDown() && key.getCode().equals(KeyCode.S)) {
                    //actionMenuSave();
                }
            }
        });

        mainTp.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                if (newValue.intValue() == 1) {
                    contentTabActive = true;
                    editorContentController.refreshContentsLv();
                    editorContentController.reselectContent();
                    menuZoomIn.setDisable(true);
                    menuZoomOut.setDisable(true);
                } else {
                    contentTabActive = false;
                    menuZoomIn.setDisable(false);
                    menuZoomOut.setDisable(false);
                    roomModelController.selectEraser();
                }
            }
        });
    }

    /**
     * Zooming in the map
     */
    @FXML
    protected void actionMenuZoomIn() {
        roomModelController.resizeMap(4);
    }

    /**
     * Zooming out of the map
     */
    @FXML
    protected void actionMenuZoomOut() {
        roomModelController.resizeMap(-4);
    }

    /**
     * Uploads data to server with user interaction
     */
    @FXML
    protected void actionMenuUpload() {
        if (DialogTemplate.showYesNoDialog(resourceBundle.getString("mainUploadFileShort"), resourceBundle.getString("mainUploadFile"), resourceBundle.getString("mainUploadFileText"))) {
            pendingUpload = true;
            actionMenuSave();
        }
    }

    /**
     * Downloads data from server
     */
    @FXML
    protected void actionMenuDownload() {
        String text;
        if (workingDirectory == null) {
            text = resourceBundle.getString("mainDownloadFileTextNoFile");
        } else {
            text = resourceBundle.getString("mainDownloadFileText");
        }

        if (DialogTemplate.showYesNoDialog(resourceBundle.getString("mainDownloadFileShort"), resourceBundle.getString("mainDownloadFile"), text)) {
            File selectedFile = DialogTemplate.showFileSaveDialog(resourceBundle.getString("mainDownloadFileShort"), (Stage) rootGridPane.getScene().getWindow());
            if (selectedFile != null) {
                pendingDownload = true;
                pendingDownloadFile = selectedFile;
                if (workingDirectory == null) {
                    saveCompleted();
                } else {
                    actionMenuSave();
                }
            }
        }
    }

    private void syncData(boolean upload, String url, String token, String statusText, File file, String errorTitle, String errorHeader, String errorText) {
        FileManager.ProgressListener progressListener = new FileManager.ProgressListener() {
            @Override
            public void progress(final int percent) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingProgressTextLb.setText(percent + "%");
                        loadingProgressBar.setProgress(percent / 100.0);
                    }
                });
            }
        };
        showStateBusy(statusText);
        loadingProgressTextLb.setVisible(true);
        new Thread() {
            public void run() {
                int statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
                if (upload) {
                    statusCode = FileManager.uploadFile(url, token, file, progressListener);
                } else {
                    statusCode = FileManager.downloadFile(url, token, file, progressListener);
                }
                pendingUpload = false;
                final int resultStatusCode = statusCode;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingProgressTextLb.setVisible(false);
                        showStateReady("");
                        loadingProgressBar.setProgress(-1);
                        boolean error = false;
                        if (upload) {
                            if (resultStatusCode != HttpURLConnection.HTTP_CREATED) {
                                error = true;
                            }
                        } else {
                            if (resultStatusCode != HttpURLConnection.HTTP_OK && resultStatusCode != HttpURLConnection.HTTP_NO_CONTENT) {
                                error = true;
                            } else {
                                open(pendingDownloadFile);
                            }
                        }
                        if (error) {
                            showStateReady("");
                            DialogTemplate.showErrorDialog(errorTitle, errorHeader, errorText + " " + resultStatusCode);
                        }
                    }
                });
            }
        }.start();
    }

    /**
     * Manages projects
     */
    @FXML
    protected void actionMenuManageProjects() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        ButtonType submitBtnType = new ButtonType(resourceBundle.getString("manageProjectFinish"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType);
        dialog.setTitle(resourceBundle.getString("manageProjectTitle"));
        dialog.setHeaderText(resourceBundle.getString("manageProjectText"));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField newProjectTf = new TextField();
        ComboBox projectCb = new ComboBox();
        ImageView addProjectIv = new ImageView();
        addProjectIv.setCursor(Cursor.HAND);
        addProjectIv.setImage(createImage);
        ImageView removeProjectIv = new ImageView();
        removeProjectIv.setCursor(Cursor.HAND);
        loadProjects(projectCb);
        setProjectRemoveImage(projectCb, removeProjectIv);
        projectCb.setPrefWidth(300);

        addProjectIv.setOnMouseClicked(event -> {
            if (newProjectTf.getText().matches("^[a-zA-Z0-9]+$") && newProjectTf.getText().length() <= 30 && !projectCb.getItems().contains(newProjectTf.getText())) {
                String projectName = newProjectTf.getText();
                newProjectTf.setText("");
                FileManager.createRemoteProject(settingsWebserviceUrl + projectName, settingsAuthToken);
                loadProjects(projectCb);
                setProjectRemoveImage(projectCb, removeProjectIv);
                if (projectCb.getItems().contains(projectName)) {
                    projectCb.getSelectionModel().select(projectName);
                }
            } else {
                DialogTemplate.showErrorDialog(resourceBundle.getString("manageProjectErrorShort"), resourceBundle.getString("manageProjectError"), resourceBundle.getString("manageProjectErrorText"));
            }
        });

        removeProjectIv.setOnMouseClicked(event -> {
            FileManager.deleteRemoteProject(settingsWebserviceUrl + projectCb.getSelectionModel().getSelectedItem(), settingsAuthToken);
            loadProjects(projectCb);
            setProjectRemoveImage(projectCb, removeProjectIv);
        });

        grid.add(newProjectTf, 0, 0);
        grid.add(addProjectIv, 1, 0);
        grid.add(projectCb, 0, 1);
        grid.add(removeProjectIv, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ((Stage) dialog.getDialogPane().getScene().getWindow()).setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                newProjectTf.requestFocus();
                newProjectTf.deselect();
            }
        });

        dialog.showAndWait();
        if (!projectCb.getItems().contains(settingsProjectName)) {
            DialogTemplate.showInfoDialog(resourceBundle.getString("manageProjectRemovedProjectInfoShort"), resourceBundle.getString("manageProjectRemovedProjectInfo"), resourceBundle.getString("manageProjectRemovedProjectInfoText"));
            actionMenuSettings();
        }
    }

    private void setProjectRemoveImage(ComboBox projectCb, ImageView removeProjectIv) {
        if (projectCb.getItems().size() == 0 || (projectCb.getItems().size() == 1 && projectCb.getItems().get(0).equals(DEFAULT_PROJECT_NAME))) {
            removeProjectIv.setImage(removeImageDisabled);
            removeProjectIv.setDisable(true);
        } else {
            removeProjectIv.setImage(removeImage);
            removeProjectIv.setDisable(false);
        }
        projectCb.getSelectionModel().select(0);
    }

    /**
     * Calls the settings menu
     */
    @FXML
    protected void actionMenuSettings() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        ButtonType submitBtnType = new ButtonType(resourceBundle.getString("settingsSave"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType, ButtonType.CANCEL);
        Node submitBtn = dialog.getDialogPane().lookupButton(submitBtnType);
        dialog.setTitle(resourceBundle.getString("settingsTitle"));
        dialog.setHeaderText(resourceBundle.getString("settingsText"));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField serverTf = new TextField();
        TextField tokenTf = new TextField();
        ComboBox projectCb = new ComboBox();
        loadProjects(projectCb);
        serverTf.setText(settingsWebserviceUrl);
        tokenTf.setText(settingsAuthToken);
        if (projectCb.getItems().contains(settingsProjectName)) {
            projectCb.getSelectionModel().select(settingsProjectName);
        } else {
            projectCb.getSelectionModel().select(0);
            settingsProjectName = (String) projectCb.getItems().get(0);
            saveSettings();
        }
        projectCb.setPrefWidth(450);
        grid.add(new Label(resourceBundle.getString("settingsServerUrl")), 0, 0);
        grid.add(serverTf, 1, 0);
        grid.add(new Label(resourceBundle.getString("settingsToken")), 0, 1);
        grid.add(tokenTf, 1, 1);
        grid.add(new Label(resourceBundle.getString("settingsProject")), 0, 2);
        grid.add(projectCb, 1, 2);
        serverTf.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                new URL(serverTf.getText());
                submitBtn.setDisable(false);
            } catch (MalformedURLException malformedURLException) {
                submitBtn.setDisable(true);
            }
        });

        serverTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean lastFocus, Boolean newFocus) {
                if (!newFocus) {
                    loadProjects(projectCb, serverTf.getText(), tokenTf.getText());
                    projectCb.getSelectionModel().select(0);
                }
            }
        });

        tokenTf.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean lastFocus, Boolean newFocus) {
                if (!newFocus) {
                    loadProjects(projectCb, serverTf.getText(), tokenTf.getText());
                    projectCb.getSelectionModel().select(0);
                }
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitBtnType) {
                List<String> settings = new ArrayList<String>();
                settings.add(serverTf.getText());
                settings.add(tokenTf.getText());
                settings.add((String) projectCb.getSelectionModel().getSelectedItem());
                return settings;
            }
            return null;
        });

        ((Stage) dialog.getDialogPane().getScene().getWindow()).setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                serverTf.requestFocus();
                serverTf.deselect();
            }
        });

        Optional<List<String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            List<String> settings = result.get();
            settingsWebserviceUrl = settings.get(0);
            settingsAuthToken = settings.get(1);
            settingsProjectName = settings.get(2);
            saveSettings();
            loadSettings();
        }
    }

    private void loadProjects(ComboBox projectCb) {
        loadProjects(projectCb, settingsWebserviceUrl, settingsAuthToken);
    }

    private void loadProjects(ComboBox projectCb, String webserviceUrl, String token) {
        projectCb.getItems().clear();
        List<String> projects = FileManager.getAllProjects(webserviceUrl, token);
        if (projects != null) {
            for (String project : projects) {
                projectCb.getItems().add(project);
            }
        }
        if (projectCb.getItems().size() == 0) {
            projectCb.getItems().add(DEFAULT_PROJECT_NAME);
        }
    }

    private void loadSettings() {
        Properties properties = new Properties();
        try {
            File settingsFile = new File(SETTINGS_FILE);
            InputStream inputStream = new FileInputStream(settingsFile);
            properties.load(inputStream);
            settingsWebserviceUrl = properties.getProperty("WebserviceUrl");
            settingsAuthToken = properties.getProperty("AuthToken");
            settingsProjectName = properties.getProperty("Project");
            DialogTemplate.setInitialDirectory(new File(properties.getProperty("FileChooserDir")));
            inputStream.close();
            if (!settingsWebserviceUrl.endsWith("/")) {
                settingsWebserviceUrl += "/";
            }
        } catch (Exception e) {
            settingsWebserviceUrl = DEFAULT_WEBSERVICE_URL;
            settingsAuthToken = DEFAULT_AUTH_TOKEN;
            settingsProjectName = DEFAULT_PROJECT_NAME;
            saveSettings();
        }
    }

    /**
     * Saves the changed settings to file
     */
    public void saveSettings() {
        try {
            Properties props = new Properties();
            props.setProperty("WebserviceUrl", settingsWebserviceUrl);
            props.setProperty("AuthToken", settingsAuthToken);
            props.setProperty("Project", settingsProjectName);
            String fileChooserDir = "";
            if (DialogTemplate.getInitialDirectory() != null) {
                fileChooserDir = DialogTemplate.getInitialDirectory().getAbsolutePath();
            }
            props.setProperty("FileChooserDir", fileChooserDir);
            File settingsFile = new File(SETTINGS_FILE);
            if (!settingsFile.exists()) {
                settingsFile.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(settingsFile);
            props.store(out, null);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCompleted() {
        String url = settingsWebserviceUrl + settingsProjectName + "/mef/";

        if (pendingUpload) {
            pendingUpload = false;
            syncData(true, url, settingsAuthToken, resourceBundle.getString("mainUploading"), workingFile, resourceBundle.getString("mainUploadFailedShort"), resourceBundle.getString("mainUploadFailed"), resourceBundle.getString("mainUploadFailedText"));
        } else if (pendingDownload) {
            pendingDownload = false;
            String hash = FileManager.calculateHash(pendingDownloadFile);
            if (hash == null) {
                url += "-";
            } else {
                url += hash;
            }
            syncData(false, url, settingsAuthToken, resourceBundle.getString("mainDownloading"), pendingDownloadFile, resourceBundle.getString("mainDownloadFailedShort"), resourceBundle.getString("mainDownloadFailed"), resourceBundle.getString("mainDownloadFailedText"));
        }
    }

    /**
     * Creates a new room model
     */
    @FXML
    protected void actionMenuNew() {
        Dialog<List<Object>> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        ButtonType submitBtnType = new ButtonType(resourceBundle.getString("mainCreateMap"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType, ButtonType.CANCEL);
        Node submitBtn = dialog.getDialogPane().lookupButton(submitBtnType);
        submitBtn.setDisable(true);
        dialog.setTitle(resourceBundle.getString("mainCreateMap"));
        dialog.setHeaderText(resourceBundle.getString("mainCreateMapText"));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField xTf = new TextField();
        TextField yTf = new TextField();
        TextField floors = new TextField();
        floors.setText("1");
        Spinner floorHeight = new Spinner();
        floorHeight.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 10.0, 2.0, 0.5));
        floorHeight.setDisable(true);

        grid.add(new Label(resourceBundle.getString("mainTerrainWidth")), 0, 0);
        grid.add(xTf, 1, 0);
        grid.add(new Label(resourceBundle.getString("mainTerrainLength")), 0, 1);
        grid.add(yTf, 1, 1);
        grid.add(new Label(resourceBundle.getString("mainTerrainNumberOfFloors")), 0, 2);
        grid.add(floors, 1, 2);
        grid.add(new Label(resourceBundle.getString("mainTerrainFloorHeight")), 0, 3);
        grid.add(floorHeight, 1, 3);

        xTf.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNewMapInput(submitBtn, xTf, yTf, floors);
        });
        yTf.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNewMapInput(submitBtn, xTf, yTf, floors);
        });

        floors.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNewMapInput(submitBtn, xTf, yTf, floors);
            if (floors.getText().matches("\\d+") && Integer.valueOf(floors.getText()) > 1) {
                floorHeight.setDisable(false);
            } else {
                floorHeight.setDisable(true);
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitBtnType) {
                List<Object> mapSettings = new ArrayList<Object>();
                mapSettings.add(Integer.parseInt(xTf.getText()));
                mapSettings.add(Integer.parseInt(yTf.getText()));
                mapSettings.add(Integer.parseInt(floors.getText()));
                mapSettings.add((double) floorHeight.getValue());
                return mapSettings;
            }
            return null;
        });

        xTf.requestFocus();
        Optional<List<Object>> result = dialog.showAndWait();

        if (result.isPresent()) {
            showStateBusy(resourceBundle.getString("mainCreatingNew"));
            List<Object> mapSettings = result.get();
            FileManager.getTmpName("");
            File folder = FileManager.getTmpName("files" + File.separator + "new");
            if (folder.list().length == 0) {
                workingDirectory = folder.getAbsolutePath();
                int xMetersValue = (int) mapSettings.get(0);
                int yMetersValue = (int) mapSettings.get(1);
                int floorsValue = (int) mapSettings.get(2);
                double floorHeightValue = (double) mapSettings.get(3);

                new Thread() {
                    public void run() {
                        boolean contentLoaded = true;
                        try {
                            editorContentController.preloadAllContents(workingDirectory);
                        } catch (Exception ex) {
                            contentLoaded = false;
                            ex.printStackTrace();
                        }
                        final Map map = new EditorMap(yMetersValue * Map.SEGMENTS_PER_METER, xMetersValue * Map.SEGMENTS_PER_METER, floorsValue, (int) floorHeightValue * Map.SEGMENTS_PER_METER);
                        final boolean success = contentLoaded;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    try {
                                        editorContentController.open();
                                        roomModelController.createMap(map, xMetersValue, yMetersValue, floorsValue, floorHeightValue);
                                        mainTp.setVisible(true);
                                        menuSaveMi.setDisable(false);
                                        menuSaveAsMi.setDisable(false);
                                        menuZoomIn.setDisable(false);
                                        menuZoomOut.setDisable(false);
                                        menuUpload.setDisable(false);
                                        setWorkingFile(null);
                                        showStateReady(resourceBundle.getString("mainNewCreated"));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        showCreatingError();
                                    }
                                } else {
                                    showCreatingError();
                                }
                            }
                        });
                    }
                }.start();
            }
        }
    }

    private void validateNewMapInput(Node submitBtn, TextField xTf, TextField yTf, TextField floors) {
        boolean disable = !(xTf.getText().matches("\\d+") && yTf.getText().matches("\\d+") && floors.getText().matches("\\d+"));
        if (!disable) {
            disable = !(Integer.parseInt(xTf.getText()) > 0 && Integer.parseInt(yTf.getText()) > 0 && Integer.parseInt(floors.getText()) > 0 &&
                    Integer.parseInt(xTf.getText()) <= 1000 && Integer.parseInt(yTf.getText()) <= 1000 && Integer.parseInt(floors.getText()) <= 100 &&
                    (Integer.parseInt(xTf.getText()) * Integer.parseInt(yTf.getText()) * Integer.parseInt(floors.getText()) <= RoomModelController.MAX_SEGMENT_METERS));
        }
        submitBtn.setDisable(disable);
    }

    /**
     * Opens a room model file with user interaction
     */
    @FXML
    protected void actionMenuOpen() {
        File selectedFile = DialogTemplate.showFileOpenDialog(resourceBundle.getString("mainOpenTitle"), (Stage) rootGridPane.getScene().getWindow());
        if (selectedFile != null) {
            open(selectedFile);
        }
    }

    /**
     * Opens a room model file
     *
     * @param file room model file
     */
    public void open(File file) {
        showStateBusy(resourceBundle.getString("mainOpening"));
        String filename = file.getName();
        String extension = "";
        int i = file.getName().lastIndexOf('.');
        if (i >= 0) {
            filename = file.getName().toLowerCase().substring(0, i);
            extension = file.getName().toLowerCase().substring(i + 1);
        }

        if (FileManager.MODEL_EDITOR_FILE_EXTENSION.equals(extension)) {
            FileManager.getTmpName("");
            File folder = FileManager.getTmpName("files" + File.separator + filename);
            if (folder != null) {
                workingDirectory = folder.getAbsolutePath();
                new Thread() {
                    public void run() {
                        boolean loadingSuccess = FileManager.decompress(file, new File(workingDirectory)) != null;
                        Map createdMap = null;
                        try {
                            editorContentController.preloadAllContents(workingDirectory);
                            createdMap = roomModelController.getRoomModelPersistence().load(workingDirectory + File.separator + "roomModel.rm", roomModelController.getMaterials());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            loadingSuccess = false;
                        }
                        final Map map = createdMap;
                        final boolean success = loadingSuccess;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    setWorkingFile(file);
                                    try {
                                        editorContentController.open();
                                        roomModelController.open(map, workingDirectory);
                                        mainTp.setVisible(true);
                                        menuSaveMi.setDisable(false);
                                        menuSaveAsMi.setDisable(false);
                                        menuZoomIn.setDisable(false);
                                        menuZoomOut.setDisable(false);
                                        menuUpload.setDisable(false);
                                        showStateReady(resourceBundle.getString("mainOpened"));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        showOpeningError();
                                    }
                                } else {
                                    showOpeningError();
                                }
                            }
                        });
                    }
                }.start();
            } else {
                showOpeningError();
            }
        } else {
            showOpeningError();
        }
    }

    private void showOpeningError() {
        showStateReady("");
        DialogTemplate.showErrorDialog(resourceBundle.getString("mainOpenFailedShort"), resourceBundle.getString("mainOpenFailed"), resourceBundle.getString("mainOpenFailedText"));
    }

    private void showCreatingError() {
        showStateReady("");
        DialogTemplate.showErrorDialog(resourceBundle.getString("mainNewFailedShort"), resourceBundle.getString("mainNewFailed"), resourceBundle.getString("mainNewFailedText"));
    }

    /**
     * Saves current room model file
     */
    @FXML
    protected void actionMenuSave() {
        savingQueueActive = true;
        if (!saving) {
            savingQueueActive = false;
            if (workingFile != null) {
                showStateBusy(resourceBundle.getString("mainSaving"));
                new Thread() {
                    public void run() {
                        saving = true;
                        roomModelController.save(workingDirectory);
                        editorContentController.save(workingDirectory);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                showStateReady(resourceBundle.getString("mainSaving"));
                            }
                        });

                        final boolean success = FileManager.compress(workingFile, new File(workingDirectory)) != null;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                saving = false;
                                if (success) {
                                    showStateReady(resourceBundle.getString("mainSaved"));
                                    if (savingQueueActive) {
                                        actionMenuSave();
                                    } else {
                                        saveCompleted();
                                    }
                                } else {
                                    showStateReady("");
                                    DialogTemplate.showErrorDialog(resourceBundle.getString("mainSaveFailedShort"), resourceBundle.getString("mainSaveFailed"), resourceBundle.getString("mainSaveFailedText"));
                                    pendingUpload = false;
                                    pendingDownload = false;
                                }
                            }
                        });
                    }
                }.start();
            } else {
                actionMenuSaveAs();
            }
        }
//        else {
//            DialogTemplate.showErrorDialog(resourceBundle.getString("mainSaveActiveTitle"), resourceBundle.getString("mainSaveActive"), resourceBundle.getString("mainSaveActiveText"));
//        }
    }

    /**
     * Saves the current room model with a different name
     */
    @FXML
    protected void actionMenuSaveAs() {
        File selectedFile = DialogTemplate.showFileSaveDialog(resourceBundle.getString("mainSaveTitle"), (Stage) rootGridPane.getScene().getWindow());
        if (selectedFile != null) {
            setWorkingFile(selectedFile);
            actionMenuSave();
        }
    }

    private static void copyFolder(File src, File dest) {
        if (!dest.exists()) {
            dest.mkdir();
        }
        if (src.listFiles() != null && src.listFiles().length > 0) {
            for (File file : src.listFiles()) {
                File fileDest = new File(dest, file.getName());
                if (file.isDirectory()) {
                    copyFolder(file, fileDest);
                } else {
                    if (!fileDest.exists()) {
                        try {
                            Files.copy(file.toPath(), fileDest.toPath());
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the current working directory
     *
     * @param workingFile working directory
     */
    public void setWorkingFile(File workingFile) {
        this.workingFile = workingFile;
        String fileNameText = resourceBundle.getString("mainFileNameDefaultText");
        if (workingFile != null) {
            fileNameText = workingFile.getAbsolutePath();
        }
        try {
            Stage parentStage = (Stage) rootGridPane.getScene().getWindow();
            parentStage.setTitle(resourceBundle.getString("mainAppName") + ": " + fileNameText);
        } catch (Exception ex) {
        }
    }

    private void showStateBusy(String message) {
        loadingTextLb.setText(message);
        loadingVb.setVisible(true);
        mainTp.setDisable(true);
        mainMb.setDisable(true);
        statusLabel.setText(message);
        statusLabel.getStyleClass().add("status-important");
        //rootGridPane.setCursor(Cursor.WAIT);
    }

    private void showStateReady(String message) {
        statusLabel.setText(message);
        if ("".equals(message) || resourceBundle.getString("mainSaved").equals(message) || resourceBundle.getString("mainOpened").equals(message)) {
            statusLabel.getStyleClass().clear();
        }
        rootGridPane.setCursor(Cursor.DEFAULT);
        mainTp.setDisable(false);
        mainMb.setDisable(false);
        loadingVb.setVisible(false);
    }

    /**
     * Checks if the saving process is done
     *
     * @return true, if it is still saving, else it is finished
     */
    public boolean isSaving() {
        return saving;
    }

    /**
     * Called when the window size has been changed
     *
     * @param windowWidth  new window width
     * @param windowHeight new window height
     */
    public void windowSizeChanged(double windowWidth, double windowHeight) {
        roomModelController.windowSizeChanged(windowWidth, windowHeight);
    }

    /**
     * Loads the a image from resources
     *
     * @param imgPath path to the image
     * @return image
     */
    public static Image loadImage(String imgPath) {
        try {
            InputStream fis = Controller.class.getResourceAsStream(imgPath);
            if (fis != null) {
                Image image = new Image(fis);
                fis.close();
                return image;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the current working directory
     *
     * @return current working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }
}
