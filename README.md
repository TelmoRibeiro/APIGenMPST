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
1) Merge Well-Formedness
2) prove **NoAction & Clean** are enough to assure **valid projections**
3) develop show (pretty print)
4) extend example's list
5) try tail recursion most functions

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
- check if the new Projectability is enough relaxed and right

> Projectability Dilemma:  
> headInteraction(global: Protocol): Set[Protocol] is not enough  
> Ex: "(broker>buyer:Notify ; buyer>seller:Msg ; seller>buyer:Pay + broker>buyer:Quit ; buyer>seller:Msg)"  
> Above HeadInteraction() would pick up {broker>buyer:Notify} and {broker>buyer:Quit} which is enough to "globally" understand if we are in the left branch or the right one  
> Nonetheless, "locally" seller only knows that he will be receiving a Msg from buyer not aware which branch that action belongs to  
> Therefore, we need at least a headInteraction(global: Protocol, role: String): Set[Protocol] which takes into account every role and checks that no role would be confused