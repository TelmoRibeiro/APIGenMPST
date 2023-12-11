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
1) Linearity
2) Bad Syntax: End
3) prove **NoAction & Clean** are enough to assure a **valid projections**
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
- should projectability confirm that the head interaction is not the same or that no common interaction appears on both branches (current implementation)
- should I have a functions that refuses protocols with non-reachable states
- should we allow this kind of reuse but bind the variable in to the scope? 2