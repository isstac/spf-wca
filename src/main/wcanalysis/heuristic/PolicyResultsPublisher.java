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

package wcanalysis.heuristic;

import java.io.File;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.report.Reporter;
import wcanalysis.heuristic.util.Util;

/**
 * @author Kasper Luckow
 *
 */
public class PolicyResultsPublisher extends ResultsPublisher {

  public static final String RESULTS_DIR_CONF = "report.console.wc.policy.resultsdir";
  
  public PolicyResultsPublisher(Config conf, Reporter reporter) {
    super(conf, reporter);
  }

  @Override
  protected String getCustomCSVColumnHeader() {
    return "newChoicesNum";
  }

  @Override
  protected String getCustomCSVColumnData() {
    PolicyGeneratorListener listener = reporter.getVM().getNextListenerOfType(PolicyGeneratorListener.class, null);
    if(listener == null)
      return "";
    return Long.toString(listener.getNumberOfNewChoices());
  }

  @Override
  protected File getResultsDir(Config conf) {
    return Util.createDirIfNotExist(conf.getString(RESULTS_DIR_CONF, "./results/policy"));
  }
  
  @Override
  protected PathListener getListener() {
    return reporter.getVM().getNextListenerOfType(PolicyGeneratorListener.class, null);
  }
}
