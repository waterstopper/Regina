package properties

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class TypeTest {
    @Test
    fun testBidirectionalPropertyResolving() {
        eval(
            """
           fun main() {
                a = A()
                log("Built")
                log(a.str())
           }
           class A {
                iter = if(parent == 0) 0 else parent.iter + 1
                next = if(iter < 1) A() else 0
                fromNext = if(next == 0) -1 else next.fromNext - 1
              
              fun str() {
                  return "\n" + iter + " " + fromNext + (if(next == 0) "" else next.str()) 
              }
              
              fun withCycle() {
                while(1) {
                    a.b.c
                }
              }
           }
        """
        )
    }

    @Test
    fun sameProperty() {
        val thrown = assertFails {
            eval(
                """
            fun main(){}
            class A {
                a = 0
                a = 1
            }
        """
            )
        }
        assertTrue(thrown.message!!.contains("Same property found above"))
    }

    @Test
    fun testLinkProperty() {
        eval(
            """
            class Root {
                a = A()
                a.root = this
                iter = 0
            }
            class A {
                iter = parent.iter + 1
                a = if(iter < 1) A() else Leaf()
                a.root2 = a.root
                a.root = this
            }
            class Leaf {}
            fun main() {
                r = Root()
                log(r)
            }
        """
        )
    }

    @Test
    fun testNontrivialTwoStepLink() {
        eval(
            """
            class B {
                nontrivial = c.d.e
                c = C()
            }
            class C {
                d = D()
                d.e = E()
            }
            class D {}
            class E {}
            fun main() {
                b = B()
                test(b.c.d.e is E)
                test(b.c.d is D)
                test(b.c.d !is E)
                test(b.nontrivial is E)
                test(b is B); test(b.c is C)
            }
        """
        )
    }

    @Test
    fun findPropertyInMiddleClass() {
        eval(
            """
           class Start {
                mid = Middle()
           }
           class Middle {
                end = End()
                property = 2
                laterInitFromEnd = 1
           }
           class End {
                start = parent.parent
                a = start.mid.property
                start.mid.laterInitFromEnd = 3
           }
           fun main() {
                start = Start()
                test(start.mid.laterInitFromEnd == 3)
                test(start.mid.end.a == 2)
           }
        """
        )
    }

    @Test
    fun testEquals() {
        eval(
            """
            fun main() {
                first = A()
                firstOtherLink = first
                second = A()
                test(first != second)
                test(first == firstOtherLink)
                first.s = 2
                first.s = 1
                test(first == firstOtherLink)
                test(firstOtherLink.s == 1)
                firstOtherLink.a = "a"
                test(first.a == "a")
                
                firstSecondOther = changeB(first)
                test(firstOtherLink.b == "b")
                test(firstSecondOther == first)
                test(firstSecondOther == firstOtherLink)
            }
            class A {
                a = 0
                b = 1
            }
            fun changeB(aInstance) {aInstance.b = "b"
                return aInstance
            }
        """
        )
    }

    @Test
    fun checkInheritance() {
        eval(
            """
           class A {
                a = 2
                fun a() {return "a"}
           }
           class B:A {
            b = 3
            fun b() {return b}
            fun a() {return "b"}
           }
           fun main() {
            b = B()
            log(b.properties)
            log(b.a())
           }
        """
        )
    }

    @Test
    fun testTripleInheritance() {
        eval(
            """
            fun main() {
                a = A()
            //    log(a.c)
                log(a.b)
                log(a.p)
            }
            class A : B {
                p = 2
            }
            class B : C {
                p = 1
                b = p
            }
            class C {
                p = 0
                c = p
            }
        """
        )
    }

    @Test
    fun testInvocationInParent() {
        eval(
            """
            fun main() {
                a = A()
                test(a.b.one == 1)
            }
            class A {
                b = B()
                
                fun get1() {return 1}
            }
            class B {
                one = parent.get1()
            }
        """
        )
    }

    @Test
    fun testNotCreatedInvocation() {
        eval(
            """
            fun main() {
                a = A()
                test(a.b.one == 1)
            }
            class A {
                b = B()
                b.c = C()
                fun get1() {return 1}
            }
            class B {
                one = c.func()
            }
            class C {
                fun func(){return 1}
            }
        """
        )
    }

    @Test
    fun createFromTypeFunction() {
        eval("""
            fun main() {
                a = A()
                b = type(a)()
                test(b is A)
            }
            
            class A {
                p = 1
            }
        """)
    }
}