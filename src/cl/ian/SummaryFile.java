package cl.ian;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Ian on 02-03-2016.
 */
public class SummaryFile {
  private static final String summaryFilename = "Summary";
  private static final String summaryExtension = ".stat";
  private static final Map<Case, String> summaryNames = new EnumMap<>(Case.class);
  private static final String dateString = new SimpleDateFormat("yyyyMMdd-hhmm").format(new Date());

  public static void createSummaryFile(Case expressionName) {
    // Create a new summary file
    summaryNames.put(expressionName, String.format("%s %s %s%s", summaryFilename, expressionName,
        dateString, summaryExtension));
    try {
      new File(summaryNames.get(expressionName)).createNewFile();
      Files.write(Paths.get(summaryNames.get(expressionName)), (expressionName + "\n\n").getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      System.out.println("Couldn't create and or write to summary file");
      e.printStackTrace();
    }
  }

  public static void writeToSummary(String message, Case expressionName) {
    try {
      Files.write(Paths.get(summaryNames.get(expressionName)), message.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      System.out.println("Couldn't write summary with message: " + message);
    }
  }
}
