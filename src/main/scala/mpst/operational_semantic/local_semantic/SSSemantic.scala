package mpst.operational_semantic.local_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Simplifier
import mpst.utilities.Types.*

// STRONG SEQ SEMANTIC //
object SSSemantic:
  def accept(local:Protocol):Boolean =
    SSSemantic.acceptAuxiliary(local)
  end accept

  // @ telmo - not functional yet
  def reduceLocal(local:Protocol):Set[(Action,State)] =
    ???
  end reduceLocal

  def reduceState(state:State):Set[(Action,State)] =
    SSSemantic.reduceAuxiliary(state).toSet
  end reduceState

  private def acceptAuxiliary(local: Protocol): Boolean =
    local match
      // terminal cases //
      case Interaction(_,_,_,_) => false
      case Send   (_,_,_,_) => false
      case Receive(_,_,_,_) => false
      case RecursionCall(_) => false
      case End              => true
      case Sequence(localA, localB) => accept(localA) && accept(localB)
      case Parallel(localA, localB) => accept(localA) && accept(localB)
      case Choice  (localA, localB) => accept(localA) || accept(localB)
      case RecursionFixedPoint(_, localB) => accept(localB)
  end acceptAuxiliary

  // using
  private def reduceAuxiliary(state:State): List[(Action, State)] =
    val environment -> protocol = state
    protocol match
      // terminal cases //
      case Interaction(agentA,agentB,message,sort) => List(Send(agentA,agentB,message,sort) -> (environment -> Receive(agentB,agentA,message,sort)))
      case Send   (agentA, agentB, message, sort)  => List(protocol -> (environment -> End))
      case Receive(agentA, agentB, message, sort)  => List(protocol -> (environment -> End))
      case RecursionCall(variable) =>
        val localB = environment(variable)
        val nonRecursiveLocalB = removeRecursion(variable, localB)
        for nextActionB -> (nextEnvironmentB -> nextLocalB) <- reduceAuxiliary(environment, nonRecursiveLocalB) yield
          nextActionB -> (nextEnvironmentB -> removeLabel(nextActionB, localB))
        // val localB = environment(variable)
        // reduceAuxiliary(environment -> localB)
      case End => Nil
      // recursive cases //
      case RecursionFixedPoint(variable, localB) =>
        val recursionCall = variable -> localB
        reduceAuxiliary((environment + recursionCall) -> localB)
      case Sequence(localA, localB) =>
        val nextListA = reduceAuxiliary(environment -> localA)
        val nextListB = reduceAuxiliary(environment -> localB)
        val resultA = for nextActionA -> (nextEnvironmentA -> nextLocalA) <- nextListA yield
          nextActionA -> (nextEnvironmentA -> Simplifier(Sequence(nextLocalA, localB)))
        val resultB = if accept(localA) then nextListB else Nil
        resultA ++ resultB
      case Parallel(localA, localB) =>
        val nextListA = reduceAuxiliary(environment -> localA)
        val nextListB = reduceAuxiliary(environment -> localB)
        val resultA = for nextActionA -> (nextEnvironmentA -> nextLocalA) <- nextListA yield
          nextActionA -> (nextEnvironmentA -> Simplifier(Parallel(nextLocalA, localB)))
        val resultB = for nextActionB -> (nextEnvironmentB -> nextLocalB) <- nextListB yield
          nextActionB -> (nextEnvironmentB -> Simplifier(Parallel(localA, nextLocalB)))
        resultA ++ resultB
      case Choice(localA, localB) =>
        val nextListA = reduceAuxiliary(environment -> localA)
        val nextListB = reduceAuxiliary(environment -> localB)
        nextListA ++ nextListB
  end reduceAuxiliary
end SSSemantic