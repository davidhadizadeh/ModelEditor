package de.hadizadeh.positioning.modeleditor.model;


import de.hadizadeh.positioning.modeleditor.controller.Controller;
import de.hadizadeh.positioning.roommodel.model.Material;
import javafx.scene.image.Image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Manages materials for the editor
 */
public class EditorMaterial extends Material {
    /**
     * path to the texture files
     */
    public static final String TEXTURES_PATH = "/res/img/textures/";
    /**
     * extension for the textures
     */
    public static final String TEXTURES_EXTENSION = ".png";
    /**
     * Prefix for the texture files
     */
    public static final String TEXTURES_PREFIX = "texture_";

    /**
     * Creates a material
     *
     * @param name material name
     */
    public EditorMaterial(String name) {
        super(name);
    }

    /**
     * Creates a material
     *
     * @param name             material name
     * @param presentationName presentation name (will be shown to the user)
     * @param color            default background color, if there is no texture
     * @param textColor        text color
     */
    public EditorMaterial(String name, String presentationName, String color, String textColor) {
        super(name, presentationName, color, textColor);
    }

    /**
     * Loads the texture from file
     */
    @Override
    protected void loadTexture() {
        try {
            InputStream fis = Controller.class.getResourceAsStream(TEXTURES_PATH + TEXTURES_PREFIX + name + TEXTURES_EXTENSION);
            if (fis != null) {
                texture = new Image(fis);
                fis.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
