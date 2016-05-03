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
