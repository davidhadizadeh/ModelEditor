package de.hadizadeh.positioning.modeleditor;


import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.modeleditor.controller.EditorContentController;
import de.hadizadeh.positioning.roommodel.Map;
import de.hadizadeh.positioning.roommodel.RoomModelPersistence;
import de.hadizadeh.positioning.roommodel.model.ContentElement;

/**
 * Persistence class for the room model files
 */
public class EditorRoomModelPersistence extends RoomModelPersistence {
    private EditorContentController editorContentController;

    /**
     * Creates the persistence manager
     *
     * @param editorContentController content controller
     */
    public EditorRoomModelPersistence(EditorContentController editorContentController) {
        this.editorContentController = editorContentController;
    }

    /**
     * Removes all positions
     */
    @Override
    protected void removeAllPositions() {
        editorContentController.removeAllPositions();
    }

    /**
     * Adds a position to a content (connects)
     *
     * @param content      content element
     * @param mappingPoint mapping point
     */
    @Override
    protected void addPosition(ContentElement content, MappingPoint mappingPoint) {
        editorContentController.addPosition(content, mappingPoint);
    }

    /**
     * Returns the content which is placed at a position
     *
     * @param mappingPoint position
     * @return connected content
     */
    @Override
    protected ContentElement getContent(MappingPoint mappingPoint) {
        return editorContentController.getContent(mappingPoint);
    }

    /**
     * Creates a room model map
     *
     * @param rows        amount of rows
     * @param columns     amount of columns
     * @param floors      amount of floors
     * @param floorHeight floor height
     * @return created map
     */
    @Override
    protected Map createMap(int rows, int columns, int floors, int floorHeight) {
        return new EditorMap(rows, columns, floors, floorHeight);
    }
}
