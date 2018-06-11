PROJECT TITLE: Compiler of the yal0.4 language to Java Bytecodes



GROUP: g34


NAME1: Adam Jakomulski, NR1: up201702380, GRADE1: 20 CONTRIBUTION1: 50%
NAME2: Daniela João, NR2: up201505982, GRADE2: 16 CONTRIBUTION2: 25%
NAME3: Filipe Pinto Reis, NR3: up201506154, GRADE3: 16 CONTRIBUTION3: 25%

GLOBAL Grade of the project: 20

SUMMARY: Our compiler takes yal files and analysis them lexically, sintacticly and semanticly. After this we create a IR to take each
function and generate the equivalent java bytecodes, having optimizations done afterwards.

EXECUTE: 
java yal2jvm [options] <input_file.yal>

options:
 -o              optimization (constant propagation, constant folding, dead code elimination)
 -r=<num>        local variables alocation with <num> registers and the graph coloring algorithm
 -p              print jasmin code
 -d              dump AST
 -h              help

For the execution of many files: 
<input_file.yal>: directory/*.yal

 
DEALING WITH SYNTACTIC ERRORS: The syntactic analyzer goes through the whole code sends the first 10 errors to a 
logger with a description of the error. After that the execution stops and the errors are printed.


SEMANTIC ANALYSIS: 
The analysis detects undeclared and uninitialized variables (also in nested and complex scopes*)
The types are checked.
Functions returning value have to have the return value initialized.

*scopes: scopes of if/else, while


INTERMEDIATE REPRESENTATIONS (IRs): 
high level IR - vector of statements* with goto references.
low level IR - tree of low level operations (corresponding to jasmin operations) for each statement.

*statement - corresponds to the one line of yal code + labels and goto operations.


CODE GENERATION:
There was no external packages - everything was written from the scratch.
Code generation was written after deep analisys of how java compiler generates YVM instructions.


The most difficult problem:
Loops and if/elese statements - Labels and goto statements had to be in correct place and nested if/else and loops instructions needed the generic solution.

The problem that was almost missed was popping value from stack, when function that returns something is called without storing this value.
In the simple case the stack was calculated with the higher size, but when call was in the loop it could throw the runtime error.
The solutions was poping value from stack. The java compiler does the same thing.


TESTSUITE AND TEST INFRASTRUCTURE: 
We had prepared set of tests, that were run all at the same time, showing errors.
Tests were also automated to compile .yal, generate .j and .class and run the .class file.


TASK DISTRIBUTION: 
Adam Jakomulski
- parser
- error treatment and recovery mechanisms
- AST
- semantic analysis
- code generation
- constant folding
- constant propagation
- dead code removal
- liveness analysis
- local variables alocation by graph coloring

Daniela João
- parser
- error treatment
- code generation 
- local variables alocation by graph coloring

Filipe Pinto Reis
- parser
- error treatment
- code generation 
- local variables alocation by graph coloring


PROS: (Identify the most positive aspects of your tool)



CONS: (Identify the most negative aspects of your tool)
 - no unit tests. The compiler is academic project, so this test was not needes, but
 still - it would save some time during development process. Unit tests are also perfect code descriptions.
