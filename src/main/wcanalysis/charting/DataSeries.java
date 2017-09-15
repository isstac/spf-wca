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

package wcanalysis.charting;

/**
 * @author Kasper Luckow
 */
public class DataSeries {
  private final double[] xs;
  private final double[] ys;
  int idx = 0;

  private final String seriesName;
  private String r2;
  private String function;

  public DataSeries(String seriesName, int size) {
    this.seriesName = seriesName;
    this.xs = new double[size];
    this.ys = new double[size];
  }


  public String getSeriesName() {
    return seriesName;
  }

  public void add(double x, double y) {
    xs[idx] = x;
    ys[idx] = y;
    idx++;
  }

  public double[] getY() {
    return this.ys;
  }

  public double[] getX() {
    return this.xs;
  }

  public int size() {
    return this.xs.length;
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
