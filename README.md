# APIGenMPST
___

### In order to run:
**JVM and SBT must be installed**  
  
In **/APIGenMPST** run:
````bash
sbt clean
sbt compile
sbt run
````
___

### To Do:
1) Relax Projectability
2) Merge Well-Formedness
3) prove **NoAction & Clean** are enough to assure **valid projections**
4) develop show (pretty print)
5) extend example's list
6) try tail recursion most functions

___

### Notes:
> Interactions can be (reasonably) extended in order to allow for Asserted Multiparty Session Types:  
> actorA>actorB:l(x: S){A} where   
> l     => branch label   
> x : S => interaction variable (x) of sort (S)  
> A     => assertion  
> example: C>A:Login(x: string){tt}  

___ 

### Questions:
- non-reachable(examples)
- fixed point variables bounded to the scope or not reused at all