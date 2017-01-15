package de.hadizadeh.positioning.modeleditor.controller;


import de.hadizadeh.positioning.content.exceptions.ContentPersistenceException;
import de.hadizadeh.positioning.modeleditor.ContentListVisualiser;
import de.hadizadeh.positioning.modeleditor.DialogTemplate;
import de.hadizadeh.positioning.modeleditor.EditorRoomModelPersistence;
import de.hadizadeh.positioning.modeleditor.model.EditorMaterial;
import de.hadizadeh.positioning.roommodel.Map;
import de.hadizadeh.positioning.roommodel.RoomModelPersistence;
import de.hadizadeh.positioning.roommodel.model.ContentElement;
import de.hadizadeh.positioning.roommodel.model.MapSegment;
import de.hadizadeh.positioning.roommodel.model.Material;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the room model
 */
public class RoomModelController {
    /**
     * Maximum room model size
     */
    public static final long MAX_SEGMENT_METERS = 1000 * 1000 * 10;
    /**
     * Path to the cursor textures
     */
    public static final String CURSOR_PATH = "/res/img/cursors/";

    @FXML
    Canvas mapCanvas;
    @FXML
    Pane mapBoxPn;
    @FXML
    ScrollBar mapBoxHorizontalSb;
    @FXML
    ScrollBar mapBoxVerticalSb;
    @FXML
    HBox materialHb;
    @FXML
    Label currentXLb;
    @FXML
    Label currentYLb;
    @FXML
    Label currentMaterialLb;
    @FXML
    Label currentContentLb;
    @FXML
    Spinner mapChangeNumberSp;
    @FXML
    AnchorPane floorCbAp;
    @FXML
    ComboBox mapChangePositionCb;
    @FXML
    AnchorPane contentConnectCbAp;
    @FXML
    Spinner floorHeightSp;

    @FXML
    ImageView addFloorIv;
    @FXML
    ImageView copyFloorIv;
    @FXML
    ImageView removeFloorIv;
    @FXML
    ImageView addMapSegmentsIv;
    @FXML
    ImageView removeMapSegmentsIv;

    private EditorContentController editorContentController;
    private List<Material> materials;
    private Map map;
    private RoomModelPersistence roomModelPersistence;
    private ToggleGroup functionButtonGroup;
    private ToggleButton selectedMaterialButton;
    private ToggleButton eraserButton;
    protected ImageCursor eraserCursor;
    protected ImageCursor materialCursor;
    protected ImageCursor contentCursor;
    private Label statusLabel;
    private ResourceBundle resourceBundle;
    private ComboBox floorCb;
    private ComboBox contentConnectCb;

    /**
     * Returns the room model persistence manager
     *
     * @return room model persistence
     */
    public RoomModelPersistence getRoomModelPersistence() {
        return roomModelPersistence;
    }

    /**
     * Returns the available materials
     *
     * @return materials
     */
    public List<Material> getMaterials() {
        return materials;
    }

    /**
     * Sets the editor content controller
     *
     * @param editorContentController editor content controller
     */
    public void setEditorContentController(EditorContentController editorContentController) {
        this.editorContentController = editorContentController;
    }

    /**
     * Loads the needed resources
     *
     * @param resourceBundle resource bundle
     * @param statusLabel    status label
     */
    public void load(ResourceBundle resourceBundle, Label statusLabel) {
        this.resourceBundle = resourceBundle;
        this.statusLabel = statusLabel;
        eraserCursor = createCursor(CURSOR_PATH + "eraser.png");
        materialCursor = createCursor(CURSOR_PATH + "material.png");
        contentCursor = createCursor(CURSOR_PATH + "content.png");

        roomModelPersistence = new EditorRoomModelPersistence(editorContentController);
        materials = new ArrayList<>();
        String[][] materialValues = Material.getDefaultMaterialValues();
        String[] materialNames = new String[]{resourceBundle.getString("roomModelMaterialEraser"), resourceBundle.getString("roomModelMaterialWall"),
                resourceBundle.getString("roomModelMaterialFurniture"), resourceBundle.getString("roomModelMaterialWindow"),
                resourceBundle.getString("roomModelMaterialDoor"), resourceBundle.getString("roomModelMaterialStairs"),
                resourceBundle.getString("roomModelMaterialElevator"), resourceBundle.getString("roomModelMaterialEscalator")};

        for (int i = 0; i < materialValues.length; i++) {
            addMaterial(materialValues[i], materialNames[i]);
        }
        addMaterialPickers();
        if (mapCanvas != null) {
            mapCanvas.widthProperty().bind(mapBoxPn.widthProperty().subtract(2));
            mapCanvas.heightProperty().bind(mapBoxPn.heightProperty().subtract(2));

            mapCanvas.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    if (event.isControlDown()) {
                        resizeMap(event.getDeltaY() / 10);
                    } else {
                        if (mapBoxVerticalSb.isVisible()) {
                            double change = mapBoxVerticalSb.getMax() * calculateVisibleRows() / map.getRows();
                            if (event.getDeltaY() > 0) {
                                change *= -1;
                            }
                            double value = mapBoxVerticalSb.getValue() + change;
                            if (value < mapBoxVerticalSb.getMin()) {
                                mapBoxVerticalSb.setValue(mapBoxVerticalSb.getMin());
                            } else if (value > mapBoxVerticalSb.getMax()) {
                                mapBoxVerticalSb.setValue(mapBoxVerticalSb.getMax());
                            } else {
                                mapBoxVerticalSb.setValue(value);
                            }
                        }
                    }
                }
            });
        }
        if (mapChangePositionCb != null) {
            mapChangePositionCb.getItems().clear();
            mapChangePositionCb.getItems().add(resourceBundle.getString("mapChangeTop"));
            mapChangePositionCb.getItems().add(resourceBundle.getString("mapChangeBottom"));
            mapChangePositionCb.getItems().add(resourceBundle.getString("mapChangeLeft"));
            mapChangePositionCb.getItems().add(resourceBundle.getString("mapChangeRight"));
            mapChangePositionCb.getSelectionModel().select(Map.Position.BOTTOM.ordinal());

            floorHeightSp.valueProperty().addListener(new ChangeListener<Double>() {
                @Override
                public void changed(ObservableValue ov, Double oldValue, Double newValue) {
                    if (map != null) {
                        map.setFloorHeight((int) (newValue * Map.SEGMENTS_PER_METER));
                    }
                }
            });
            Tooltip.install(addFloorIv, new Tooltip(resourceBundle.getString("roomModelAddFloor")));
            Tooltip.install(copyFloorIv, new Tooltip(resourceBundle.getString("roomModelCopyFloor")));
            Tooltip.install(removeFloorIv, new Tooltip(resourceBundle.getString("roomModelRemoveFloor")));
            Tooltip.install(addMapSegmentsIv, new Tooltip(resourceBundle.getString("roomModelAddMapSegments")));
            Tooltip.install(removeMapSegmentsIv, new Tooltip(resourceBundle.getString("roomModelRemoveMapSegments")));
        }
    }

    private void addMaterial(String[] materialValues, String name) {
        materials.add(new EditorMaterial(materialValues[0], name, materialValues[1], materialValues[2]));
    }

    /**
     * Resizes the map
     *
     * @param delta scaling factor
     */
    public void resizeMap(double delta) {
        MapSegment.setSize(MapSegment.getSize() + delta);
        refreshCanvasScrollbars();
        int visibleRows = calculateVisibleRows(getCanvaHeight());
        int visibleColumns = calculateVisibleColumns(getCanvasWidth());
        if(mapBoxVerticalSb != null) {
            map.render((int) mapBoxVerticalSb.getValue(), (int) mapBoxHorizontalSb.getValue(), visibleRows, visibleColumns);
        }
    }

    /**
     * Clears the status text
     */
    @FXML
    protected void actionStatusClear() {
        setContextStatus("");
    }

    /**
     * Adds a floor
     */
    @FXML
    protected void actionAddFloor() {
        if (calculateMapSegmentsPerFloor() + calculateMapSegments() <= MAX_SEGMENT_METERS) {
            map.addFloor();
            loadFloors(map.getFloors());
            floorCb.getSelectionModel().selectLast();
        } else {
            DialogTemplate.showErrorDialog(resourceBundle.getString("roomModelMaximumSizeErrorShort"), resourceBundle.getString("roomModelMaximumSizeError"), resourceBundle.getString("roomModelMaximumSizeErrorText"));
        }
    }

    /**
     * Copies all data of a floor to a new one
     */
    @FXML
    protected void actionCopyFloor() {
        if (calculateMapSegmentsPerFloor() + calculateMapSegments() <= MAX_SEGMENT_METERS) {
            map.addFloor(floorCb.getSelectionModel().getSelectedIndex());
            loadFloors(map.getFloors());
            floorCb.getSelectionModel().selectLast();
        } else {
            DialogTemplate.showErrorDialog(resourceBundle.getString("roomModelMaximumSizeErrorShort"), resourceBundle.getString("roomModelMaximumSizeError"), resourceBundle.getString("roomModelMaximumSizeErrorText"));
        }
    }

    /**
     * Removes a floor
     */
    @FXML
    protected void actionRemoveFloor() {
        if (map.getFloors() > 1) {
            if (DialogTemplate.showYesNoDialog(resourceBundle.getString("dialogConfirmation"), resourceBundle.getString("roomModelRemoveFloor"), resourceBundle.getString("roomModelRemoveFloorConfirmation"))) {
                int floorIndex = floorCb.getSelectionModel().getSelectedIndex();
                map.removeFloor(floorIndex);
                loadFloors(map.getFloors());
                if (floorIndex < map.getFloors()) {
                    floorCb.getSelectionModel().select(floorIndex);
                } else {
                    floorCb.getSelectionModel().select(floorIndex - 1);
                }
                if (map.getFloors() < 2) {
                    floorHeightSp.getValueFactory().setValue(2.0);
                }
            }
        } else {
            DialogTemplate.showErrorDialog(resourceBundle.getString("roomModelRemoveFloorErrorShort"), resourceBundle.getString("roomModelRemoveFloorError"), resourceBundle.getString("roomModelRemoveFloorErrorText"));
        }
    }

    private void contentSelectionChanged() {
        int index = contentConnectCb.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            ContentElement contentElement = editorContentController.getContentElement(index);
            map.setSelectedContent(contentElement);
            map.setSelectedMaterial(null);
            getCanvas().setCursor(contentCursor);
            if (selectedMaterialButton != null) {
                selectedMaterialButton.setSelected(false);
            }
        } else {
            map.setSelectedContent(null);
            getCanvas().setCursor(eraserCursor);
        }
    }

    /**
     * Adds the map segments
     */
    @FXML
    protected void actionAddMapSegments() {
        int position = mapChangePositionCb.getSelectionModel().getSelectedIndex();
        int changeNumber = (int) mapChangeNumberSp.getValue();
        long addition = changeNumber;
        if (position < Map.Position.LEFT.ordinal()) {
            addition *= map.getColumns() / Map.SEGMENTS_PER_METER;
        } else {
            addition *= map.getRows() / Map.SEGMENTS_PER_METER;
        }

        if (addition + calculateMapSegments() <= MAX_SEGMENT_METERS) {
            map.addMapSegments(changeNumber * Map.SEGMENTS_PER_METER, position);
            refreshCanvasScrollbars();
            map.render();
        } else {
            DialogTemplate.showErrorDialog(resourceBundle.getString("roomModelMaximumSizeErrorShort"), resourceBundle.getString("roomModelMaximumSizeError"), resourceBundle.getString("roomModelMaximumSizeErrorText"));
        }
    }

    private long calculateMapSegments() {
        return calculateMapSegmentsPerFloor() * map.getFloors();
    }

    private long calculateMapSegmentsPerFloor() {
        return (map.getRows() / Map.SEGMENTS_PER_METER) * (map.getColumns() / Map.SEGMENTS_PER_METER);
    }

    /**
     * Sets the status bar text to the add map segments text
     */
    @FXML
    protected void actionStatusAddMapSegments() {
        setContextStatus(resourceBundle.getString("roomModelAddMapSegments"));
    }

    /**
     * Removes map segments
     */
    @FXML
    protected void actionRemoveMapSegments() {
        boolean success = map.removeMapSegments((int) mapChangeNumberSp.getValue() * Map.SEGMENTS_PER_METER, mapChangePositionCb.getSelectionModel().getSelectedIndex());
        if (success) {
            refreshCanvasScrollbars();
            map.render();
        } else {
            DialogTemplate.showErrorDialog(resourceBundle.getString("roomModelChangeFailedShort"), resourceBundle.getString("roomModelChangeFailed"), resourceBundle.getString("roomModelChangeFailedText"));
        }
    }

    /**
     * Sets the status bar text to the remove map segments text
     */
    @FXML
    protected void actionStatusRemoveMapSegments() {
        setContextStatus(resourceBundle.getString("roomModelRemoveMapSegments"));
    }

    /**
     * Sets the status bar text to the add floors text
     */
    @FXML
    protected void actionStatusAddFloor() {
        setContextStatus(resourceBundle.getString("roomModelAddFloor"));
    }

    /**
     * Sets the status bar text to the copy floors text
     */
    @FXML
    protected void actionStatusCopyFloor() {
        setContextStatus(resourceBundle.getString("roomModelCopyFloor"));
    }

    /**
     * Sets the status bar text to the remove floor text
     */
    @FXML
    protected void actionStatusRemoveFloor() {
        setContextStatus(resourceBundle.getString("roomModelRemoveFloor"));
    }

    private void addMaterialPickers() {
        if (materialHb != null) {
            materialHb.setSpacing(10.0);
            functionButtonGroup = new ToggleGroup();
            for (Material material : materials) {
                ToggleButton button = new ToggleButton();
                button.setText(material.getPresentationName());
                button.setAlignment(Pos.BASELINE_LEFT);
                button.setToggleGroup(functionButtonGroup);


                if (material.getTexture() != null) {
                    ImageView imageView = new ImageView((Image) material.getTexture());
                    imageView.setFitWidth(40);
                    imageView.setFitHeight(40);
                    button.setGraphic(imageView);
                    button.setStyle("-fx-pref-width: 150;");
                } else {
                    button.setStyle("-fx-text-fill: " + material.getTextColor() + "; -fx-background-color: " + material.getColor() + "; -fx-pref-width: 150;");
                }

                if ("eraser".equals(material.getName())) {
                    eraserButton = button;
                    button.setOnMouseClicked(event -> {
                        selectEraser();
                    });

                } else {
                    button.setOnMouseClicked(event -> {
                        map.setSelectedMaterial(material);
                        materialClicked(button);
                        getCanvas().setCursor(materialCursor);
                    });
                }
                materialHb.getChildren().add(button);
            }
        }
    }

    /**
     * Selects the eraser
     */
    public void selectEraser() {
        map.setSelectedMaterial(null);
        map.setSelectedContent(null);
        materialClicked(eraserButton);
        Canvas canvas = getCanvas();
        if(canvas != null) {
            canvas.setCursor(eraserCursor);
        }
    }

    private void materialClicked(ToggleButton button) {
        if (contentConnectCb != null) {
            contentConnectCb.getSelectionModel().clearSelection();
            button.setSelected(true);
        }
        selectedMaterialButton = button;
    }

    /**
     * Creates a cursor with texture
     *
     * @param imgPath path to the texture
     * @return cursor
     */
    public static ImageCursor createCursor(String imgPath) {
        Image image = Controller.loadImage(imgPath);
        if (image != null) {
            return new ImageCursor(image, 5, 5);
        }
        return null;
    }

    private Color getContrastColor(String hexColor) {
        return getContrastColor(Color.web(hexColor));
    }

    private Color getContrastColor(Color color) {
        Color contrastColor = Color.rgb(255 - (int) (255 * color.getRed()),
                255 - (int) (255 * color.getGreen()),
                255 - (int) (255 * color.getBlue()));
        return contrastColor;
    }

    private String getWebColor(Color color) {
        return "#" + Integer.toHexString(color.hashCode());
    }

    /**
     * Reloads the content box
     *
     * @param contentListItems cotent items
     */
    public void refreshContentBox(List<ContentListVisualiser.ContentListItem> contentListItems) {
        contentConnectCbAp.getChildren().remove(contentConnectCb);
        contentConnectCb = createDefaultComboBox(contentConnectCbAp);
        contentConnectCb.valueProperty().addListener(new ChangeListener<ContentListVisualiser.ContentListItem>() {
            @Override
            public void changed(ObservableValue ov, ContentListVisualiser.ContentListItem t, ContentListVisualiser.ContentListItem t1) {
                contentSelectionChanged();
            }
        });

        contentConnectCb.setCellFactory(ContentListVisualiser.getContentListCallback());
        contentConnectCb.setButtonCell(new ContentListVisualiser.IconTextCell());
        for (ContentListVisualiser.ContentListItem contentListItem : contentListItems) {
            contentConnectCb.getItems().add(contentListItem);
        }
        if (contentListItems.size() == 0) {
            contentConnectCb.setDisable(true);
        } else {
            contentConnectCb.setDisable(false);
        }
        if (map != null) {
            map.render();
        }
    }

    /**
     * Saves the room model file
     *
     * @param path path to the file
     */
    public void save(String path) {
        try {
            roomModelPersistence.save(path + File.separator + "roomModel.rm", map);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContentPersistenceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a map
     *
     * @param createdMap  reference to the created map
     * @param xMeters     x size in meters
     * @param yMeters     y size in meters
     * @param floors      amount of floors
     * @param floorHeight height of each floor
     */
    public void createMap(Map createdMap, int xMeters, int yMeters, int floors, double floorHeight) {
        //map = new EditorMap(yMeters * SEGMENTS_PER_METER, xMeters * SEGMENTS_PER_METER, floors, (int) floorHeight * SEGMENTS_PER_METER);
        map = createdMap;
        if (mapBoxPn != null) {
            refreshCanvasScrollbars();
            mapBoxHorizontalSb.setValue(mapBoxHorizontalSb.getMin());
            mapBoxVerticalSb.setValue(mapBoxVerticalSb.getMin());
            map.render(mapCanvas, 0, 0, calculateVisibleRows(), calculateVisibleColumns(), currentXLb, currentYLb, currentMaterialLb, currentContentLb, resourceBundle);
            loadFloors(floors);
            floorHeightSp.getValueFactory().setValue(floorHeight);
            eraserButton.setSelected(true);
            selectedMaterialButton = eraserButton;
            getCanvas().setCursor(eraserCursor);
        }
    }

    /**
     * Opens the room model files and creates the map
     *
     * @param createdMap created map
     * @param path       path to the room model file
     * @throws IOException if the file could not be opened
     */
    public void open(Map createdMap, String path) throws IOException {
        //map = roomModelPersistence.load(path + File.separator + "roomModel.rm", materials);
        map = createdMap;
        if (map.getFloors() == 1) {
            map.setFloorHeight(2 * Map.SEGMENTS_PER_METER);
        }
        refreshCanvasScrollbars();
        mapBoxHorizontalSb.setValue(mapBoxHorizontalSb.getMin());
        mapBoxVerticalSb.setValue(mapBoxVerticalSb.getMin());
        map.render(mapCanvas, 0, 0, calculateVisibleRows(), calculateVisibleColumns(), currentXLb, currentYLb, currentMaterialLb, currentContentLb, resourceBundle);
        loadFloors(map.getFloors());
        double floorHeight = (double) map.getFloorHeight() / Map.SEGMENTS_PER_METER;
        floorHeightSp.getValueFactory().setValue(floorHeight);
        eraserButton.setSelected(true);
        selectedMaterialButton = eraserButton;
        getCanvas().setCursor(eraserCursor);
    }

    private void loadFloors(int floors) {
        floorCb = createDefaultComboBox(floorCbAp);
        floorCb.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                int index = floorCb.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    map.setCurrentFloor(index);
                    map.render();
                }
            }
        });
        for (int i = 1; i <= floors; i++) {
            floorCb.getItems().add(i + ". " + resourceBundle.getString("roomModelFloor"));
        }
        floorCb.getSelectionModel().selectFirst();
    }

    private void setContextStatus(String text) {
        if (!statusLabel.getStyleClass().contains("status-important")) {
            statusLabel.setText(text);
        }
    }

    private ComboBox createDefaultComboBox(AnchorPane box) {
        box.getChildren().removeAll();
        ComboBox comboBox = new ComboBox();
        box.getChildren().add(comboBox);
        comboBox.setVisibleRowCount(6);
        AnchorPane.setLeftAnchor(comboBox, 0.0);
        AnchorPane.setRightAnchor(comboBox, 0.0);
        return comboBox;
    }

    /**
     * Returns the canvas
     *
     * @return canvas
     */
    public Canvas getCanvas() {
        return ((Canvas) map.getCanvas());
    }

    /**
     * Called if the window size changed
     *
     * @param windowWidth  with of the window
     * @param windowHeight height of the window
     */
    public void windowSizeChanged(double windowWidth, double windowHeight) {
        if (map != null) {
            refreshCanvasScrollbars(windowWidth, windowHeight);
            map.render();
        }
    }

    private void refreshCanvasScrollbars() {
        if(mapBoxHorizontalSb != null) {
            refreshCanvasScrollbars(-1, -1);
        }
    }

    private void refreshCanvasScrollbars(double windowWidth, double windowHeight) {
        if (map != null) {
            double width = getCanvasWidth();
            double height = getCanvaHeight();

            if (windowWidth >= 0) {
                width = windowWidth - 33;
                height = windowHeight - 350;
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                refreshCanvasScrollbars();
                            }
                        });
                    }
                }.start();
            }
            final int visibleRows = calculateVisibleRows(height);
            final int visibleColumns = calculateVisibleColumns(width);

            mapBoxHorizontalSb.setMin(0);
            mapBoxHorizontalSb.setMax(map.getColumns() - visibleColumns);
            mapBoxHorizontalSb.setVisibleAmount(mapBoxHorizontalSb.getMax() * visibleColumns / map.getColumns());
            mapBoxVerticalSb.setMin(0);
            mapBoxVerticalSb.setMax(map.getRows() - visibleRows);
            mapBoxVerticalSb.setVisibleAmount(mapBoxVerticalSb.getMax() * visibleRows / map.getRows());

            mapBoxHorizontalSb.setVisible(visibleColumns < map.getColumns());
            mapBoxVerticalSb.setVisible(visibleRows < map.getRows());

            if (mapBoxVerticalSb.getValue() > mapBoxVerticalSb.getMax()) {
                mapBoxVerticalSb.setValue(mapBoxVerticalSb.getMax());
            } else if (mapBoxVerticalSb.getValue() < mapBoxVerticalSb.getMin()) {
                mapBoxVerticalSb.setValue(mapBoxVerticalSb.getMin());
            }
            if (mapBoxHorizontalSb.getValue() > mapBoxHorizontalSb.getMax()) {
                mapBoxHorizontalSb.setValue(mapBoxHorizontalSb.getMax());
            } else if (mapBoxHorizontalSb.getValue() < mapBoxHorizontalSb.getMin()) {
                mapBoxHorizontalSb.setValue(mapBoxHorizontalSb.getMin());
            }

            mapBoxHorizontalSb.valueProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov,
                                    Number oldValue, Number newValue) {
                    map.render((int) mapBoxVerticalSb.getValue(), (int) mapBoxHorizontalSb.getValue(), visibleRows, visibleColumns);
                }
            });

            mapBoxVerticalSb.valueProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov,
                                    Number oldValue, Number newValue) {
                    map.render((int) mapBoxVerticalSb.getValue(), (int) mapBoxHorizontalSb.getValue(), visibleRows, visibleColumns);
                }
            });
        }
    }


    private double getCanvasWidth() {
        if(mapBoxPn != null) {
            return mapBoxPn.getWidth() - 2.0;
        }
        return 0.0;
    }

    private double getCanvaHeight() {
        if(mapBoxPn != null) {
            return mapBoxPn.getHeight() - 2.0;
        }
        return 0.0;
    }


    private int calculateVisibleColumns() {
        return calculateVisibleColumns(mapBoxPn.getWidth());
    }

    private int calculateVisibleRows() {
        return calculateVisibleColumns(mapBoxPn.getHeight());
    }


    private int calculateVisibleColumns(double width) {
        return (int) (width / MapSegment.getSize());
    }

    private int calculateVisibleRows(double height) {
        return (int) (height / MapSegment.getSize());
    }
}
