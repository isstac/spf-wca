# SPF WCA
This JPF module implements SPF WCA and SPF SCA.

## Usage 
The Java PathFinder shell `wcanalysis.WorstCaseAnalyzer` can be used to set up Phase 1 (policy generation) and Phase 2 (guided search) of the analysis.
The analysis can be performed by executing the JPF config file that specifies the parameters of the analysis, the constraint solver, the entry point of the system under analysis etc:

```bash
$ ./jpf-core/bin/jpf <path-to-jpf-file>
```

This will generate a results folder according to the option `symbolic.worstcase.outputpath` (see below).

In the results folder, a subfolder `verbose/heuristic/` will be generated that contains the constraints found for the analyzed input size. In addition a csv file is produced which summarizes all the results.
**Note** that consecutive runs of the analysis will produced and new data set that is **appended** to this file. 

The csv file includes various statistics:

* **inputSize** Current input size to the SUT. It assumes this information is available in the jpf file as the value of the `target.args` property (it is for the examples in benchmarks.heuristic)
* **wcInstrExec** Shows how many instructions were executed on the worst case path
* **cfgInputSize** Specifies which input size was used for generating the annotated CFG used for the heuristic. This will by default not be added to the .csv file
* **analysisTime** The analysis time when using the heuristic
* **mem** The peak memory consumption during heuristic-based exploration
* **depth** The maximum depth recorded (which constitutes the worst case path)
* **paths** Number of paths explored with the search heuristic
* **resolvedChoicesNum** How many decisions that were resolved as a consequence of using the heuristic
* **unresolvedChoicesNum** How many decisions that could not be resolved by the heuristic
* **newChoicesNum** New decisions encountered during exploration for which the heuristic has no information
* **wcConstraint** The constraint recorded for the worst case path. Any solution to this provides test inputs that are guaranteed to exercise worst case behavior of the SUT

In addition, data points <inputSize, depth> (here depth is the notion of worst case for a path) are generated, and the shell will automatically generate a plot showing the raw data, and the various fitted functions based on regression analysis. The user can zoon in on the graph by highlighting a region.

## Configuration
The following must be supplied in jpf file (or imported from another jpf file using the `@include` directive):

```bash
@using jpf-security
@using jpf-symbc

shell=wcanalysis.WorstCaseAnalyzer

classpath=${jpf-security}/build/examples
target=fully.qualified.name.of.target.class

symbolic.worstcase.policy.inputsize=XXX
symbolic.worstcase.input.max=YYY

symbolic.wc.policy.history.size=ZZZ

```
The `classpath` variable should be updated according to the system under test. `target` denotes the entry point of the system under test.

Replace `XXX` with the input size at which the policy should be obtained. This corrsponds to phase 1. Replace `YYY` with the maximum input size at which the heuristic search (phase 2) should be run. The heuristic search will run from input size 1-`YYY`.

`symbolic.wc.policy.history.size` is important because it controls whether the guidance policy produced in phase 1 is memoryless or history-based. By setting this variable to 0, a memoryless policy is used; otherwise, a history with the specified size `ZZZ` will be used.

## Optional Configuration
In addition, the user can optionally use the following for the `WorstCaseAnalyzer` shell:

* **```symbolic.heuristic.measuredmethods=<method desc(s)>```**  A list (separated by semicolons) that specifies from which method(s), the value for the worst case path should start counting. Default is the value of ```symbolic.method``` i.e. the "symbolic target method".

* **```symbolic.worstcase.verbose=<true | false>```** This will generate verbose output e.g. analysis statistics. 

* **```symbolic.worstcase.outputpath=<path>```** This will output the contraints for each worst case path and in addition summarize analysis statistics in a csv file. See above.

* **```symbolic.worstcase.req.maxinputsize=<Number>```** Plot the budget input size from the requirement

* **```symbolic.worstcase.req.maxres=<Number>```** Plot the budget max resource size from the requirement

* **```symbolic.worstcase.predictionmodel.size=<Number>```** The maximum plotted domain of the fitting functions.

* **```symbolic.worstcase.reusepolicy=<true | false>```** By setting this to true, a computed policy will be reused if it has been previously computed.
