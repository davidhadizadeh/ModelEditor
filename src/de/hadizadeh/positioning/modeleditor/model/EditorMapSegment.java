package de.hadizadeh.positioning.modeleditor.model;


import de.hadizadeh.positioning.modeleditor.controller.Controller;
import de.hadizadeh.positioning.roommodel.model.MapSegment;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles map segments for the editor
 */
public class EditorMapSegment extends MapSegment {
    private static Image contentTexture;

    public EditorMapSegment() {
        super();
        if (contentTexture == null) {
            loadContentTexture();
        }
    }

    /**
     * Copies a map segment
     *
     * @param copy old map segment
     */
    public EditorMapSegment(MapSegment copy) {
        super(copy);
    }

    /**
     * Renders a map segment
     *
     * @param graphic        graphic for drawing
     * @param originalRow    row number
     * @param originalColumn column number
     * @param renderRow      current row (with scroll position)
     * @param renderColumn   current column (with scroll position)
     */
    @Override
    public void render(Object graphic, int originalRow, int originalColumn, int renderRow, int renderColumn) {
        GraphicsContext gc = (GraphicsContext) graphic;
        double x;
        double y;
        double w;
        double h;

        if (originalRow % 2 == 0) {
            gc.setFill(Color.web(mediumStrokeColor));
            gc.fillRect(renderColumn * size, renderRow * size - strokeLineWidth, size, strokeLineWidth);
        }

        if (originalColumn % 2 == 0) {
            gc.setFill(Color.web(mediumStrokeColor));
            gc.fillRect(renderColumn * size - strokeLineWidth, renderRow * size, strokeLineWidth, size);
        }

//        if (content == null || content.getTitle() == null) {
//            gc.setFill(Color.web(strokeColor));
//        } else {
//            gc.setFill(Color.web(boldStrokeColor));
//        }
        gc.setFill(Color.web(strokeColor));
        gc.fillRect(renderColumn * size, renderRow * size, size, size);

        if (material != null) {
            gc.setFill(Color.web(material.getColor()));
        } else {
            gc.setFill(Color.WHITE);
        }

        double currentStrokeLineWidth = strokeLineWidth;
//        if (content == null || content.getTitle() == null) {
//            currentStrokeLineWidth = strokeLineWidth;
//        } else {
//            currentStrokeLineWidth = boldStrokeLineWidth;
//        }

        x = renderColumn * size + currentStrokeLineWidth;
        y = renderRow * size + currentStrokeLineWidth;
        w = size - 2 * currentStrokeLineWidth;
        h = size - 2 * currentStrokeLineWidth;

        if (material != null && material.getTexture() != null) {
            gc.drawImage((Image) material.getTexture(), x, y, w - 1, h - 1);
        } else {
            gc.fillRect(x, y, w, h);
        }

        if (content != null && content.getTitle() != null) {
            double centerAddition = (size - size / contentSizeFactor) / 2;
            gc.drawImage(contentTexture, x + centerAddition, y + centerAddition, w / contentSizeFactor, h / contentSizeFactor);
        }
    }

    private static void loadContentTexture() {
        try {
            InputStream fis = Controller.class.getResourceAsStream(EditorMaterial.TEXTURES_PATH + EditorMaterial.TEXTURES_PREFIX + "content" + EditorMaterial.TEXTURES_EXTENSION);
            if (fis != null) {
                contentTexture = new Image(fis);
                fis.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
