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

import java.text.DecimalFormat;

import com.google.common.base.Predicate;

/**
 * @author Kasper Luckow
 * TODO: check that the output of getFunction is correct
 */
public class PolyTrendLine extends OLSTrendLine {
  private final int degree;
  public PolyTrendLine(int degree) {
    if (degree < 0) throw new IllegalArgumentException("The degree of the polynomial must not be negative");
    this.degree = degree;
  }
  protected double[] xVector(double x) { // {1, x, x*x, x*x*x, ...}
    double[] poly = new double[degree+1];
    double xi=1;
    for(int i=0; i<=degree; i++) {
      poly[i]=xi;
      xi*=x;
    }
    return poly;
  }
  @Override
  protected boolean logY() {return false;}
  
  @Override
  public String getFunction() {
    StringBuilder functionSb = new StringBuilder();
    DecimalFormat df = new DecimalFormat("#.00");
    for(int i=0; i<=degree; i++) {
      double coef = super.coef.getColumn(0)[i];
      if(coef == 0.0d) //we skip coefficients that are 0
        continue;
      functionSb.append(df.format(coef));
      if(i > 0) {
        functionSb.append("x");
        if(i > 1) {
          functionSb.append("^" + i);
        }
      }
      if(i < degree) {
        functionSb.append(" + ");
      }
    }
    return functionSb.toString();
  }


  @Override
  public Predicate<Double> getDomainPredicate() {
    return new Predicate<Double>() {
      @Override
      public boolean apply(Double arg0) {
        return true;
      }
    };
  }

  @Override
  public Predicate<Double> getRangePredicate() {
    return new Predicate<Double>() {
      @Override
      public boolean apply(Double arg0) {
        return true;
      }
    };
  }
}
