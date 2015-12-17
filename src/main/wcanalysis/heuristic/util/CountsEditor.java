package wcanalysis.heuristic.util;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import att.grappa.Graph;
import isstac.structure.cfg.Block;
import isstac.structure.cfg.CFG;
import isstac.structure.cfg.util.CFGToDOT;
import isstac.structure.serialize.JavaSerializer;

/**
 * @author Kasper Luckow
 * 
 */
public class CountsEditor {

  private static Options options;
  static {
    options = new Options();
    Option inputOpt = new Option("i", "input", true, "Input serialized CFG (.ser extension)");
    inputOpt.setRequired(true);
    options.addOption(inputOpt);
    Option outputDir = new Option("o", "output", true, "Output file of edited CFG (optional)");
    options.addOption(outputDir);
    Option genDot = new Option("v", "visualize", false, "Generate dot file (and pdf) when saving CFG");
    options.addOption(genDot);
  }
  
  private static enum BRANCH {
    TRUE,
    FALSE;
  }
  
  private static final Pattern editPattern = Pattern.compile("([0-9]+)\\s+([TFtf]+)\\s+([0-9]+)");

  public static void main(String[] args) throws IOException, URISyntaxException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    }
    catch(ParseException exp) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Counts editor", options);
      System.exit(-1);
    }
    assert cmd != null;
    String inputCFGFileName = cmd.getOptionValue("i");
    CFG inputCFG = null;
    try(InputStream cfgIn = new FileInputStream(inputCFGFileName)) {
      inputCFG = new JavaSerializer().deserialize(cfgIn, CFG.class);
    }
    String outputFileName = cmd.hasOption("o") ? cmd.getOptionValue("o") : inputCFGFileName;
    
    startInteractiveEditSession(inputCFG, outputFileName, cmd.hasOption("v"));
  }
  
  private static void startInteractiveEditSession(CFG inputCFG, String outputFileName, boolean genDot) {
    try(Scanner scanner = new Scanner(System.in)) {
      PrintStream out = System.out;
      String line;
      out.println(getUsageString());
      out.println("Starting interactive session:");
      out.print("> ");
      for(line = scanner.nextLine(); line != null; out.print("> "), line = scanner.nextLine()) {
        if(line.equals("quit")) {
          out.println("quitting...");
          quit();
        } else if(line.equals("save")) {
          try {
            save(inputCFG, outputFileName, genDot);
          } catch (IOException | InterruptedException e) {
            out.println("Error happened when serializing CFG " + e.getMessage());
            continue;
          }
          out.print("Saved CFG to " + outputFileName);
          if(genDot)
            out.println(" and generated dot file");
          else
            out.println();
          continue;
        }
        Matcher editMatcher = editPattern.matcher(line);
        if(editMatcher.find()) {
          long bbId = Long.parseLong(editMatcher.group(1));
          String brStr = editMatcher.group(2);
          BRANCH branch;
          if(brStr.equals("T") || brStr.equals("t"))
            branch = BRANCH.TRUE;
          else
            branch = BRANCH.FALSE;
          int count = Integer.parseInt(editMatcher.group(3));
          boolean success = updateCount(inputCFG, bbId, branch, count);
          if(!success) {
            out.println("could not update count. Did you specify correct basic block id and branch?");
            out.println(getUsageString());
          }
        } else {
          out.println(getUsageString());
        }
      }
    }
  }
  
  private static boolean updateCount(CFG cfg, long bbId, BRANCH branch, int count) {
    Block targetBlock = cfg.getBlockWithID(bbId);
    if(targetBlock == null)
      return false;/*
    VisitCount visitCount = (branch == BRANCH.TRUE) ?
                              targetBlock.getAttribute(TrueBranchCount.class) :
                              targetBlock.getAttribute(FalseBranchCount.class);
                           
    if(visitCount == null) { //maybe it didn't contain any counts on that branch -- we'll add one
      visitCount = (branch == BRANCH.TRUE) ? new TrueBranchCount(count) :
                                             new FalseBranchCount(count);
      targetBlock.setAttribute(visitCount);
    } else
      visitCount.setCount(count);*/
    return true;
  }
  
  private static void save(CFG cfg, String outputFileName, boolean genDot) throws FileNotFoundException, IOException, InterruptedException {
    JavaSerializer serializer = new JavaSerializer();
    try(FileOutputStream out = new FileOutputStream(outputFileName)) {
      serializer.serialize(cfg, out);
    }
    if(genDot) {
      CFGToDOT dotVis = new CFGToDOT();
      Graph dotGraph = dotVis.build(cfg, false);
      File outputDir = new File(outputFileName).getParentFile();
      String fileName = cfg.getFqMethodName().replaceAll("[^\\p{Alpha}]+","") + ".dot";
      File dotFile = new File(outputDir, fileName);
      dotGraph.printGraph(new FileOutputStream(dotFile));
      //this will fail on windows likely -- we just catch the exception and continue
      CFGToDOT.dot2pdf(dotFile);
    }
  }
  
  private static void quit() {
    System.exit(0);
  }
  
  private static String getUsageString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Legal commands: \n").
       append("\t<Basic block id> <T|F> <count>\n").
       append("\tsave\n").
       append("\tquit\n");
    return sb.toString();
  }
}
