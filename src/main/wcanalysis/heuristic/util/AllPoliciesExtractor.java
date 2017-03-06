/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
