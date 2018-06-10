PROJECT TITLE: Compiler of the yal0.4 language to Java Bytecodes



GROUP: g34


NAME1: Adam Jakomulski, NR1: up201702380, GRADE1: 20 CONTRIBUTION1:
NAME2: Daniela João, NR2: up201505982, GRADE2: 16 CONTRIBUTION2:
NAME3: Filipe Pinto Reis, NR3: up201506154, GRADE3: 16 CONTRIBUTION3:

GLOBAL Grade of the project: 20

SUMMARY: Our compiler takes yal files and analysis them lexically, sintacticly and semanticly. After this we create a IR to take each
function and generate the equivalent java bytecodes, having optimizations done afterwards.

EXECUTE: Go to git repository and navigate "gitrep" > master > src > yal2jvm.
After getting to this directory, run Main.java <optional -r/-o for optimazation > <path to yal files>
 
DEALING WITH SYNTACTIC ERRORS: (Describe how the syntactic error recovery of your tool does work. Does it exit after the first error?)

SEMANTIC ANALYSIS: (Refer the semantic rules implemented by your tool.)

INTERMEDIATE REPRESENTATIONS (IRs): (for example, when applicable, briefly describe the HLIR (high-level IR) and the LLIR (low-level IR) used, if your tool includes an LLIR with structure different from the HLIR)

CODE GENERATION: (when applicable, describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)
 
OVERVIEW: (refer the approach used in your tool, the main algorithms, the third-party tools and/or packages, etc.)

TESTSUITE AND TEST INFRASTRUCTURE: (Describe the content of your testsuite regarding the number of examples, the approach to automate the test, etc.)
 
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



