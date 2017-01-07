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

package wcanalysis.heuristic;

import java.io.File;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.report.Reporter;
import wcanalysis.heuristic.util.Util;

/**
 * @author Kasper Luckow
 */
public class HeuristicResultsPublisher extends ResultsPublisher {
  
  public static final String RESULTS_DIR_CONF = "report.console.wc.heuristic.resultsdir";
  
  public HeuristicResultsPublisher(Config conf, Reporter reporter) {
    super(conf, reporter);
  }

  @Override
  protected String getCustomCSVColumnHeader() {
    return "resolvedChoicesNum,resolvedPerfectChoicesNum,resolvedHistoryChoicesNum,resolvedInvariantChoicesNum,unresolvedChoicesNum,newChoicesNum";
  }

  @Override
  protected String getCustomCSVColumnData() {
    HeuristicListener listener = reporter.getVM().getNextListenerOfType(HeuristicListener.class, null);
    if(listener == null)
      return "";

    HeuristicStatistics statistics = listener.getStatistics();
    String out = (statistics.resolvedHistoryChoices + statistics.resolvedInvariantChoices +
        statistics.resolvedPerfectChoices) + "," +
        statistics.resolvedPerfectChoices + "," +
        statistics.resolvedHistoryChoices + "," +
        statistics.resolvedInvariantChoices + "," +
        statistics.unresolvedChoices + "," +
        statistics.newChoices;
    return out;
  }
  
  @Override
  protected File getResultsDir(Config conf) {
    return Util.createDirIfNotExist(conf.getString(RESULTS_DIR_CONF, "./results/heuristic"));
  }

  @Override
  protected PathListener getListener() {
    return reporter.getVM().getNextListenerOfType(HeuristicListener.class, null);
  }
}
