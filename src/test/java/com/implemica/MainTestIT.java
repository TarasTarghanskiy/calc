package com.implemica;

import com.implemica.controller.OutputFormat;
import com.implemica.model.operation.CalcOperator;
import com.implemica.view.View;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextFlowMatchers;
import org.testfx.util.WaitForAsyncUtils;

class MainTestIT extends ApplicationTest {

  private Logger log = Logger.getRootLogger();
  private Map<String, Node> keyButtonMap = new HashMap<>();
  private FxRobot robot;
  private final static Random RANDOM = new Random();
  private static final MathContext CEILING = new MathContext(10100, RoundingMode.CEILING);
  private static final MathContext HALF_UP = new MathContext(10100, RoundingMode.HALF_UP);
  private Robot awtRobot = new Robot();

  MainTestIT() throws AWTException {
  }

  @Override
  public void init() throws Exception {
    FxToolkit.registerStage(Stage::new);
  }

  @Start
  public void start(Stage stage) throws Exception {
    new View().start(stage);
    ((GridPane) stage.getScene().lookup("#numPad")).getChildren().forEach(b -> {
      switch (b.toString()) {
        case "PERCENT":
          keyButtonMap.put("%", b);
          break;
        case "CLEAR_ENTRY":
          keyButtonMap.put("E", b);
          break;
        case "CLEAR_ALL":
          keyButtonMap.put("A", b);
          break;
        case "CLEAR_LAST":
          keyButtonMap.put("L", b);
          break;
        case "ONE_DIVIDED_BY_X":
          keyButtonMap.put("&", b);
          break;
        case "EXPONENT_TWO":
          keyButtonMap.put("²", b);
          break;
        case "SQUARE_ROOT_OF_TWO":
          keyButtonMap.put("√", b);
          break;
        case "NEGATE":
          keyButtonMap.put("±", b);
          break;
        case "SEPARATOR":
          keyButtonMap.put(".", b);
          break;
        case "RESULT_":
          keyButtonMap.put("=", b);
          break;
        default:
          keyButtonMap.put(b.toString(), b);
      }
    });


    ((HBox) stage.getScene().lookup("#memoryBox")).getChildren().forEach(b -> {
      switch (b.toString()) {
        case "MEMORY_CLEAR":
          keyButtonMap.put("C", b);
          break;
        case "MEMORY_RECALL":
          keyButtonMap.put("R", b);
          break;
        case "MEMORY_ADDITION+":
          keyButtonMap.put("P", b);
          break;
        case "MEMORY_SUBTRACTION":
          keyButtonMap.put("M", b);
          break;
        case "MEMORY_STORE":
          keyButtonMap.put("S", b);
          break;
        case "MEMORY_SHOW":
          keyButtonMap.put("V", b);
          break;

      }
    });
  }

  private void pressButton(Node button) {
    Bounds boundInScreen = button.localToScreen(button.getBoundsInLocal());
    awtRobot.mouseMove((int) boundInScreen.getCenterX(), (int) boundInScreen.getCenterY());
    awtRobot.delay(15);
    awtRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    awtRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
//        robot.clickOn(keyButtonMap.get(key));
  }

  private String randomNumber() {
    StringBuilder builder = new StringBuilder();
    boolean haveDot = false;

    for (int i = 0; i < 15; i++) {
      builder.append(RANDOM.nextInt(9));
      if (!haveDot && RANDOM.nextInt(10) < 1) {
        haveDot = true;
        builder.append(".");
        builder.append(RANDOM.nextInt(9));
      }
    }

    return builder.toString();
  }

  private void assertOperations(String operations, String viewResult, String historyResult) {
    pressButton(keyButtonMap.get("A"));
    Stream.of(operations.split(""))
        .map(e -> keyButtonMap.get(e))
        .map(e -> (Node) e)
        .forEach(this::pressButton);

    WaitForAsyncUtils.waitForFxEvents();
    FxAssert.verifyThat("#calcView", LabeledMatchers.hasText(viewResult));
    FxAssert.verifyThat("#storyView", TextFlowMatchers.hasText(CalcOperator.replace(historyResult)));
  }


  @Test
  void basicTests() {
    assertOperations("5²", "25", "sqr(5) ");
    assertOperations("5²²", "625", "sqr(sqr(5)) ");
    assertOperations("10/3*3=", "10", "10 / 3 * 3 = ");
    assertOperations("3&*3=", "1", "1/(3) * 3 = ");
    assertOperations("3*3&=", "1", "3 * 1/(3) = ");
    assertOperations("2/3*3=", "2", "2 / 3 * 3 = ");
    assertOperations("1+1±=", "0", "1 + -1 = ");
    assertOperations("1±+1=", "0", "-1 + 1 = ");
  }

  @Test
  void simpleOverflowTests() {
    assertOperations("9999999999999999²²²²²²²²²²", "Overflow", "sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(9999999999999999)))))))))) ");
    assertOperations("0.0000000000000001²²²²²²²²²²", "Overflow", "sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,0000000000000001)))))))))) ");
  }

  @Test
  void maxResultRoundingTests() {
    assertOperations("9999999999999999+0.49=", "9 999 999 999 999 999", "9999999999999999 + 0,49 = ");
    assertOperations("9999999999999999+0.5=", "1e+16", "9999999999999999 + 0,5 = ");
    assertOperations("9999999999999999+0.51=", "1e+16", "9999999999999999 + 0,51 = ");
  }

  @Test
  void topOverflowTests() {
    assertOperations("9999999999999999+0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²-1*10+9+", "9,999999999999999e+9999", "9999999999999999 + 0,5 / 10 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) - 1 * 10 + 9 + ");
    assertOperations("9999999999999999+0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²-1*10+10+", "9,999999999999999e+9999", "9999999999999999 + 0,5 / 10 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) - 1 * 10 + 10 + ");
    assertOperations("9999999999999999+0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²-1*10+11+", "Overflow", "9999999999999999 + 0,5 / 10 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) - 1 * 10 + 11 + ");
  }

  @Test
  void bottomOverflowTests() {
    assertOperations("0.000000000000011*0.1*0.000000000000001²²²²²²²²²*0.000000000000001²²²²²²²*0.000000000001²²²²²=", "1,1e-9999", "0,000000000000011 * 0,1 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))) * sqr(sqr(sqr(sqr(sqr(0,000000000001))))) = ");
    assertOperations("0.00000000000001*0.1*0.000000000000001²²²²²²²²²*0.000000000000001²²²²²²²*0.000000000001²²²²²=", "1e-9999", "0,00000000000001 * 0,1 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))) * sqr(sqr(sqr(sqr(sqr(0,000000000001))))) = ");
    assertOperations("0.000000000000009*0.1*0.000000000000001²²²²²²²²²*0.000000000000001²²²²²²²*0.000000000001²²²²²=", "Overflow", "0,000000000000009 * 0,1 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))) * sqr(sqr(sqr(sqr(sqr(0,000000000001))))) = ");
  }

  @Test
  void negateBottomOverflowTests() {
    assertOperations("0.000000000000011±*0.1*0.000000000000001²²²²²²²²²*0.000000000000001²²²²²²²*0.000000000001²²²²²=", "-1,1e-9999", "-0,000000000000011 * 0,1 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))) * sqr(sqr(sqr(sqr(sqr(0,000000000001))))) = ");
    assertOperations("0.00000000000001±*0.1*0.000000000000001²²²²²²²²²*0.000000000000001²²²²²²²*0.000000000001²²²²²=", "-1e-9999", "-0,00000000000001 * 0,1 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))) * sqr(sqr(sqr(sqr(sqr(0,000000000001))))) = ");
    assertOperations("0.000000000000009±*0.1*0.000000000000001²²²²²²²²²*0.000000000000001²²²²²²²*0.000000000001²²²²²=", "Overflow", "-0,000000000000009 * 0,1 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(0,000000000000001))))))) * sqr(sqr(sqr(sqr(sqr(0,000000000001))))) = ");
  }

  @Test
  void negateTopOverflowTests() {
    assertOperations("9999999999999999±-0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+1*10-9-", "-9,999999999999999e+9999", "-9999999999999999 - 0,5 / 10 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + 1 * 10 - 9 - ");
    assertOperations("9999999999999999±-0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+1*10-10-", "-9,999999999999999e+9999", "-9999999999999999 - 0,5 / 10 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + 1 * 10 - 10 - ");
    assertOperations("9999999999999999±-0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+1*10-11-", "Overflow", "-9999999999999999 - 0,5 / 10 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + 1 * 10 - 11 - ");
  }


  @Test
  void divisionTests() {
    log.info("division tests status: start");
    assertOperations("4/0=", "Cannot divide by zero", "4 / 0 ");
    assertOperations("4/1=", "4", "4 / 1 = ");
    assertOperations("4/1±=", "-4", "4 / -1 = ");
    assertOperations("5/0=", "Cannot divide by zero", "5 / 0 ");
    assertOperations("3/0=", "Cannot divide by zero", "3 / 0 ");
    assertOperations("253476/0=", "Cannot divide by zero", "253476 / 0 ");
    assertOperations("253476/1=", "253 476", "253476 / 1 = ");
    assertOperations("253476/1±=", "-253 476", "253476 / -1 = ");
    assertOperations("253477/0=", "Cannot divide by zero", "253477 / 0 ");
    assertOperations("253475/0=", "Cannot divide by zero", "253475 / 0 ");
    assertOperations("0.236478/0=", "Cannot divide by zero", "0,236478 / 0 ");
    assertOperations("0.236478/1=", "0,236478", "0,236478 / 1 = ");
    assertOperations("0.236478/1±=", "-0,236478", "0,236478 / -1 = ");
    assertOperations("0.236479/0=", "Cannot divide by zero", "0,236479 / 0 ");
    assertOperations("0.236477/0=", "Cannot divide by zero", "0,236477 / 0 ");

    assertOperations("8/4=", "2", "8 / 4 = ");
    assertOperations("65/15=", "4,333333333333333", "65 / 15 = ");
    assertOperations("672/121=", "5,553719008264463", "672 / 121 = ");
    assertOperations("894305/712432=", "1,255284714892088", "894305 / 712432 = ");

    assertOperations("0.4/0.8=", "0,5", "0,4 / 0,8 = ");
    assertOperations("0.45/0.15=", "3", "0,45 / 0,15 = ");
    assertOperations("0.6598/0.2345=", "2,8136460554371", "0,6598 / 0,2345 = ");
    assertOperations("0.783459/0.243895=", "3,212279874536173", "0,783459 / 0,243895 = ");

    assertOperations("6/6±=", "-1", "6 / -6 = ");
    assertOperations("86/58±=", "-1,482758620689655", "86 / -58 = ");
    assertOperations("7232/2134±=", "-3,388940955951265", "7232 / -2134 = ");
    assertOperations("834579/324465±=", "-2,572169571448384", "834579 / -324465 = ");

    assertOperations("±8/4=", "2", "8 / 4 = ");
    assertOperations("±65/15=", "4,333333333333333", "65 / 15 = ");
    assertOperations("±672/121=", "5,553719008264463", "672 / 121 = ");
    assertOperations("±894305/712432=", "1,255284714892088", "894305 / 712432 = ");

    assertOperations("0.3/0.2±=", "-1,5", "0,3 / -0,2 = ");
    assertOperations("0.84/0.42±=", "-2", "0,84 / -0,42 = ");
    assertOperations("0.7372/0.0472±=", "-15,61864406779661", "0,7372 / -0,0472 = ");
    assertOperations("0.612783/0.642837±=", "-0,9532478684332109", "0,612783 / -0,642837 = ");

    assertOperations("0.9±/0.4=", "-2,25", "-0,9 / 0,4 = ");
    assertOperations("0.83±/0.12=", "-6,916666666666667", "-0,83 / 0,12 = ");
    assertOperations("0.7583±/0.0002=", "-3 791,5", "-0,7583 / 0,0002 = ");
    assertOperations("0.642387±/0.634573=", "-1,012313792109024", "-0,642387 / 0,634573 = ");

    for (int i = 0; i < 20; i++) {
      BigDecimal firstNum = new BigDecimal(randomNumber());
      BigDecimal secondNum = new BigDecimal(randomNumber());
      String history = OutputFormat.outputFormat(firstNum, "") + " / " + OutputFormat.outputFormat(secondNum, "") + " = ";
      String result = OutputFormat.outputFormat(firstNum.divide(secondNum, CEILING), " ");
      log.info(firstNum + " / " + secondNum + " = " + result);
      assertOperations(firstNum.toString() + "/" + secondNum.toString() + "=", result, history);
    }
    log.info("division tests status: passed");
  }

  @Test
  void multiplicationTests() {
    log.info("multiplication tests status: start");
    assertOperations("0*1=", "0", "0 * 1 = ");
    assertOperations("0*1±=", "0", "0 * -1 = ");
    assertOperations("0*1±=", "0", "0 * -1 = ");
    assertOperations("1*0=", "0", "1 * 0 = ");
    assertOperations("1*0±=", "0", "1 * 0 = ");
    assertOperations("1±*0=", "0", "-1 * 0 = ");

    assertOperations("643860*1=", "643 860", "643860 * 1 = ");
    assertOperations("1*2356823=", "2 356 823", "1 * 2356823 = ");
    assertOperations("1±*0.1234=", "-0,1234", "-1 * 0,1234 = ");
    assertOperations("1*7834±=", "-7 834", "1 * -7834 = ");
    assertOperations("1±*0.486975±=", "0,486975", "-1 * -0,486975 = ");
    assertOperations("2345*1±=", "-2 345", "2345 * -1 = ");
    assertOperations("0.4124±*1=", "-0,4124", "-0,4124 * 1 = ");
    assertOperations("523878±*1±=", "523 878", "-523878 * -1 = ");

    assertOperations("4*5=", "20", "4 * 5 = ");
    assertOperations("34*89=", "3 026", "34 * 89 = ");
    assertOperations("5324*7665=", "40 808 460", "5324 * 7665 = ");
    assertOperations("347873*234678=", "81 638 139 894", "347873 * 234678 = ");
    assertOperations("34567834*54357832=", "1 879 032 513 175 888", "34567834 * 54357832 = ");

    assertOperations("6±*9=", "-54", "-6 * 9 = ");
    assertOperations("56±*99=", "-5 544", "-56 * 99 = ");
    assertOperations("7283±*9829=", "-71 584 607", "-7283 * 9829 = ");
    assertOperations("723468±*234829=", "-169 891 266 972", "-723468 * 234829 = ");
    assertOperations("63846578±*61204903=", "-3 907 723 613 371 934", "-63846578 * 61204903 = ");

    assertOperations("7*8±=", "-56", "7 * -8 = ");
    assertOperations("32*48±=", "-1 536", "32 * -48 = ");
    assertOperations("6343*9456±=", "-59 979 408", "6343 * -9456 = ");
    assertOperations("650000*250000±=", "-162 500 000 000", "650000 * -250000 = ");
    assertOperations("62374823*92384372±=", "-5 762 458 851 466 156", "62374823 * -92384372 = ");

    assertOperations("4*5=", "20", "4 * 5 = ");
    assertOperations("34*89=", "3 026", "34 * 89 = ");
    assertOperations("5324*7665=", "40 808 460", "5324 * 7665 = ");
    assertOperations("347873*234678=", "81 638 139 894", "347873 * 234678 = ");
    assertOperations("34567834*54357832=", "1 879 032 513 175 888", "34567834 * 54357832 = ");

    assertOperations("0.3*0.5=", "0,15", "0,3 * 0,5 = ");
    assertOperations("0.23*0.57=", "0,1311", "0,23 * 0,57 = ");
    assertOperations("0.2364*0.5786=", "0,13678104", "0,2364 * 0,5786 = ");
    assertOperations("0.346343*0.563478=", "0,195156660954", "0,346343 * 0,563478 = ");
    assertOperations("0.67345893*0.18593420=", "0,125219047382406", "0,67345893 * 0,1859342 = ");

    assertOperations("0.4±*0.1=", "-0,04", "-0,4 * 0,1 = ");
    assertOperations("0.06±*0.12=", "-0,0072", "-0,06 * 0,12 = ");
    assertOperations("0.4356±*0.3464=", "-0,15089184", "-0,4356 * 0,3464 = ");
    assertOperations("0.346323±*0.899349=", "-0,311465243727", "-0,346323 * 0,899349 = ");
    assertOperations("0.12345648±*0.12435123=", "-0,0153519651394704", "-0,12345648 * 0,12435123 = ");

    assertOperations("0.5*0.3±=", "-0,15", "0,5 * -0,3 = ");
    assertOperations("0.08*0.25±=", "-0,02", "0,08 * -0,25 = ");
    assertOperations("0.8437*0.2341±=", "-0,19751017", "0,8437 * -0,2341 = ");
    assertOperations("0.927349*0.234657±=", "-0,217608934293", "0,927349 * -0,234657 = ");
    assertOperations("0.23759325*0.26582823±=", "-0,0631589931074475", "0,23759325 * -0,26582823 = ");

    assertOperations("9999999999999999+0.5*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=", "9,999999999999999e+9999", "9999999999999999 + 0,5 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) = ");
    assertOperations("9999999999999999+0.6*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=", "Overflow", "9999999999999999 + 0,6 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) = ");
    assertOperations("9999999999999999+0.7*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=", "Overflow", "9999999999999999 + 0,7 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) = ");


    assertOperations("999999999999999+0.94*10=", "9 999 999 999 999 999", "999999999999999 + 0,94 * 10 = ");
    assertOperations("999999999999999+0.95*10=", "1e+16", "999999999999999 + 0,95 * 10 = ");
    assertOperations("999999999999999+0.96*10=", "1e+16", "999999999999999 + 0,96 * 10 = ");

    for (int i = 0; i < 20; i++) {
      BigDecimal firstNum = new BigDecimal(randomNumber());
      BigDecimal secondNum = new BigDecimal(randomNumber());
      String history = OutputFormat.outputFormat(firstNum, "") + " * " + OutputFormat.outputFormat(secondNum, "") + " = ";
      String result = OutputFormat.outputFormat(firstNum.multiply(secondNum, CEILING), " ");
      log.info(history + " = " + result);
      assertOperations(firstNum.toString() + '*' + secondNum.toString() + '=', result, history);
    }
    log.info("multiplication tests status: passed");
  }

  @Test
  void exponentOfTwoTests() {
    log.info("exponentOfTwo tests status: start");
    assertOperations("0²", "0", "sqr(0) ");
    assertOperations("1²", "1", "sqr(1) ");
    assertOperations("1±²", "1", "sqr(-1) ");

    assertOperations("12²", "144", "sqr(12) ");
    assertOperations("312²", "97 344", "sqr(312) ");
    assertOperations("0.32²", "0,1024", "sqr(0,32) ");
    assertOperations("0.423²", "0,178929", "sqr(0,423) ");
    assertOperations("0.2346²", "0,05503716", "sqr(0,2346) ");
    assertOperations("0.23466²", "0,0550653156", "sqr(0,23466) ");
    assertOperations("0.325456²", "0,105921607936", "sqr(0,325456) ");
    assertOperations("0.325456±²", "0,105921607936", "sqr(-0,325456) ");
    assertOperations("0.2635947²", "0,06948216586809", "sqr(0,2635947) ");

    assertOperations("4532²", "20 539 024", "sqr(4532) ");
    assertOperations("42364²", "1 794 708 496", "sqr(42364) ");
    assertOperations("234576²", "55 025 899 776", "sqr(234576) ");
    assertOperations("234576±²", "55 025 899 776", "sqr(-234576) ");
    assertOperations("2374685²", "5 639 128 849 225", "sqr(2374685) ");
    assertOperations("500050050²", "2,500500525050025e+17", "sqr(500050050) ");

    assertOperations("4±²", "16", "sqr(-4) ");
    assertOperations("60±²", "3 600", "sqr(-60) ");
    assertOperations("6459±²", "41 718 681", "sqr(-6459) ");
    assertOperations("234687±²", "55 077 987 969", "sqr(-234687) ");
    assertOperations("65348734±²", "4 270 457 035 402 756", "sqr(-65348734) ");
    assertOperations("7263409542±²", "5,275711817481665e+19", "sqr(-7263409542) ");

    assertOperations("5816321949²²²²²²²²²²", "1,000000894193004e+9999", "sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(5816321949)))))))))) ");
    assertOperations("581632195²²²²²²²²²²²²", "Overflow", "sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(581632195))))))))))) ");

    assertOperations("100000000+0.00000002=²", "1e+16", "sqr(100000000) ");
    assertOperations("100000000+0.00000003=²", "1,000000000000001e+16", "sqr(100000000) ");
    assertOperations("100000000+0.00000004=²", "1,000000000000001e+16", "sqr(100000000) ");

    assertOperations("0.0100000002²", "0,000100000004", "sqr(0,0100000002) ");
    assertOperations("0.0100000003²", "1,000000060000001e-4", "sqr(0,0100000003) ");
    assertOperations("0.0100000004²", "1,000000080000002e-4", "sqr(0,0100000004) ");

    for (int i = 0; i < 20; i++) {
      BigDecimal firstNum = new BigDecimal(randomNumber());
      String result = OutputFormat.outputFormat(firstNum.multiply(firstNum, HALF_UP), " ");
      log.info(firstNum + "² = " + result);
      assertOperations(firstNum.toString() + '²', result, "sqr(" + OutputFormat.outputFormat(firstNum, "") + ") ");
    }
    log.info("exponentOfTwo tests status: passed");
  }

  @Test
  void subtractionTests() {
    log.info("subtraction tests status: start");

    assertOperations("9-5=", "4", "9 - 5 = ");
    assertOperations("23-11=", "12", "23 - 11 = ");
    assertOperations("4570-3242=", "1 328", "4570 - 3242 = ");
    assertOperations("321546-432648=", "-111 102", "321546 - 432648 = ");
    assertOperations("73684637-56348599=", "17 336 038", "73684637 - 56348599 = ");

    assertOperations("9.4-5.2=", "4,2", "9,4 - 5,2 = ");
    assertOperations("23.81-34.12=", "-10,31", "23,81 - 34,12 = ");
    assertOperations("6347.2436-3424.0033=", "2 923,2403", "6347,2436 - 3424,0033 = ");
    assertOperations("426387.172389-999000.624023=", "-572 613,451634", "426387,172389 - 999000,624023 = ");

    assertOperations("0.67-0.99=", "-0,32", "0,67 - 0,99 = ");
    assertOperations("0.1357-0.7531=", "-0,6174", "0,1357 - 0,7531 = ");
    assertOperations("0.263578-0.125368=", "0,13821", "0,263578 - 0,125368 = ");
    assertOperations("0.63254723-0.26357992=", "0,36896731", "0,63254723 - 0,26357992 = ");

    assertOperations("4-4±=", "8", "4 - -4 = ");
    assertOperations("34-56±=", "90", "34 - -56 = ");
    assertOperations("4567-2345±=", "6 912", "4567 - -2345 = ");
    assertOperations("42368576-12347865±=", "54 716 441", "42368576 - -12347865 = ");

    assertOperations("5±-9=", "-14", "-5 - 9 = ");
    assertOperations("45±-18=", "-63", "-45 - 18 = ");
    assertOperations("2374±-2183=", "-4 557", "-2374 - 2183 = ");
    assertOperations("938529±-237598=", "-1 176 127", "-938529 - 237598 = ");

    assertOperations("0.26-0.43±=", "0,69", "0,26 - -0,43 = ");
    assertOperations("0.2187-0.5647±=", "0,7834", "0,2187 - -0,5647 = ");
    assertOperations("0.182379-0.993482±=", "1,175861", "0,182379 - -0,993482 = ");
    assertOperations("0.43254798-0.99462837±=", "1,42717635", "0,43254798 - -0,99462837 = ");

    assertOperations("0.45±-0.23=", "-0,68", "-0,45 - 0,23 = ");
    assertOperations("0.9685±-0.0315=", "-1", "-0,9685 - 0,0315 = ");
    assertOperations("0.024099±-0.002357=", "-0,026456", "-0,024099 - 0,002357 = ");
    assertOperations("0.28340423±-0.82359043=", "-1,10699466", "-0,28340423 - 0,82359043 = ");

    assertOperations("9999999999999999+0.6-0.1=", "1e+16", "9999999999999999 + 0,6 - 0,1 = ");
    assertOperations("9999999999999999+0.6-0.1==", "9 999 999 999 999 999", "1e+16 - 0,1 = ");
    assertOperations("9999999999999999+0.6-0.1===", "9 999 999 999 999 999", "9999999999999999 - 0,1 = ");

    assertOperations("9999999999999999±-0.49=", "-9 999 999 999 999 999", "-9999999999999999 - 0,49 = ");
    assertOperations("9999999999999999±-0.5=", "-1e+16", "-9999999999999999 - 0,5 = ");
    assertOperations("9999999999999999±-0.51=", "-1e+16", "-9999999999999999 - 0,51 = ");

    assertOperations("9999999999999999-9999999999999999=", "0", "9999999999999999 - 9999999999999999 = ");
    assertOperations("0.0000000000000001-0.0000000000000001=", "0", "0,0000000000000001 - 0,0000000000000001 = ");
    assertOperations("9999999999999999-9999999999999999±=", "2e+16", "9999999999999999 - -9999999999999999 = ");
    assertOperations("0.0000000000000001-0.0000000000000001±=", "0,0000000000000002", "0,0000000000000001 - -0,0000000000000001 = ");
    assertOperations("9999999999999999±-9999999999999999=", "-2e+16", "-9999999999999999 - 9999999999999999 = ");
    assertOperations("0.0000000000000001±-0.0000000000000001=", "-0,0000000000000002", "-0,0000000000000001 - 0,0000000000000001 = ");

    assertOperations("0.001=", "0,001", "0,001 = ");
    assertOperations("0.001-0.000000001²=", "9,99999999999999e-4", "0,001 - sqr(0,000000001) = ");
    assertOperations("0.001-0.000000001²==", "9,99999999999998e-4", "9,99999999999999e-4 - sqr(0,000000001) = ");

    assertOperations("0.01=", "0,01", "0,01 = ");
    assertOperations("0.01-0.00000001²=", "0,0099999999999999", "0,01 - sqr(0,00000001) = ");
    assertOperations("0.01-0.00000001²==", "0,0099999999999998", "0,0099999999999999 - sqr(0,00000001) = ");

    assertOperations("0.1=", "0,1", "0,1 = ");
    assertOperations("0.1-0.00000001²=", "0,0999999999999999", "0,1 - sqr(0,00000001) = ");
    assertOperations("0.1-0.00000001²==", "0,0999999999999998", "0,0999999999999999 - sqr(0,00000001) = ");

    assertOperations("1=", "1", "1 = ");
    assertOperations("1-0.00000001²=", "0,9999999999999999", "1 - sqr(0,00000001) = ");
    assertOperations("1-0.00000001²==", "0,9999999999999998", "0,9999999999999999 - sqr(0,00000001) = ");

    for (int i = 0; i < 20; i++) {
      BigDecimal firstNum = new BigDecimal(randomNumber());
      BigDecimal secondNum = new BigDecimal(randomNumber());
      String history = OutputFormat.outputFormat(firstNum, "") + " - " + OutputFormat.outputFormat(secondNum, "") + " = ";
      String result = OutputFormat.outputFormat(firstNum.subtract(secondNum, CEILING), " ");
      log.info(history + " = " + result);
      assertOperations(firstNum.toString() + '-' + secondNum.toString() + '=', result, history);
    }
    log.info("subtraction tests status: passed");
  }

  @Test
  void additionTests() {
    log.info("addition tests status: start");
    assertOperations("9+9±=", "0", "9 + -9 = ");
    assertOperations("39+39±=", "0", "39 + -39 = ");
    assertOperations("2367±+2367=", "0", "-2367 + 2367 = ");
    assertOperations("138945+138945±=", "0", "138945 + -138945 = ");
    assertOperations("7678676445478655±+7678676445478655=", "0", "-7678676445478655 + 7678676445478655 = ");

    assertOperations("0.3+0.3±=", "0", "0,3 + -0,3 = ");
    assertOperations("0.8472±+0.8472=", "0", "-0,8472 + 0,8472 = ");
    assertOperations("0.829345+0.829345±=", "0", "0,829345 + -0,829345 = ");
    assertOperations("0.1245162341235784±+0.1245162341235784=", "0", "-0,1245162341235784 + 0,1245162341235784 = ");

    assertOperations("1268+2219=", "3 487", "1268 + 2219 = ");
    assertOperations("2345+1078±=", "1 267", "2345 + -1078 = ");
    assertOperations("4365±+9999=", "5 634", "-4365 + 9999 = ");
    assertOperations("4365±+1253±=", "-5 618", "-4365 + -1253 = ");
    assertOperations("4536.128741262576+4375.871258737424=", "8 912", "4536,128741262576 + 4375,871258737424 = ");

    assertOperations("0.2545+0.1259=", "0,3804", "0,2545 + 0,1259 = ");
    assertOperations("0.5463+0.2525±=", "0,2938", "0,5463 + -0,2525 = ");
    assertOperations("0.2643±+0.8785=", "0,6142", "-0,2643 + 0,8785 = ");
    assertOperations("0.2643±+0.2516±=", "-0,5159", "-0,2643 + -0,2516 = ");

    assertOperations("20000001+43475646=", "63 475 647", "20000001 + 43475646 = ");
    assertOperations("54635745+23543333±=", "31 092 412", "54635745 + -23543333 = ");
    assertOperations("23256456.63254756±+42535353.25342654=", "19 278 896,62087898", "-23256456,63254756 + 42535353,25342654 = ");
    assertOperations("55111111.22222222+12545455.12121212±=", "42 565 656,1010101", "55111111,22222222 + -12545455,12121212 = ");
    assertOperations("12545455.12121212±+55111111.22222222=", "42 565 656,1010101", "-12545455,12121212 + 55111111,22222222 = ");
    assertOperations("21234978.41325465±+12354734.56123461±=", "-33 589 712,97448926", "-21234978,41325465 + -12354734,56123461 = ");

    assertOperations("0.20000011+0.59123462=", "0,79123473", "0,20000011 + 0,59123462 = ");
    assertOperations("0.35463288+0.23849577±=", "0,11613711", "0,35463288 + -0,23849577 = ");
    assertOperations("0.55551111±+0.74379501=", "0,1882839", "-0,55551111 + 0,74379501 = ");
    assertOperations("0.21376591±+0.54632837±=", "-0,76009428", "-0,21376591 + -0,54632837 = ");

    assertOperations("314234123414+523362452721=", "837 596 576 135", "314234123414 + 523362452721 = ");
    assertOperations("919191919191+234512442314±=", "684 679 476 877", "919191919191 + -234512442314 = ");
    assertOperations("121212121212±+245342543264=", "124 130 422 052", "-121212121212 + 245342543264 = ");
    assertOperations("215341254323.1253+251453412142.1253=", "466 794 666 465,2506", "215341254323,1253 + 251453412142,1253 = ");

    assertOperations("0.523404009123+0.222369234561=", "0,745773243684", "0,523404009123 + 0,222369234561 = ");
    assertOperations("0.999999999999+0.124337489192±=", "0,875662510807", "0,999999999999 + -0,124337489192 = ");
    assertOperations("0.121212121212±+0.233546298135=", "0,112334176923", "-0,121212121212 + 0,233546298135 = ");

    assertOperations("7678676445478.655±+7678676445478.655=", "0", "-7678676445478,655 + 7678676445478,655 = ");
    assertOperations("7678676445.478655±+7678676445.478655=", "0", "-7678676445,478655 + 7678676445,478655 = ");
    assertOperations("7678676.445478655±+7678676.445478655=", "0", "-7678676,445478655 + 7678676,445478655 = ");
    assertOperations("7678.676445478655±+7678.676445478655=", "0", "-7678,676445478655 + 7678,676445478655 = ");
    assertOperations("7.678676445478655±+7.678676445478655=", "0", "-7,678676445478655 + 7,678676445478655 = ");

    assertOperations("100000000²²+300000000²²=", "8,2e+33", "sqr(sqr(100000000)) + sqr(sqr(300000000)) = ");
    assertOperations("100000000²²²²+100%=", "2e+128", "sqr(sqr(sqr(sqr(100000000)))) + 1e+128 = ");
    assertOperations("100000000²²²²²²²+100%=", "2e+1024", "sqr(sqr(sqr(sqr(sqr(sqr(sqr(100000000))))))) + 1e+1024 = ");

    assertOperations("0.001±=", "-0,001", "-0,001 = ");
    assertOperations("0.001±+0.000000001²=", "-9,99999999999999e-4", "-0,001 + sqr(0,000000001) = ");
    assertOperations("0.001±+0.000000001²==", "-9,99999999999998e-4", "-9,99999999999999e-4 + sqr(0,000000001) = ");

    assertOperations("0.01±=", "-0,01", "-0,01 = ");
    assertOperations("0.01±+0.00000001²=", "-0,0099999999999999", "-0,01 + sqr(0,00000001) = ");
    assertOperations("0.01±+0.00000001²==", "-0,0099999999999998", "-0,0099999999999999 + sqr(0,00000001) = ");

    assertOperations("0.1±=", "-0,1", "-0,1 = ");
    assertOperations("0.1±+0.00000001²=", "-0,0999999999999999", "-0,1 + sqr(0,00000001) = ");
    assertOperations("0.1±+0.00000001²==", "-0,0999999999999998", "-0,0999999999999999 + sqr(0,00000001) = ");

    assertOperations("9999999999999999+9999999999999999=", "2e+16", "9999999999999999 + 9999999999999999 = ");
    assertOperations("9999999999999998+9999999999999999=", "2e+16", "9999999999999998 + 9999999999999999 = ");
    assertOperations("9999999999999999+9999999999999998=", "2e+16", "9999999999999999 + 9999999999999998 = ");

    assertOperations("0.0000000000000001+0.0000000000000001=", "0,0000000000000002", "0,0000000000000001 + 0,0000000000000001 = ");
    assertOperations("0.0000000000000001+0.0000000000000002=", "0,0000000000000003", "0,0000000000000001 + 0,0000000000000002 = ");
    assertOperations("0.0000000000000002+0.0000000000000001=", "0,0000000000000003", "0,0000000000000002 + 0,0000000000000001 = ");

    assertOperations("1±=", "-1", "-1 = ");
    assertOperations("1±+0.00000001²=", "-0,9999999999999999", "-1 + sqr(0,00000001) = ");
    assertOperations("1±+0.00000001²==", "-0,9999999999999998", "-0,9999999999999999 + sqr(0,00000001) = ");

    assertOperations("9999999999999999+0.4999999999999998+0.0000000000000001+", "9 999 999 999 999 999", "9999999999999999 + 0,4999999999999998 + 0,0000000000000001 + ");
    assertOperations("9999999999999999+0.4999999999999999+0.0000000000000001+", "1e+16", "9999999999999999 + 0,4999999999999999 + 0,0000000000000001 + ");
    assertOperations("9999999999999999+0.5+0.0000000000000001=", "1e+16", "9999999999999999 + 0,5 + 0,0000000000000001 = ");

    assertOperations("9999999999999999±+0.3±+0.19±+", "-9 999 999 999 999 999", "-9999999999999999 + -0,3 + -0,19 + ");
    assertOperations("9999999999999999±+0.3±+0.19±+0.01±+", "-1e+16", "-9999999999999999 + -0,3 + -0,19 + -0,01 + ");
    assertOperations("9999999999999999±+0.3±+0.19±+0.01±+0.01±=", "-1e+16", "-9999999999999999 + -0,3 + -0,19 + -0,01 + -0,01 = ");

    for (int i = 0; i < 20; i++) {
      BigDecimal firstNum = new BigDecimal(randomNumber());
      BigDecimal secondNum = new BigDecimal(randomNumber());
      String history = OutputFormat.outputFormat(firstNum, "") + " + " + OutputFormat.outputFormat(secondNum, "") + " = ";
      String result = OutputFormat.outputFormat(firstNum.add(secondNum, CEILING), " ");
      log.info(firstNum + " * " + secondNum + " = " + result);
      assertOperations(firstNum.toString() + '+' + secondNum.toString() + '=', result, history);
    }
    log.info("addition tests status: passed");
  }

  @Test
  void squareRootTests() {
    log.info("squareRoot tests status: start");
    assertOperations("0√", "0", "√(0) ");
    assertOperations("1√", "1", "√(1) ");

    assertOperations("47√", "6,855654600401044", "√(47) ");
    assertOperations("345√", "18,57417562100671", "√(345) ");
    assertOperations("8546√", "92,44457799135653", "√(8546) ");
    assertOperations("17424√", "132", "√(17424) ");
    assertOperations("361237√", "601,0299493369694", "√(361237) ");
    assertOperations("9999999999999999√√", "10 000", "√(√(9999999999999999)) ");

    assertOperations("0.56√", "0,7483314773547883", "√(0,56) ");
    assertOperations("0.463√", "0,68044103344816", "√(0,463) ");
    assertOperations("0.4389√", "0,6624952830020754", "√(0,4389) ");
    assertOperations("0.42637√", "0,6529701371425802", "√(0,42637) ");
    assertOperations("0.053361√", "0,231", "√(0,053361) ");

    assertOperations("9999999999999999*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=√", "9,999999999999999e+4999", "√(9,999999999999999e+9999) ");
    assertOperations("9000000000000000*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=√", "9,486832980505138e+4999", "√(9e+9999) ");

    assertOperations("1±√", "Invalid input", "√(-1) ");
    assertOperations("9999999999999±√", "Invalid input", "√(-9999999999999) ");

    for (int i = 0; i < 20; i++) {
      BigDecimal num = new BigDecimal(randomNumber());
      String history = "√(" + OutputFormat.outputFormat(num, "") + ") ";
      String result = OutputFormat.outputFormat(num.sqrt(HALF_UP), " ");
      log.info(history + " = " + result);
      assertOperations(num.toString() + '√', result, history);
    }
    log.info("squareRoot tests status: passed");
  }

  @Test
  void percentageTests() {
    assertOperations("20*20%=", "4", "20 * 0,2 = ");
    assertOperations("20/40%=", "50", "20 / 0,4 = ");
    assertOperations("50+10%=", "55", "50 + 5 = ");
    assertOperations("480-45%=", "264", "480 - 216 = ");
    assertOperations("500=30%", "0", "0 ");
    assertOperations("52²*10%=", "270,4", "sqr(52) * 0,1 = ");

    assertOperations("4+5%=", "4,2", "4 + 0,2 = ");
    assertOperations("54+48%=", "79,92", "54 + 25,92 = ");
    assertOperations("7843+1392%=", "117 017,56", "7843 + 109174,56 = ");
    assertOperations("789214+904567%=", "7 139 758 617,38", "789214 + 7138969403,38 = ");

    assertOperations("5+2±%=", "4,9", "5 + -0,1 = ");
    assertOperations("23+92±%=", "1,84", "23 + -21,16 = ");
    assertOperations("2348+9213±%=", "-213 973,24", "2348 + -216321,24 = ");
    assertOperations("643209+763212±%=", "-4 908 405 064,08", "643209 + -4909048273,08 = ");

    assertOperations("6-9%=", "5,46", "6 - 0,54 = ");
    assertOperations("88-43%=", "50,16", "88 - 37,84 = ");
    assertOperations("2438-9011%=", "-217 250,18", "2438 - 219688,18 = ");
    assertOperations("634857-432785%=", "-2 746 931 010,45", "634857 - 2747565867,45 = ");

    assertOperations("6*4%=", "0,24", "6 * 0,04 = ");
    assertOperations("43*99%=", "42,57", "43 * 0,99 = ");
    assertOperations("5676*9761%=", "554 034,36", "5676 * 97,61 = ");
    assertOperations("745863*992105%=", "7 399 744 116,15", "745863 * 9921,05 = ");

    assertOperations("6/2%=", "300", "6 / 0,02 = ");
    assertOperations("77/21%=", "366,6666666666667", "77 / 0,21 = ");
    assertOperations("5432/3456%=", "157,1759259259259", "5432 / 34,56 = ");
    assertOperations("753494/554545%=", "135,8760785869497", "753494 / 5545,45 = ");

    assertOperations("0.4+0.5%=", "0,402", "0,4 + 0,002 = ");
    assertOperations("0.12+0.02%=", "0,120024", "0,12 + 0,000024 = ");
    assertOperations("0.2364+0.6565%=", "0,237951966", "0,2364 + 0,001551966 = ");
    assertOperations("0.298181+0.451199%=", "0,29952638969019", "0,298181 + 0,00134538969019 = ");

    assertOperations("0.5*0.1%=", "0,0005", "0,5 * 0,001 = ");
    assertOperations("0.47*0.99%=", "0,004653", "0,47 * 0,0099 = ");
    assertOperations("0.6234*0.3123%=", "0,0019468782", "0,6234 * 0,003123 = ");
    assertOperations("0.543246*0.458273%=", "0,00248954974158", "0,543246 * 0,00458273 = ");

    assertOperations("9999999999999999+0.4*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+0.000000000000001%=", "9,999999999999999e+9999", "9999999999999999 + 0,4 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + 9,999999999999999e+9982 = ");
    assertOperations("9999999999999999+0.4*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+0.000000000000002%=", "Overflow", "9999999999999999 + 0,4 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + 2e+9983 = ");
    assertOperations("9999999999999999+0.4*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+0.000000000000003%=", "Overflow", "9999999999999999 + 0,4 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + 3e+9983 = ");

    assertOperations("9999999999999999±-0.4*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+0.000000000000001%=", "-9,999999999999999e+9999", "-9999999999999999 - 0,4 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + -9,999999999999999e+9982 = ");
    assertOperations("9999999999999999±-0.4*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+0.000000000000002%=", "Overflow", "-9999999999999999 - 0,4 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + -2e+9983 = ");
    assertOperations("9999999999999999±-0.4*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²+0.000000000000003%=", "Overflow", "-9999999999999999 - 0,4 * sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))))) * sqr(sqr(sqr(sqr(sqr(sqr(sqr(1000000000000000))))))) * sqr(sqr(sqr(sqr(sqr(1000000000000))))) + -3e+9983 = ");

    assertOperations("9999999999999999+0.49+100%", "9 999 999 999 999 999", "9999999999999999 + 0,49 + 9999999999999999 ");
    assertOperations("9999999999999999+0.5+100%", "1e+16", "9999999999999999 + 0,5 + 1e+16 ");
    assertOperations("9999999999999999+0.51+100%", "1e+16", "9999999999999999 + 0,51 + 1e+16 ");

    assertOperations("1+0.00000000000001%=", "1", "1 + 0,0000000000000001 = ");
    assertOperations("1+0.000000000000009%", "9e-17", "1 + 9e-17 ");
    assertOperations("1+0.000000000000008%", "8e-17", "1 + 8e-17 ");
  }

  @Test
  void oneDivideByX() {
    log.info("oneDividedByX tests status: start");
    assertOperations("0&", "Cannot divide by zero", "1/(0) ");
    assertOperations("1&", "1", "1/(1) ");
    assertOperations("4&", "0,25", "1/(4) ");
    assertOperations("44&", "0,0227272727272727", "1/(44) ");
    assertOperations("643&", "0,0015552099533437", "1/(643) ");
    assertOperations("4632&", "2,158894645941278e-4", "1/(4632) ");
    assertOperations("23546&", "4,247005860868088e-5", "1/(23546) ");
    assertOperations("500000&", "0,000002", "1/(500000) ");

    assertOperations("1±&", "-1", "1/(-1) ");
    assertOperations("47±&", "-0,0212765957446809", "1/(-47) ");
    assertOperations("12±&", "-0,0833333333333333", "1/(-12) ");
    assertOperations("342±&", "-0,0029239766081871", "1/(-342) ");
    assertOperations("500±&", "-0,002", "1/(-500) ");
    assertOperations("12378±&", "-8,078849571820973e-5", "1/(-12378) ");
    assertOperations("723465±&", "-1,382236873933086e-6", "1/(-723465) ");
    assertOperations("8000000±&", "-0,000000125", "1/(-8000000) ");

    assertOperations("0.4&", "2,5", "1/(0,4) ");
    assertOperations("0.56&", "1,785714285714286", "1/(0,56) ");
    assertOperations("0.436&", "2,293577981651376", "1/(0,436) ");
    assertOperations("0.3245&", "3,081664098613251", "1/(0,3245) ");
    assertOperations("0.12537&", "7,976389885937625", "1/(0,12537) ");
    assertOperations("0.000002&", "500 000", "1/(0,000002) ");
    assertOperations("0.0000025&", "400 000", "1/(0,0000025) ");
    assertOperations("0.23546143&", "4,246980068030675", "1/(0,23546143) ");

    assertOperations("0.23±&", "-4,347826086956522", "1/(-0,23) ");
    assertOperations("0.423±&", "-2,364066193853428", "1/(-0,423) ");
    assertOperations("0.0101±&", "-99,00990099009901", "1/(-0,0101) ");
    assertOperations("0.02319±&", "-43,122035360069", "1/(-0,02319) ");
    assertOperations("0.020374±&", "-49,08216354176892", "1/(-0,020374) ");
    assertOperations("0.2222222±&", "-4,500000450000045", "1/(-0,2222222) ");
    assertOperations("0.00000004±&", "-25 000 000", "1/(-0,00000004) ");

    assertOperations("6.9&", "0,1449275362318841", "1/(6,9) ");
    assertOperations("66.23&", "0,015098897780462", "1/(66,23) ");
    assertOperations("783.235&", "0,0012767560183087", "1/(783,235) ");
    assertOperations("7328.1235&", "1,364605823032322e-4", "1/(7328,1235) ");
    assertOperations("64723.23489&", "1,545040203413108e-5", "1/(64723,23489) ");
    assertOperations("758345.245273&", "1,318660605091558e-6", "1/(758345,245273) ");
    assertOperations("9234796.5739458&", "1,08286088598996e-7", "1/(9234796,5739458) ");

    assertOperations("4.2±&", "-0,2380952380952381", "1/(-4,2) ");
    assertOperations("23.34±&", "-0,0428449014567267", "1/(-23,34) ");
    assertOperations("524.662±&", "-0,0019059889986315", "1/(-524,662) ");
    assertOperations("3245.1239±&", "-3,081546439567377e-4", "1/(-3245,1239) ");
    assertOperations("65347.78456±&", "-1,53027376020963e-5", "1/(-65347,78456) ");
    assertOperations("236457.573486±&", "-4,22908847983762e-6", "1/(-236457,573486) ");
    assertOperations("2452364.3584365±&", "-4,077697494500972e-7", "1/(-2452364,3584365) ");

    assertOperations("0.0000000000000001&", "1e+16", "1/(0,0000000000000001) ");
    assertOperations("0.0000000000000001&&", "0,0000000000000001", "1/(1/(0,0000000000000001)) ");
    assertOperations("0.0000000000000001&&&", "1e+16", "1/(1/(1/(0,0000000000000001))) ");
    assertOperations("0.0000000000000001&&&&", "0,0000000000000001", "1/(1/(1/(1/(0,0000000000000001)))) ");

    assertOperations("9999999999999999&", "0,0000000000000001", "1/(9999999999999999) ");
    assertOperations("9999999999999999&&", "9 999 999 999 999 999", "1/(1/(9999999999999999)) ");
    assertOperations("9999999999999999&&&", "0,0000000000000001", "1/(1/(1/(9999999999999999))) ");

    assertOperations("9999999999999999&&&&", "9 999 999 999 999 999", "1/(1/(1/(1/(9999999999999999)))) ");
    assertOperations("0.0000000000000001&&&&", "0,0000000000000001", "1/(1/(1/(1/(0,0000000000000001)))) ");

    assertOperations("1000000000000000+0.05*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=&", "1e-9999", "1/(1e+9999) ");
    assertOperations("1000000000000000+0.06*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=&", "Overflow", "1/(1e+9999) ");
    assertOperations("1000000000000000+0.07*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=&", "Overflow", "1/(1e+9999) ");

    assertOperations("1000000000000000±-0.05*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=&", "-1e-9999", "1/(-1e+9999) ");
    assertOperations("1000000000000000±-0.06*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=&", "Overflow", "1/(-1e+9999) ");
    assertOperations("1000000000000000±-0.07*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²=&", "Overflow", "1/(-1e+9999) ");

    assertOperations("0.0000000000000009/10=&", "1,111111111111111e+16", "1/(9e-17) ");
    assertOperations("0.000000000000001/10=&", "1e+16", "1/(0,0000000000000001) ");
    assertOperations("0.0000000000000011/10=&", "9 090 909 090 909 091", "1/(1,1e-16) ");

    assertOperations("9999999999999995+0.1=&", "0,0000000000000001", "1/(9999999999999995) ");
    assertOperations("9999999999999995&", "1,000000000000001e-16", "1/(9999999999999995) ");
    assertOperations("9999999999999994+0.9=&", "1,000000000000001e-16", "1/(9999999999999995) ");

    for (int i = 0; i < 20; i++) {
      BigDecimal firstNum = new BigDecimal(randomNumber());
      String history = "1/(" + OutputFormat.outputFormat(firstNum.toString(), "") + ") ";
      String result = OutputFormat.outputFormat(BigDecimal.ONE.divide(firstNum, CEILING), " ");
      log.info(history + " = " + result);
      assertOperations(firstNum.toString() + '&', result, history);
    }
    log.info("oneDividedByX tests status: passed");
  }

  @Test
  void negateTests() {
    assertOperations("0±", "0", "");
    assertOperations("0±±", "0", "");
    assertOperations("0.±", "-0,", "");
    assertOperations("0.±±", "0,", "");

    assertOperations("5±", "-5", "");
    assertOperations("34±", "-34", "");
    assertOperations("2341±", "-2 341", "");
    assertOperations("782461±", "-782 461", "");

    assertOperations("0.3±", "-0,3", "");
    assertOperations("0.43±", "-0,43", "");
    assertOperations("0.6712±", "-0,6712", "");
    assertOperations("0.236471±", "-0,236471", "");

    assertOperations("5²±", "-25", "negate(sqr(5)) ");
    assertOperations("23²±", "-529", "negate(sqr(23)) ");
    assertOperations("9344²±", "-87 310 336", "negate(sqr(9344)) ");
    assertOperations("127632²±", "-16 289 927 424", "negate(sqr(127632)) ");

    assertOperations("0.1²±", "-0,01", "negate(sqr(0,1)) ");
    assertOperations("0.41²±", "-0,1681", "negate(sqr(0,41)) ");
    assertOperations("0.7823²±", "-0,61199329", "negate(sqr(0,7823)) ");
    assertOperations("0.891235²±", "-0,794299825225", "negate(sqr(0,891235)) ");

    assertOperations("3&±", "-0,3333333333333333", "negate(1/(3)) ");
    assertOperations("425&±", "-0,0023529411764706", "negate(1/(425)) ");
    assertOperations("12345&±", "-8,100445524503848e-5", "negate(1/(12345)) ");
    assertOperations("6534829&±", "-1,530261924221736e-7", "negate(1/(6534829)) ");

    assertOperations("0.2&±", "-5", "negate(1/(0,2)) ");
    assertOperations("0.812&±", "-1,231527093596059", "negate(1/(0,812)) ");
    assertOperations("0.92912&±", "-1,076287239538488", "negate(1/(0,92912)) ");
    assertOperations("0.7123641&±", "-1,403776523831002", "negate(1/(0,7123641)) ");


    assertOperations("6√±", "-2,449489742783178", "negate(√(6)) ");
    assertOperations("49√±", "-7", "negate(√(49)) ");
    assertOperations("6437√±", "-80,23091673413685", "negate(√(6437)) ");
    assertOperations("454632√±", "-674,2640432352892", "negate(√(454632)) ");



    assertOperations("9999999999999999±", "-9 999 999 999 999 999", "");
    assertOperations("0.0000000000000001±", "-0,0000000000000001", "");
    assertOperations("9999999999999999±±", "9 999 999 999 999 999", "");
    assertOperations("9999999999999999.±", "-9 999 999 999 999 999,", "");
    assertOperations("9999999999999999.±±", "9 999 999 999 999 999,", "");
    assertOperations("5±+±=", "0", "-5 + negate(-5) = ");
  }

  @Test
    // E - CLEAR_ENTRY
  void clearEntryTests() {
    assertOperations("8+", "8", "8 + ");
    assertOperations("8+E", "0", "8 + ");
    assertOperations("8+5²", "25", "8 + sqr(5) ");
    assertOperations("8+5²E", "0", "8 + ");
    assertOperations("8+5²±E", "0", "8 + ");
  }

  @Test
    // L - CLEAR_LAST
  void removeTests() {
    assertOperations("LL", "0", "");
    assertOperations("5²L", "25", "sqr(5) ");
    assertOperations("5+L", "5", "5 + ");
    assertOperations("12345LLLL", "1", "");
  } 

  // C - Memory Clear
  // R - Memory Recall
  // P - Memory Plus
  // M - Memory Minus 1e9983
  // S - Memory Save
  // V - Memory Show
  @Test
  void memoryTests() {
    assertOperations("S5R", "0", "");


    assertOperations("9999999999999999+0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²-1*10+8=SAR+1=", "9,999999999999999e+9999", "9,999999999999999e+9999 + 1 = ");
    assertOperations("9999999999999999+0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²-1*10+8=SAR+2=", "9,999999999999999e+9999", "9,999999999999999e+9999 + 2 = ");
    assertOperations("9999999999999999+0.5/10*1000000000000000²²²²²²²²²*1000000000000000²²²²²²²*1000000000000²²²²²-1*10+8=SAR+3=", "Overflow", "9,999999999999999e+9999 + 3 = ");
  }
}
