package wcanalysis.heuristic.util;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import wcanalysis.heuristic.HeuristicListener;
import wcanalysis.heuristic.PolicyGeneratorListener;
import wcanalysis.heuristic.PolicyResultsPublisher;
import wcanalysis.heuristic.ResultsPublisher;

public class AllPoliciesExtractor implements JPFShell {

  private static final String SIZE_FROM = "policies.inputsize.from";
  private static final String SIZE_TO = "policies.inputsize.to";
  
  private final int from;
  private final int to;
  
  private Config conf;
  
  public AllPoliciesExtractor(Config config) {
    this.from = config.getInt(SIZE_FROM,1);
    this.to = config.getInt(SIZE_TO,1);
    this.conf = config;
  }
  
  @Override
  public void start(String[] args) {
    this.conf.setProperty("report.console.class", PolicyResultsPublisher.class.getName());
    for(int i = this.from; i <= this.to; i++) {
      this.conf.setProperty("target.args", ""+i);
      JPF jpf = new JPF(this.conf);
      jpf.addListener(new PolicyGeneratorListener(this.conf, jpf)); //weird instantiation...
      //get policy
      jpf.run();
    }
  }
}
