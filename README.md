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
1) Merge Well-Formedness Definitions (**Better Performance**)
2) Prove **NoAction + Clean** are enough to ensure **valid projections**
3) Develop Show (**pretty print**)
4) Extend example's list
5) Try to tail-recurse most functions

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
- are there non-reachable states?
- should fixed point variables be bounded to the scope or should we not allow reuse in an inner block?
- is Projectability right and relaxed enough?