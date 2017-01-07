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

package wcanalysis.fitting;

import com.google.common.base.Predicate;

/**
 * @author Kasper Luckow
 * From http://stackoverflow.com/questions/17592139/trend-lines-regression-curve-fitting-java-library
 */
public interface TrendLine {

  public Predicate<Double> getDomainPredicate();
  public Predicate<Double> getRangePredicate();
  public void setValues(double[] y, double[] x);
  public double predict(double inputSize);
  public String getFunction();
  public double getRSquared();
  public double getAdjustedRSquared();
}