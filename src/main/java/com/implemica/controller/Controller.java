package com.implemica.controller;

import static com.implemica.model.operation.CalcMemory.MEMORY_ADDITION;
import static com.implemica.model.operation.CalcMemory.MEMORY_CLEAR;
import static com.implemica.model.operation.CalcMemory.MEMORY_RECALL;
import static com.implemica.model.operation.CalcMemory.MEMORY_SHOW;
import static com.implemica.model.operation.CalcMemory.MEMORY_STORE;
import static com.implemica.model.operation.CalcMemory.MEMORY_SUBTRACTION;
import static com.implemica.model.operation.CalcOperator.RESULT;
import static com.implemica.model.operation.CalcOther.CLEAR_ALL;
import static com.implemica.model.operation.CalcOther.CLEAR_ENTRY;
import static com.implemica.model.operation.CalcOther.CLEAR_LAST;
import static com.implemica.model.operation.CalcOther.NEGATE;
import static com.implemica.model.operation.CalcOther.PERCENT;
import static com.implemica.model.operation.CalcOther.RESULT_;
import static com.implemica.model.operation.CalcOther.SEPARATOR;
import static java.util.Map.entry;

import com.implemica.model.CalcModel;
import com.implemica.model.operation.CalcMemory;
import com.implemica.util.constant.CalcConstant;
import com.implemica.view.components.buttons.CalcButton;
import com.implemica.model.operation.CalcFunction;
import com.implemica.model.operation.CalcOperator;
import com.implemica.model.operation.CalcOther;
import com.implemica.view.components.buttons.FunctionButton;
import com.implemica.view.components.buttons.MemoryButton;
import com.implemica.view.components.buttons.NumberButton;
import com.implemica.view.components.buttons.OperatorButton;
import com.implemica.view.components.buttons.OtherButton;
import com.implemica.util.resize.ResizeMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Controller {

  @FXML
  public Button sizeChanger;
  @FXML
  public Button rightScroller;
  @FXML
  public Button leftScroller;
  @FXML
  public Pane smallSqrt;
  @FXML
  public Pane largeSqrt;
  @FXML
  public Label calcView;
  @FXML
  public Button miniButton;
  @FXML
  public TextFlow storyView;
  @FXML
  public AnchorPane leftMenu;
  @FXML
  public Pane rightStoryPane;
  @FXML
  public Button bottomStoryButton;
  @FXML
  public GridPane numPad;
  @FXML
  public Pane paneForBottomStoryPane;
  @FXML
  public AnchorPane bottomStoryPane;
  @FXML
  public ScrollPane scrollPane;
  @FXML
  public MemoryButton memoryClear;
  @FXML
  public MemoryButton memoryRecall;
  @FXML
  public MemoryButton memoryAddition;
  @FXML
  public MemoryButton memorySubtraction;
  @FXML
  public MemoryButton memoryStore;
  @FXML
  public MemoryButton memoryShow;
  @FXML
  public HBox memoryBox;
  private Stage stage;
  private double relativeX;
  private double relativeY;
  private Font defaultFont;
  private boolean error;
  private String inputNumber = "0";
  private CalcOperator currentOperator;
  private boolean isResult;
  private BigDecimal savedNumber;
  private boolean isSavedNum;

  private List<Text> leftStory = new ArrayList<>();
  private BigDecimal leftResult = BigDecimal.ZERO;

  private String currentStory;
  private BigDecimal currentResult;

  private BigDecimal resultNumber = BigDecimal.ZERO;

  private final Map<CalcOther, Runnable> otherActivities = Map.ofEntries(
      entry(RESULT_, () -> {
        if (inputNumber != null) {
          currentResult = new BigDecimal(inputNumber);
          currentStory = OutputFormat.outputFormat(inputNumber, "");
          inputNumber = null;
        }

        if (isResult) {
          leftStory = new ArrayList<>();
          leftStory.add(storyText(OutputFormat.outputFormat(resultNumber, ""), 12));
          leftResult = resultNumber;
        }

        if (currentResult == null) {
          currentResult = resultNumber;
          currentStory = OutputFormat.outputFormat(resultNumber, "");
        }

        if (currentOperator != null) {
          resultNumber = currentOperator.modelExecuting(leftResult, currentResult);
        } else {
          resultNumber = currentResult;
        }

        isResult = true;
      }),
      entry(CLEAR_ALL, () -> {
        leftResult = BigDecimal.ZERO;
        leftStory = new ArrayList<>();
        currentOperator = null;
        inputNumber = "0";
        currentResult = null;
        currentStory = null;
        resultNumber = BigDecimal.ZERO;
        isResult = false;
      }),
      entry(CLEAR_ENTRY, () -> {
        inputNumber = "0";
        currentResult = null;
        currentStory = null;
        resultNumber = BigDecimal.ZERO;
      }),
      entry(CLEAR_LAST, () -> {
        if (inputNumber == null || inputNumber.equals("0")) {
          return;
        }
        if (CalcConstant.DIGIT_PATTERN.matcher(inputNumber).matches()) {
          inputNumber = "0";
        } else {
          inputNumber = inputNumber.substring(0, inputNumber.length() - 1);
        }
      }),
      entry(SEPARATOR, () -> {
        if (inputNumber == null) {
          inputNumber = "0.";
        } else if (!inputNumber.contains(".")) {
          inputNumber = inputNumber + ".";
        }
      }),
      entry(NEGATE, () -> {
        if (inputNumber != null) {
          if (inputNumber.equals("0")) {
            return;
          }
          if (inputNumber.contains("-")) {
            inputNumber = inputNumber.replace("-", "");
          } else {
            inputNumber = '-' + inputNumber;
          }
        } else {
          addFunction(CalcFunction.ADDITIVE_INVERSE);
        }
      }),
      entry(PERCENT, () -> {
        BigDecimal res = inputNumber != null ? new BigDecimal(inputNumber) : resultNumber;
        inputNumber = null;

        if (currentOperator != null && (currentOperator.equals(CalcOperator.MULTIPLICATION) || currentOperator.equals(CalcOperator.DIVISION))) {
          currentResult = CalcModel.percent(BigDecimal.ONE, res);
          currentStory = OutputFormat.outputFormat(currentResult, "");
        } else {
          currentResult = CalcModel.percent(leftResult, res);
          currentStory = OutputFormat.outputFormat(currentResult, "");
        }
        resultNumber = currentResult;
      })
  );

  private final Map<CalcMemory, Runnable> memoryActivities = Map.ofEntries(
      entry(MEMORY_ADDITION, () -> {
        if (savedNumber == null) {
          savedNumber = BigDecimal.ZERO;
          memoryClear.setDisable(false);
          memoryRecall.setDisable(false);
          memoryShow.setDisable(false);
        }
        if (inputNumber != null) {
          savedNumber = CalcOperator.ADDITION.modelExecuting(savedNumber, new BigDecimal(inputNumber));
        } else {
          savedNumber = CalcOperator.ADDITION.modelExecuting(savedNumber, resultNumber);
        }
      }),
      entry(MEMORY_CLEAR, () -> {
        savedNumber = null;
        memoryBox.getChildren().forEach(node -> ((MemoryButton) node).setDefaultEnabling());
      }),
      entry(MEMORY_RECALL, () -> {
        resultNumber = savedNumber;
        currentResult = savedNumber;
        currentStory = OutputFormat.outputFormat(savedNumber, "");
        inputNumber = null;
        if (currentOperator != null && currentOperator.equals(RESULT)) {
          currentStory = null;
        }
      }),
      entry(MEMORY_STORE, () -> {
        if (savedNumber == null) {
          savedNumber = BigDecimal.ZERO;
          memoryClear.setDisable(false);
          memoryRecall.setDisable(false);
          memoryShow.setDisable(false);
        }
        if (inputNumber != null) {
          savedNumber = new BigDecimal(inputNumber);
        } else {
          savedNumber = resultNumber;
        }
      }),
      entry(MEMORY_SUBTRACTION, () -> {
        if (savedNumber == null) {
          savedNumber = BigDecimal.ZERO;
          memoryClear.setDisable(false);
          memoryRecall.setDisable(false);
          memoryShow.setDisable(false);
        }
        if (inputNumber != null) {
          savedNumber = CalcOperator.SUBTRACTION.modelExecuting(savedNumber, new BigDecimal(inputNumber));
        } else {
          savedNumber = CalcOperator.SUBTRACTION.modelExecuting(savedNumber, resultNumber);
        }
      }),
      entry(MEMORY_SHOW, () -> {
        if (bottomStoryPane.isVisible()) {
          openBottomStoryPane(null);
        } else {
          closeBottomStoryPane(null);
        }
      })
  );

  private final Map<Class<? extends CalcButton>, Consumer<CalcButton>> calcActivities = Map.ofEntries(
      entry(NumberButton.class, button -> {
        String newDigit = ((NumberButton) button).getText();
        if (isResult) {
          otherActivities.get(CLEAR_ALL).run();
        }
        if (inputNumber == null || inputNumber.equals("0")) {
          inputNumber = newDigit;
          currentStory = null;
          currentResult = null;
        } else if (inputNumber.contains("0.") && inputNumber.length() < 18) {
          inputNumber = inputNumber + newDigit;
        } else if (inputNumber.replace(".", "").length() < 16) {
          inputNumber = inputNumber + newDigit;
        }
      }),
      entry(OtherButton.class, button -> otherActivities.get(((OtherButton) button).getType()).run()),
      entry(OperatorButton.class, button -> {
        CalcOperator operator = ((OperatorButton) button).getOperator();

        isResult = false;
        if (inputNumber != null) {
          currentResult = new BigDecimal(inputNumber);
          currentStory = OutputFormat.outputFormat(inputNumber, "");
          inputNumber = null;
        }

        if (currentResult == null) {
          currentOperator = operator;
          return;
        }

        if (currentOperator != null) {
          leftResult = currentOperator.modelExecuting(leftResult, currentResult);
          leftStory.add(storyText(currentOperator.getSymbol(), 8));
        } else {
          leftResult = currentResult;
        }

        if (currentStory != null) {
          leftStory.add(storyText(currentStory, 12));
        }

        resultNumber = leftResult;
        currentStory = null;
        currentResult = null;
        currentOperator = operator;
      }),
      entry(FunctionButton.class, button -> {
        CalcFunction function = ((FunctionButton) button).getFunction();

        if (isResult) {
          BigDecimal res = resultNumber;
          otherActivities.get(CLEAR_ALL).run();
          inputNumber = res.toString();
        }

        if (inputNumber != null) {
          currentResult = new BigDecimal(inputNumber);
          currentStory = OutputFormat.outputFormat(new BigDecimal(inputNumber), "");
          inputNumber = null;
        }

        if (currentResult == null) {
          currentResult = resultNumber;
          currentStory = OutputFormat.outputFormat(resultNumber, "");
        }

        currentStory = function.getFormula().replace("#", currentStory);
        currentResult = function.modelExecuting(currentResult);
        resultNumber = currentResult;
        isResult = false;
      }),
      entry(MemoryButton.class, button -> memoryActivities.get(((MemoryButton) button).getType()).run())
  );


  public void init(Stage stage) {
    this.stage = stage;

    ScrollBar scrollBar = (ScrollBar) ((ScrollPane) ((AnchorPane) ((AnchorPane) stage.getScene().lookup("#leftMenu")).getChildren().get(0)).getChildren().get(1)).lookup(".scroll-bar");
    scrollBar.setOnMouseMoved(e -> {
      scrollBar.lookup(":vertical .increment-arrow").setStyle("-fx-background-color: rgb(130, 130, 130);");
      scrollBar.lookup(":vertical .decrement-arrow").setStyle("-fx-background-color: rgb(130, 130, 130);");
      scrollBar.setPrefWidth(15);
    });
    scrollBar.setOnMouseExited(e -> {
      final Timer timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (!scrollBar.isHover() && !scrollBar.isPressed()) {
            scrollBar.lookup(":vertical .increment-arrow").setStyle("-fx-background-color: transparent;");
            scrollBar.lookup(":vertical .decrement-arrow").setStyle("-fx-background-color: transparent;");
            scrollBar.setPrefWidth(2);
            timer.cancel();
          }
        }
      }, 3000, 3000);
    });

    calcView.textProperty().addListener(e -> {

    });
    stage.widthProperty().addListener(stageSizeListener);
    stage.heightProperty().addListener(stageSizeListener);

    numPad.maxHeightProperty().bind(stage.heightProperty().subtract(10).multiply(2).divide(3));

    calcView.setTextOverrun(OverrunStyle.CLIP);
    storyView.widthProperty().addListener(e -> scrollPane.setHvalue(1.0));

    ChangeListener<Number> changeListener = (observableValue, number, t1) -> {
      Platform.runLater(() -> {
        Text text = new Text();
        text.setText(calcView.getText());
        text.setFont(new Font("Segoe UI Semibold", defaultFont.getSize()));
        Bounds calcViewBounds = calcView.localToScene(calcView.getBoundsInLocal());

        while (text.getLayoutBounds().getWidth() > calcViewBounds.getWidth()) {
          text.setFont(new Font(text.getFont().getFamily(), text.getFont().getSize() - 1));
        }

        calcView.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size:" + (text.getFont().getSize()));
      });
    };

    stage.getScene().heightProperty().addListener(changeListener);
    stage.getScene().widthProperty().addListener(changeListener);

    calcView.textProperty().

        addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> calcViewText, String oldValue, String newValue) {
            Text text = new Text();
            text.setText(newValue);
            text.setFont(new Font("Segoe UI Semibold", stage.getHeight() > 801 ? 78 : 48));
            Bounds calcViewBounds = calcView.localToScene(calcView.getBoundsInLocal());

            while (text.getLayoutBounds().getWidth() > calcViewBounds.getWidth()) {
              text.setFont(new Font(text.getFont().getFamily(), text.getFont().getSize() - 1));
            }

            calcView.setStyle("-fx-font-family: 'Segoe UI Semibold'; -fx-font-size:" + (text.getFont().getSize()));
          }
        });
  }

  public void onSystemPaneDragged(MouseEvent event) {
    paneForBottomStoryPane.setVisible(false);
    leftMenu.setVisible(false);
    if (ResizeMode.isMaximized()) {
      ResizeMode.switchWindowsMode();
      sizeChanger.setText("\uE922");
      relativeX = -1 * (stage.getWidth() / 2);
    } else if (stage.getScene().getCursor().equals(Cursor.DEFAULT) && event.getButton() == MouseButton.PRIMARY) {
      stage.setX(event.getScreenX() + relativeX);
      stage.setY(event.getScreenY() + relativeY);
    }
  }

  public void onSystemPanePressed(MouseEvent event) {
    if (stage.getScene().getCursor().equals(Cursor.DEFAULT) && event.getButton() == MouseButton.PRIMARY) {
      relativeX = stage.getX() - event.getScreenX();
      relativeY = stage.getY() - event.getScreenY();
    }
  }

  public void mini(MouseEvent event) {
    if (stage.getScene().getCursor().equals(Cursor.DEFAULT) && event.getButton() == MouseButton.PRIMARY) {
      stage.setIconified(true);
      miniButton.setStyle("-fx-background-color: transparent");
    }
  }

  public void close(MouseEvent event) {
    if (stage.getScene().getCursor().equals(Cursor.DEFAULT) && event.getButton() == MouseButton.PRIMARY) {
      stage.close();
    }
  }

  public void changeSize(MouseEvent event) {
    if (stage.getScene().getCursor().equals(Cursor.DEFAULT) && event.getButton() == MouseButton.PRIMARY) {
      ResizeMode.switchWindowsMode();
      if (ResizeMode.isMaximized()) {
        sizeChanger.setText("\uE923");
      } else {
        sizeChanger.setText("\uE922");
      }
    }
  }

  public void scrollRight(MouseEvent event) {
    scrollPane.setHvalue(scrollPane.getHvalue() + 100);
  }

  public void scrollLeft(MouseEvent event) {
    scrollPane.setHvalue(scrollPane.getHvalue() - 100);
  }

  public void calcActivity(MouseEvent event) {
    if (event.getButton() != MouseButton.PRIMARY) {
      return;
    }
    CalcButton button = (CalcButton) event.getSource();

    if (error) {
      error = false;
      otherActivities.get(CLEAR_ALL).run();
      memoryBox.getChildren().forEach(node -> ((MemoryButton) node).setDefaultEnabling());
      numPad.getChildren().forEach(node -> node.setDisable(false));
      if (button instanceof NumberButton) {
        calcActivity(event);
      }
      return;
    }

    try {
      calcActivities.get(button.getClass()).accept(button);
      calcView.setText(view());
    } catch (NumberFormatException e) {
      calcView.setText(e.getMessage());
      numPad.getChildren().filtered(node -> !((CalcButton) node).isErrorAllowed()).forEach(node -> node.setDisable(true));
      memoryActivities.get(MEMORY_CLEAR).run();
      memoryBox.getChildren().forEach(node -> node.setDisable(true));
      error = true;
    }

    storyView.getChildren().clear();
    storyView.getChildren().addAll(leftStory);

    if (currentOperator != null) {
      storyView.getChildren().add(storyText(currentOperator.getSymbol(), 8));
    }
    if (currentStory != null && !(button instanceof MemoryButton)) {
      storyView.getChildren().add(storyText(currentStory, 12));
    }
    if (isResult) {
      storyView.getChildren().add(storyText(RESULT.getSymbol(), 8));
    }

    storyView.setPrefWidth(Double.max(storyView.getChildren().stream().map(e -> e.getLayoutBounds().getWidth()).reduce(Double::sum).orElse(0.0), stage.getWidth()) + 25);
  }

  public void switchLeftMenu(MouseEvent event) {

    Button button;
    if (event.getSource() instanceof Button) {
      button = (Button) event.getSource();
    } else {
      button = (Button) ((AnchorPane) leftMenu.getChildren().get(0)).getChildren().get(0);
    }
    if (button.isDisable()) {
      return;
    }
    AnchorPane anchorPane = (AnchorPane) leftMenu.getChildren().get(0);
    ScrollPane pane = (ScrollPane) anchorPane.getChildren().get(1);
    Button aboutButton = (Button) anchorPane.getChildren().get(2);

    double width = 255;
    if (leftMenu.isVisible()) {
      final Timer timer = new Timer();
      timer.scheduleAtFixedRate(new TimerTask() {
        double i = width / 100;

        @Override
        public void run() {
          if (pane.getPrefWidth() > 0) {
            pane.setPrefWidth(pane.getPrefWidth() - i);
            anchorPane.setPrefWidth(pane.getPrefWidth() - i);
            if (pane.getPrefWidth() - 40 < 0) {
              anchorPane.setVisible(false);
              aboutButton.setVisible(false);
            }
          } else {
            timer.cancel();
            timer.purge();
            button.setDisable(false);
            leftMenu.setVisible(!leftMenu.isVisible());
            pane.setPrefWidth(width);
            pane.setPrefWidth(width);
          }
        }
      }, 0, 5);
    } else {
      anchorPane.setPrefWidth(0);
      pane.setPrefWidth(0);
      final Timer timer = new Timer();
      timer.scheduleAtFixedRate(new TimerTask() {
        double i = width / 100;

        @Override
        public void run() {
          if (pane.getPrefWidth() < width) {
            pane.setPrefWidth(pane.getPrefWidth() + i);
            anchorPane.setPrefWidth(anchorPane.getPrefWidth() + i);
            if (pane.getPrefWidth() + width - 40 > width) {
              aboutButton.setVisible(true);
              anchorPane.setVisible(true);
            }
          } else {
            timer.cancel();
            timer.purge();
            button.setDisable(false);
          }
        }
      }, 0, 5);
      leftMenu.setVisible(!leftMenu.isVisible());
    }
  }

  private ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
    paneForBottomStoryPane.setVisible(false);
    leftMenu.setVisible(false);
    if (stage.getWidth() > 583 && rightStoryPane.getMaxWidth() != 325) {
      memoryShow.setVisible(false);
      rightStoryPane.setMaxWidth(325.0);
      rightStoryPane.setPrefWidth(245.0);
      rightStoryPane.setMinWidth(245.0);
      bottomStoryButton.setVisible(false);
    } else if (stage.getWidth() < 583 && rightStoryPane.getMaxWidth() != 0) {
      rightStoryPane.setMaxWidth(0);
      rightStoryPane.setPrefWidth(0);
      rightStoryPane.setMinWidth(0);
      bottomStoryButton.setVisible(true);
      memoryShow.setVisible(true);
    }

    if (stage.getWidth() > 795 && stage.getHeight() > 780) {
      largeSqrt.setVisible(true);
      smallSqrt.setVisible(false);
      numPad.getChildren().forEach(node -> {
        if (node.getStyleClass().contains("gray-button") || node.getStyleClass().contains("result-button")) {
          node.setStyle("-fx-font-size: 20;");
        }

        if (node.getStyleClass().contains("white-button")) {
          node.setStyle("-fx-font-size: 24;");
        }
      });
    } else {
      largeSqrt.setVisible(false);
      smallSqrt.setVisible(true);
      numPad.getChildren().forEach(node -> {
        if (node.getStyleClass().contains("gray-button") || node.getStyleClass().contains("result-button")) {
          node.setStyle("-fx-font-size: 15;");
        }
        if (node.getStyleClass().contains("white-button")) {
          node.setStyle("-fx-font-size: 18;");
        }
      });
    }

    if (stage.getHeight() > 810) {
      defaultFont = new Font("Segoe UI Semibold", 72);
    } else {
      defaultFont = new Font("Segoe UI Semibold", 48);
    }
  };

  public void openBottomStoryPane(MouseEvent event) {
    paneForBottomStoryPane.setVisible(true);
    Bounds b = numPad.localToScene(numPad.getBoundsInLocal());
    bottomStoryPane.setPrefWidth(b.getWidth());
    bottomStoryPane.setPrefHeight(b.getHeight());
    bottomStoryPane.setLayoutY(stage.getHeight() - b.getHeight() - 42);
  }

  public void closeBottomStoryPane(MouseEvent event) {
    paneForBottomStoryPane.setVisible(false);
  }

  private void addFunction(CalcFunction function) {
    if (isResult) {
      BigDecimal res = resultNumber;
      otherActivities.get(CLEAR_ALL).run();
      inputNumber = res.toString();
    }

    if (inputNumber != null) {
      currentResult = new BigDecimal(inputNumber);
      currentStory = OutputFormat.outputFormat(inputNumber, "");
      inputNumber = null;
    }

    if (currentResult == null) {
      currentResult = resultNumber;
      currentStory = OutputFormat.outputFormat(resultNumber, "");
    }

    currentStory = function.getFormula().replace("#", currentStory);
    currentResult = function.modelExecuting(currentResult);
    resultNumber = currentResult;
    isResult = false;
  }

  private Text storyText(String text, int size) {
    Text element = new Text();
    element.setText(text + ' ');
    element.setStyle("-fx-font-size:" + size);
    return element;
  }

  private String view() {
    if (inputNumber != null) {
      return OutputFormat.outputFormat(inputNumber, " ");
    }
    return OutputFormat.outputFormat(resultNumber, " ");
  }
}

