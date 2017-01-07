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

package wcanalysis.charting;

/**
 * @author Kasper Luckow
 */
public class DataSeries {
  private DataCollection data = new DataCollection();
  private final String seriesName;
  private String r2;
  private String function;

  public DataSeries(String seriesName) {
    this.seriesName = seriesName;
  }


  public String getSeriesName() {
    return seriesName;
  }

  public void add(double x, double y) {
    this.data.addDatapoint(x, y);
  }

  public double[] getY() {
    return this.data.getY();
  }

  public double[] getX() {
    return this.data.getX();
  }

  public int size() {
    return this.data.size();
  }

  public String getR2() {
    return r2;
  }

  public void setR2(String r2) {
    this.r2 = r2;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }
}
