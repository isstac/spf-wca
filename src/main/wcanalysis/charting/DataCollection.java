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

import java.util.ArrayList;

/**
 * @author Kasper Luckow
 */
public class DataCollection {
  
  private ArrayList<Double> xs = new ArrayList<>();
  private ArrayList<Double> ys = new ArrayList<>();
  
  public void addDatapoint(double x, double y) {
    this.xs.add(x);
    this.ys.add(y);
  }
  
  public double[] getX() {
    return convert(xs);
  }
  
  public double[] getY() {
    return convert(ys);
  }
  
  private static double[] convert(ArrayList<Double> d) {
    double[] dc = new double[d.size()];
    for(int i=0; i < d.size(); i++)
      dc[i] = d.get(i);
    return dc;
  }
  
  public int size() {
    return xs.size();
  }
  
}
