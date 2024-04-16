package mpst.analysis

import mpst.syntax.Protocol
import mpst.syntax.Protocol.*
import mpst.utilities.Types.*


case class Graph(vertices:Set[Agent],edges:Set[(Agent,Agent)]):
  private def merge(graph:Graph): Graph =
    Graph(vertices ++ graph.vertices, edges ++ graph.edges)
  end merge

object Graph:
  def buildGraph(protocol:Protocol):Set[Graph] =
    protocol match
      case Interaction(agentA,agentB,_,_) => Set(Graph(Set() + agentA + agentB, Set() + (agentA -> agentB)))
      case RecursionCall(_) => Set()
      case End => Set()
      case Sequence(protocolA,protocolB) => buildGraph(protocolA).flatMap(graphA => buildGraph(protocolB).map(graphB => graphA.merge(graphB)))
      case Parallel(protocolA,protocolB) => buildGraph(protocolA).flatMap(graphA => buildGraph(protocolB).map(graphB => graphA.merge(graphB)))
      case Choice  (protocolA,protocolB) => buildGraph(protocolA) ++ buildGraph(protocolB)
      case RecursionFixedPoint(_,protocolB) => buildGraph(protocolB)
      case local => throw new RuntimeException(s"unexpected local type found in $local")
  end buildGraph
end Graph