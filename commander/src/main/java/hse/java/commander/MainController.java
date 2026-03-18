package hse.java.commander;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainController {

    @FXML private ListView<Path> leftPane;
    @FXML private ListView<Path> rightPane;
    @FXML private Label leftPathLabel;
    @FXML private Label rightPathLabel;
    @FXML private Button btnCopy;
    @FXML private Button btnMove;
    @FXML private Button btnDelete;

    private Path leftDir;
    private Path rightDir;
    private ListView<Path> focusedPane;

    public void initialize() {
        if (leftDir == null) leftDir = Paths.get(System.getProperty("user.home"));
        if (rightDir == null) rightDir = Paths.get(System.getProperty("user.home"));

        Callback<ListView<Path>, ListCell<Path>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.getFileName() == null) setText(item.toString());
                    else setText(item.getFileName().toString());
                    if (Files.isDirectory(item)) setStyle("-fx-font-weight: bold;");
                    else setStyle("");
                }
            }
        };

        leftPane.setCellFactory(cellFactory);
        rightPane.setCellFactory(cellFactory);

        setupPaneInteraction(leftPane, true);
        setupPaneInteraction(rightPane, false);

        btnCopy.setOnAction(e -> transferSelected(TransferOp.COPY));
        btnMove.setOnAction(e -> transferSelected(TransferOp.MOVE));
        btnDelete.setOnAction(e -> transferSelected(TransferOp.DELETE));

        refreshBoth();
    }

    public void setInitialDirs(Path leftStart, Path rightStart) {
        if (leftStart != null) leftDir = leftStart;
        if (rightStart != null) rightDir = rightStart;
        if (leftPane != null && rightPane != null) refreshBoth();
    }

    private void setupPaneInteraction(ListView<Path> pane, boolean isLeft) {
        pane.setOnMouseClicked(evt -> {
            focusedPane = pane;
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {
                Path sel = pane.getSelectionModel().getSelectedItem();
                if (sel == null) return;

                Path current = isLeft ? leftDir : rightDir;

                if (sel.equals(current.getParent())) {
                    if (current.getParent() != null) {
                        if (isLeft) leftDir = current.getParent();
                        else rightDir = current.getParent();
                        refreshBoth();
                    }
                    return;
                }

                if (Files.isDirectory(sel)) {
                    if (isLeft) leftDir = sel;
                    else rightDir = sel;
                    refreshBoth();
                }
            }
        });
    }

    private enum TransferOp { COPY, MOVE, DELETE }

    private void transferSelected(TransferOp op) {
        if (focusedPane == null) return;
        Path selected = focusedPane.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Path sourceDir = (focusedPane == leftPane) ? leftDir : rightDir;
        Path targetDir = (focusedPane == leftPane) ? rightDir : leftDir;

        if (selected.equals(sourceDir.getParent())) return;

        Path src = sourceDir.resolve(selected.getFileName());

        if (op == TransferOp.DELETE) {
            try {
                Files.deleteIfExists(src);
            } catch (IOException ignored) {}
            refreshBoth();
            return;
        }

        Path dst = targetDir.resolve(selected.getFileName());

        try {
            if (op == TransferOp.COPY) {
                if (Files.isDirectory(src)) {
                    if (!Files.exists(dst)) Files.createDirectories(dst);
                } else {
                    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                }
            } else if (op == TransferOp.MOVE) {
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {}

        refreshBoth();
    }

    private void refreshBoth() {
        safeRefresh(leftPane, leftDir, leftPathLabel);
        safeRefresh(rightPane, rightDir, rightPathLabel);
    }

    private void safeRefresh(ListView<Path> pane, Path dir, Label label) {
        if (dir == null) {
            pane.setItems(FXCollections.observableArrayList());
            if (label != null) label.setText("");
            return;
        }

        ObservableList<Path> items = FXCollections.observableArrayList();
        Path parent = dir.getParent();
        if (parent != null) items.add(parent);

        try (Stream<Path> stream = Files.list(dir)) {
            var sorted = stream
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList());
            items.addAll(sorted);
        } catch (IOException ignored) {}

        pane.setItems(items);
        if (label != null) label.setText(dir.toString());
    }
}
