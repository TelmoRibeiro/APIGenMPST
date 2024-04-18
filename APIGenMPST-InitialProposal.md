API generation of Multiple Multiparty Session Types
====================

Multiparty session types (MPST) support both the *global description* of interaction patterns between a set of collaborating components, and the *local description* of the patterns of interaction of each of these components. A common approach to enforce that components follow this description is the generation of APIs. These APIs consist of a set of functions or structures that can be used by the components to communicate, which raise errors (statically or at runtime) whenever the interaction protocol is violated.

Many variations of MPST exist, each using a specific set of constructs to describe interaction patterns, and using a specific set of assumptions over the network. Furthermore, different implementations to generate APIs have different assumptions and provide different guarantees. Hence, existing approaches do not target larger distributed systems using components implemented in different languages [1].

This project proposes to investigate existing approaches to generate APIs from MPST (e.g., [2,3,4]), and to implement two or more approaches to generate APIs. Guided by this survey and implementations, we propose to investigate the compatibility of API engines, and how to facilitate the interactions between components that use different API engines, while maximising the guarantees provided by the global types and minimising the required assumptions and restrictions.

More specifically, the concrete plan for this project is as follows.

 1. Survey existing engines to generate APIs for MPST (2M)
 2. Attempt to categorise what concepts each of these engine supports (1M)
 2. Implement an API engine for a concrete MPST and a concrete back-end language (4M)
 3. Implement other variation(s) of the API engine (2M)
 4. Using a concrete example of a global type, use more than one API engine to generate multiple APIs and execute a concurrent system with heterogeneous components (3M)
 5. Write the dissertation (3M)
 

# Bibliography
[1] Sung-Shik Jongmans, José Proença (2022). ST4MP: A Blueprint of Multiparty Session Typing for Multilingual Programming. ISoLA 2022, LNCS 13701.
[2] Hu, R., Yoshida, N. (2016). Hybrid session verification through endpoint API generation. FASE 2016, LNCS 9633.
[3] Cledou, G., Edixhoven, L., Jongmans, S.S., Proença, J. (2022). API generation for multiparty session types, revisited and revised using Scala 3. ECOOP 2022. LIPIcs 222.
[4] Lagaillardie, N., Neykova, R., Yoshida, N. (2020). Implementing multiparty session types in Rust. COORDINATION 2020. LNCS 12134.