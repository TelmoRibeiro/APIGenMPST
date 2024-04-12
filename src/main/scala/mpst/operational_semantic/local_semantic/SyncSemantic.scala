package mpst.operational_semantic.local_semantic

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Simplifier

object SyncSemantic:
  def accept(local:Protocol):Boolean =
    SyncSemantic.acceptAuxiliary(local)
  end accept

  def reduce(state:State):Set[(Action,State)] =
    SyncSemantic.reduceAuxiliary(state).toSet
  end reduce

  private def acceptAuxiliary(local: Protocol): Boolean =
    local match
      // terminal cases //
      case    Send(agentA, agentB, message, sort) => false
      case Receive(agentA, agentB, message, sort) => false
      case RecursionCall(_) => false
      case End              => true
      // recursive cases //
      case RecursionFixedPoint(_, localB) => accept(localB)
      case Sequence(localA, localB) => accept(localA) && accept(localB)
      case Parallel(localA, localB) => accept(localA) && accept(localB)
      case   Choice(localA, localB) => accept(localA) || accept(localB)
      // unexpected cases //
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end acceptAuxiliary

  private def reduceAuxiliary(state:State): List[(Action, State)] =
    val environment -> local = state
    local match
      // terminal cases //
      case Send   (agentA, agentB, message, sort) => List(local -> (environment -> End))
      case Receive(agentA, agentB, message, sort) => List(local -> (environment -> End))
      case RecursionCall(variable) =>
        val localB = environment(variable)
        val nonRecursiveLocalB = removeRecursion(variable, localB)
        for nextActionB -> (nextEnvironmentB -> nextLocalB) <- reduceAuxiliary(environment, nonRecursiveLocalB) yield
          nextActionB -> (nextEnvironmentB -> removeLabel(nextActionB, localB))
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
      // unexpected cases //
      case global => throw new RuntimeException(s"unexpected global type $global found\n")
  end reduceAuxiliary
end SyncSemantic