package de.hadizadeh.positioning.modeleditor;


import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * Manages the visualisation of content lists
 */
public class ContentListVisualiser {

    private static HBox getCell(Label title, ImageView imageView) {
        HBox cell = new HBox();
        cell.setAlignment(Pos.CENTER_LEFT);
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        cell.getChildren().add(imageView);
        cell.getChildren().add(title);
        cell.setSpacing(10);
        return cell;
    }

    private static Callback<ListView<ContentListItem>, ListCell<ContentListItem>> contentListCallback = new Callback<ListView<ContentListItem>, ListCell<ContentListItem>>() {
        @Override
        public ListCell<ContentListItem> call(ListView<ContentListItem> p) {
            return new ListCell<ContentListItem>() {
                Label title = new Label();
                ImageView imageView = new ImageView();
                private final HBox cell;

                {
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    cell = getCell(title, imageView);
                }

                @Override
                protected void updateItem(ContentListItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                    } else {
                        String titleText = item.getTitle();
                        if (titleText != null) {
                            if (titleText.length() > 28) {
                                titleText = titleText.substring(0, 28) + "...";
                            }
                        }
                        title.setText(titleText);
                        imageView.setImage(item.getImage());
                        setGraphic(cell);
                    }
                }
            };
        }
    };

    /**
     * Returns the content list callback
     *
     * @return content list callback
     */
    public static Callback<ListView<ContentListItem>, ListCell<ContentListItem>> getContentListCallback() {
        return contentListCallback;
    }

    /**
     * Manages a single content item in the list
     */
    public static class ContentListItem {
        private String title;
        private Image image;

        /**
         * Creates the conten item
         *
         * @param title title
         * @param image image
         */
        public ContentListItem(String title, Image image) {
            this.title = title;
            this.image = image;
        }

        /**
         * Returns the title
         *
         * @return title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the image
         *
         * @return image
         */
        public Image getImage() {
            return image;
        }
    }

    /**
     * Manages a single text cell with an icen
     */
    public static class IconTextCell extends ListCell<ContentListItem> {
        /**
         * Updates the current item
         *
         * @param item  item
         * @param empty true if the item is empty, else it is not empty
         */
        @Override
        protected void updateItem(ContentListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                Label title = new Label();
                ImageView imageView = new ImageView();
                HBox cell = getCell(title, imageView);
                title.setText(item.getTitle());
                imageView.setImage(item.getImage());
                setGraphic(cell);
            }
        }
    }

    ;

}
