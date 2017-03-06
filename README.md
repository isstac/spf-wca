# SPF WCA

## Installation
You should have `jpf-core` and `jpf-symbc` installed.

To install `spf-wca`, update your `site.properties` file (usually `~/.jpf/site.properties`) with the path to your `spf-wca` directory. 
```
spf-wca=/path/to/spf-wca
```

**Don't** add `spf-wca` to the `extensions` variable.

Make sure you have `Ivy` installed on your system. Then, obtain all the dependencies by running :
```
$ ant resolve
```
The dependencies will be downloaded to `lib/`.

Now `spf-wca` can be built by simply running:
```
$ ant build
```

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
@using spf-wca

shell=wcanalysis.WorstCaseAnalyzer

classpath=${spf-wca}/build/examples
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

# LICENSE
`spf-wca` is Copyright (c) 2017, The ISSTAC Authors and is released under the MIT License. See 
the `LICENSE` file in the root of this project and the headers of the individual files in the 
`src/` folder for the details.

`spf-wca` uses benchmarks from the WISE project by Jacob Burnim, Sudeep Juvekar, Koushik Sen. 
The benchmarks are available here [WISE-1.0.tar.gz](https://www.burn.im/pubs/WISE-1.0.tar.gz).

WISE is Copyright (c) 2011, Regents of the University of California,
and is released under an open-source BSD-style license.  See the
individual source files under `src/examples/wise` for details. A copy of the `README` file of 
WISE that includes license details can be found in the file `licenses/README.WISE`.

Benchmark code in `src/examples/wise`/*/*.java` is based on the code obtained from the WISE 
project. It is
Copyright (c) 2011, Regents of the University of California, and is
released under an open-source BSD-style license.

We repeat here the license details from the `README` file (with file paths adjusted) in the WISE 
distribution from above:

>The code in 'src/examples/wise/rbtree/` for
>red-black trees is by Tuomo Saarni, obtained from:
>
>    http://users.utu.fi/~tuiisa/Java/index.html
>
>
>under the following license:
>
>    Here's some java sources I've made. Most codes are free to
>    download. If you use some of my sources just remember give me the
>    credits.
>
>The code in src/examples/wise/java15/{util,lang}/ is
>originally from the Oracle Java (TM) 2 Platform Standard Edition
>Development Kit 5.0 Update 22, obtained and redistributed under the
>Java Research License v1.5 -- please see `licenses/JavaResearchLicense.txt` for
>details. Use and distribution of this technology is subject to the
>Java Research License included herein.

In addition, `spf-wca` relies on several other libraries:

* Google Guava: Copyright (c) The Guava Authors and is distributed under the Apache License, 
Version 2.0. The license for Google Guava can be found in the file `licenses/COPYING.GUAVA`.
* XChart:  Copyright (c) Knowm Inc. (http://knowm.org) and contributors and Xeiam LLC 
(http://xeiam.com) and contributors. XChart is distributed under the Apache License, Version 2.0.
 A copy of the license can be found in the file `licenses/LICENSE.XCHART`. The `NOTICE` file of 
 XChart can be found in the file `licenses/NOTICE.XCHART`.
* Apache Commons Math3, which is distributed under the Apache License, Version 2.0. A copy of the
 license can be found in the file `licenses/LICENSE.COMMONS_MATH3`. The `NOTICE` file of Apache Commons 
 Math3 can be found in the file `licenses/NOTICE.COMMONS_MATH3`.
* Apache Commons CSV, which is distributed under the Apache License, Version 2.0. A copy of the
  license can be found in the file `licenses/LICENSE.COMMONS_CSV`. The `NOTICE` file of Apache Commons 
  CSV can be found in the file `licenses/NOTICE.COMMONS_CSV`.
* Apache Commons CLI, which is distributed under the Apache License, Version 2.0. A copy of the
  license can be found in the file `licenses/LICENSE.COMMONS_CLI`. The `NOTICE` file of Apache 
  Commons CLI can be found in the file `licenses/NOTICE.COMMONS_CLI`. 
* Apache Commons Lang, which is distributed under the Apache License, Version 2.0. A copy of the
  license can be found in the file `licenses/LICENSE.COMMONS_LANG`. The `NOTICE` file of Apache 
  Commons LANG can be found in the file `licenses/NOTICE.COMMONS_LANG`.
