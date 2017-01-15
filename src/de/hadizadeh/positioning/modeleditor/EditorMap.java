package de.hadizadeh.positioning.modeleditor;

import de.hadizadeh.positioning.modeleditor.controller.RoomModelController;
import de.hadizadeh.positioning.modeleditor.model.EditorMapSegment;
import de.hadizadeh.positioning.roommodel.Map;
import de.hadizadeh.positioning.roommodel.model.ContentElement;
import de.hadizadeh.positioning.roommodel.model.MapSegment;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.util.ResourceBundle;

/**
 * Manages a room model map for editing models
 */
public class EditorMap extends Map {
    private Canvas canvas;
    private GraphicsContext gc;
    private int lastDraggedRow = -1;
    private int lastDraggedColumn = -1;
    private Label currentXLb;
    private Label currentYLb;
    private Label currentMaterialLb;
    private Label currentContentLb;
    private ResourceBundle resourceBundle;
    private int startRow;
    private int startColumn;
    private int visibleRows;
    private int visibleColumns;
    private ImageCursor notAllowedCursor;
    private Cursor currentCursor;

    /**
     * Creates the map
     *
     * @param rows        amount of rows
     * @param columns     amount of columns
     * @param floors      amount of floors
     * @param floorHeight height of the floors
     */
    public EditorMap(int rows, int columns, int floors, int floorHeight) {
        super(rows, columns, floors, floorHeight);
        notAllowedCursor = RoomModelController.createCursor(RoomModelController.CURSOR_PATH + "not_available.png");
    }

    private void initCanvas() {
        gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged(event -> {
            int row = startRow + calculateRow(event.getY());
            int column = startColumn + calculateColumn(event.getX());
            if (canvas.getCursor() != notAllowedCursor) {
                currentCursor = canvas.getCursor();
            }
            if (row >= 0 && column >= 0 && (row != lastDraggedRow || column != lastDraggedColumn) && row < rows && column < columns) {
                if (currentCursor != null) {
                    canvas.setCursor(currentCursor);
                }
                selectMapSegment(row, column);
            } else if (!(row >= 0 && column >= 0 && row < rows && column < columns)) {
                canvas.setCursor(notAllowedCursor);
            }
            lastDraggedRow = row;
            lastDraggedColumn = column;
            updateMouseMoved(event);
        });

        canvas.setOnMouseClicked(event -> {
            int row = startRow + calculateRow(event.getY());
            int column = startColumn + calculateColumn(event.getX());
            if ((row != lastDraggedRow || column != lastDraggedColumn) && row >= 0 && column >= 0 && row < rows && column < columns) {
                selectMapSegment(row, column);
            }
            lastDraggedRow = -1;
            lastDraggedColumn = -1;
        });

        canvas.setOnMouseMoved(event -> {
            updateMouseMoved(event);
        });

        canvas.setOnMouseExited(event -> {
            currentXLb.setText("-");
            currentYLb.setText("-");
            currentContentLb.setText("-");
            currentMaterialLb.setText("-");
        });
    }

    /**
     * Updates the visualized data when the mouse moved
     *
     * @param event mouse event
     */
    protected void updateMouseMoved(MouseEvent event) {
        int row = startRow + calculateRow(event.getY());
        int column = startColumn + calculateColumn(event.getX());
        if (canvas.getCursor() != notAllowedCursor) {
            currentCursor = canvas.getCursor();
        }
        if (row >= 0 && column >= 0 && row < rows && column < columns) {
            if (currentCursor != null) {
                canvas.setCursor(currentCursor);
            }
            ContentElement content = mapSegments[currentFloor][row][column].getContent();
            float metersX = (column + 1) / (float) Map.SEGMENTS_PER_METER;
            float metersY = (super.rows - (row)) / (float) Map.SEGMENTS_PER_METER;
            currentXLb.setText(metersX + " " + resourceBundle.getString("roomModelMeters"));
            currentYLb.setText(metersY + " " + resourceBundle.getString("roomModelMeters"));
            if (content != null) {
                String titleText = content.getTitle();
                if (titleText.length() > 16) {
                    titleText = titleText.substring(0, 16) + "...";
                }
                currentContentLb.setText(titleText);
            } else {
                currentContentLb.setText("-");
            }
            if (mapSegments[currentFloor][row][column].getMaterial() != null) {
                currentMaterialLb.setText(mapSegments[currentFloor][row][column].getMaterial().getPresentationName());
            } else {
                currentMaterialLb.setText("-");
            }
        } else if (!(row >= 0 && column >= 0 && row < rows && column < columns)) {
            canvas.setCursor(notAllowedCursor);
        }
    }

    /**
     * Selects the map segment
     *
     * @param row    row to select
     * @param column column to select
     */
    protected void selectMapSegment(int row, int column) {
        if (selectedContent == null) {
            if (selectedMaterial != null || mapSegments[currentFloor][row][column].getContent() == null) {
                mapSegments[currentFloor][row][column].setMaterial(selectedMaterial);
            }
            if (selectedMaterial == null) {
                mapSegments[currentFloor][row][column].setContent(null);
            }
        } else if (selectedContent != null) {
            mapSegments[currentFloor][row][column].setContent(selectedContent);
        }
        render();
    }

    /**
     * Returns the canvas
     *
     * @return canvas
     */
    @Override
    public Object getCanvas() {
        return canvas;
    }

    /**
     * Resizes the map
     */
    @Override
    public void resize() {
        super.resize();
    }

    /**
     * Renders the map
     */
    @Override
    public void render() {
        render(canvas, startRow, startColumn, visibleRows, visibleColumns, currentXLb, currentYLb, currentMaterialLb, currentContentLb, resourceBundle);
    }

    /**
     * Renders the map
     *
     * @param startRow       starting row
     * @param startColumn    starting column
     * @param visibleRows    amount of visible rows
     * @param visibleColumns amount of visible columns
     */
    @Override
    public void render(int startRow, int startColumn, int visibleRows, int visibleColumns) {
        render(canvas, startRow, startColumn, visibleRows, visibleColumns, currentXLb, currentYLb, currentMaterialLb, currentContentLb, resourceBundle);
    }

    /**
     * Renders the map
     *
     * @param mapCanvas         canvas
     * @param startRow          starting row
     * @param startColumn       starting column
     * @param visibleRows       visible rows
     * @param visibleColumns    visible columns
     * @param currentXLb        current x label
     * @param currentYLb        current y label
     * @param currentMaterialLb current material label
     * @param currentContentLb  current content label
     * @param resourceBundle    resource bundle
     */
    @Override
    public void render(Object mapCanvas, int startRow, int startColumn, int visibleRows, int visibleColumns, Object currentXLb, Object currentYLb, Object currentMaterialLb, Object currentContentLb, Object resourceBundle) {
        if (startRow < 0) {
            startRow = 0;
        }
        if (startColumn < 0) {
            startColumn = 0;
        }
        this.currentXLb = (Label) currentXLb;
        this.currentYLb = (Label) currentYLb;
        this.currentMaterialLb = (Label) currentMaterialLb;
        this.currentContentLb = (Label) currentContentLb;
        this.resourceBundle = (ResourceBundle) resourceBundle;
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.visibleRows = visibleRows;
        this.visibleColumns = visibleColumns;
        if (this.canvas == null) {
            this.canvas = (Canvas) mapCanvas;
            if (this.canvas != null) {
                initCanvas();
            }
        }
        if (this.canvas != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            render(gc, startRow, startColumn, visibleRows, visibleColumns);
        }
    }

    /**
     * Creates a map segment
     *
     * @return created map segment
     */
    @Override
    public MapSegment createMapSegment() {
        return new EditorMapSegment();
    }

    /**
     * Copies a map segment from an existing one
     *
     * @param mapSegment map segment to copy
     * @return copied new map segment
     */
    @Override
    public MapSegment copyMapSegment(MapSegment mapSegment) {
        return new EditorMapSegment(mapSegment);
    }
}
