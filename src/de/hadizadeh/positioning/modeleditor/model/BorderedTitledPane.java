package de.hadizadeh.positioning.modeleditor.model;


import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Group Box
 */
public class BorderedTitledPane extends StackPane {
    private Label titleLabel = new Label();
    private StackPane contentPane = new StackPane();
    private Node content;

    /**
     * Creates a group box
     */
    public BorderedTitledPane() {
        titleLabel.setText("default title");
        titleLabel.getStyleClass().add("bordered-titled-title");
        StackPane.setAlignment(titleLabel, Pos.TOP_CENTER);

        getStyleClass().add("bordered-titled-border");
        getChildren().addAll(titleLabel, contentPane);
    }

    /**
     * Sets the contents
     *
     * @param content content
     */
    public void setContent(Node content) {
        content.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);
    }

    /**
     * Returns the content
     *
     * @return content
     */
    public Node getContent() {
        return content;
    }

    /**
     * Sets the title
     *
     * @param title titel
     */
    public void setTitle(String title) {
        titleLabel.setText(" " + title + " ");
    }

    /**
     * Returns the title
     *
     * @return title
     */
    public String getTitle() {
        return titleLabel.getText();
    }
}