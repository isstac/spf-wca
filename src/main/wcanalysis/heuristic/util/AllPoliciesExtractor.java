/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
