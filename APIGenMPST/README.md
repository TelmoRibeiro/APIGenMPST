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
1) Fix Projection
2) Relax WSFI Semantic
3) Merge Projectability Definitions (**Better Performance**)
4) Develop Show (**pretty print**)
5) Extend example's list
6) Try to tail-recurse most functions
7) Optimize binary to limitless
___

### Notes:
> Interactions can be (reasonably) extended in order to allow for Asserted Multiparty Session Types:  
> actorA>actorB:l(x: S){A} where   
> l     => branch label   
> x : S => interaction variable (x) of sort (S)  
> A     => assertion  
> example: C>A:Login(x: string){tt}  
___