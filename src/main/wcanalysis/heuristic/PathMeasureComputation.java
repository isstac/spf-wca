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

/**
 * @author Kasper Luckow
 *
 */
public interface PathMeasureComputation {
  
  //TODO: This is really ugly. This should be fixed when we decide
  //on the pathmeasure computation
  public static class Result {
    private final int resolutions;
    private final int memorylessResolution;
    
    public Result(int resolutions, int memorylessResolutions) {
      this.resolutions = resolutions;
      this.memorylessResolution = memorylessResolutions;
    }

    public int getMemorylessResolution() {
      return memorylessResolution;
    }

    public int getResolutions() {
      return resolutions;
    }
  }
  
  public Result compute(WorstCasePath path);
}
