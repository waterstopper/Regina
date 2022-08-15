import FileSystem.write

fun addGeometry2D() {
    write(
        "std/geometry2D.rgn", """
        // also a vector class
class Point {
    x = 0
    y = 0

    fun plus(other) { return Point(x=x + other.x, y=y + other.y) }
    fun minus(other) { return Point(x=x - other.x, y=y - other.y) }
    fun crossProduct(other) { return x * other.y - y * other.x }
    fun scale(coeff) { return Point(x=x * coeff, y=y * coeff) }

    // angle in degrees // TODO invocation in function args
    fun rotate(angle, pivot = Point()) {
        s = sin(toRadians(angle))
        c = cos(toRadians(angle))
        this.x = this.x - pivot.x
        this.y = this.y - pivot.y

        xnew = this.x * c - this.y * s
        ynew = this.x * s + this.y * c
        this.x = pivot.x + xnew
        this.y = pivot.y + ynew
        return this
    }

    fun distance(p) {
        if(p is Point)
            return sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y))
    }
}
object Constants {
    PI = 3.1415926539
}

fun toRadians(angle) {
    return double(angle) / 180 * Constants.PI
}

fun toDegrees(angle) {
    return double(angle) * Constants.PI / 180
}

class Segment {
    p1 = Point()
    p2 = Point()

    fun length() { p1;


    return p1.distance(p2) }
    fun vec() { return p2.minus(p1) }
    fun isPerpendicular(other) { return dotProduct(other) == 0 }

    fun center() { return centerFigure(this) }
    fun rotate(angle, pivot = center()) { rotateFigure(this, angle, pivot) }

    fun dotProduct(other) {
        vec = this.vec()
        otherVec = other.vec()
        return vec.x * otherVec.x + vec.y * otherVec.y
    }

    fun angleBetween(other) {
        if(other is Segment)
            return acos(cosAngleBetweenSegments(this, other))
        if(other is Line)
            return acos(cosAngleBetweenSegments(this, other.segment))
    }

    fun getLine() {
        return Line(a=p2.y-p1.y, b=p1.x-p2.x, c=p1.x*(p1.y-p2.y)+p1.y*(p2.x-p1.x))
    }

    fun inSegment(point) {
        line = getLine()
        if(!line.inLine(point))
            return false
        inX = if(point.x > p1.x) point.x <= p2.x else point.x > p2.x
        inY = if(point.y > p1.y) point.y <= p2.y else point.y > p2.y
        return inX && inY
    }
}

class Line {
    // ax + by + c = 0
    a = 0
    b = 1
    c = 0

    fun inLine(point) {
        return a*point.x + b*point.y + c == 0
    }

    // get a base of the perpendicular from `point` to this line
    fun toLine(point) {
        perpendicular = getPerpendicularLineContainingPoint(point)
    }

    fun getPerpendicularLineContainingPoint(point) {
        aN = b
        bN = a
        cN = -(aN * point.x + bN * point.y)
        return Line(a=aN,b=bN,c=cN)
    }

    fun intersectsLine(line) {
        if(isCollinear(line)) {
            if(isSameLine(line))
                if(b == 0)
                    return [Point(x=-c/a, y=0), Point(x=-c/a, y=1)]
                else return [Point(x=0, y=-c/b), Point(x=1, y=(-c-a)/b)]
            else return 0
        }
        return Point(x=(b*other.c - c*other.b)/(a*other.b - b*other.a), \
            y=(c*other.a - a*other.c)/(a*other.b - b*other.a))
    }

    fun isSameLine(other) {
        if(isCollinear(other))
            return double(c) / a == double(other.c) / other.a
        return false
    }

    fun isCollinear(other) {
        if(b == 0 || other.b == 0) {
            if(b != other.b)
                return false
            return true
        }
        return double(a) / b == double(other.a) / other.b
    }
}

class Rectangle {
    width = 1
    height = 1
    center = Point()
    rotation = 0

    fun center() { return center }
    fun rotate(angle, pivot = center()) { rotateFigure(this, angle, pivot) }
    fun length() { return }
}

class Triangle {
    p1 = Point()
    p2 = Point()
    p3 = Point()

    fun center() { return centerFigure(this) }
    fun rotate(angle, pivot = center()) { rotateFigure(this, angle, pivot) }
}

class Circle {
    r = 1
    center = Point()
}

class Ellipse {
    a = 1
    b = 1
    center = Point()
    rotation = 0

    fun coefficients() {}
    fun rotate(angle, pivot = center) { rotateFigure(this, angle, pivot) }
}

class Polyline {
    points = []

    fun center() { return centerFromPointsArray(points) }
    fun rotate(angle, pivot = center()) { rotateFigure(this, angle, pivot) }
}

class Plane {
    objects = []

    fun center() {
        points = []
        i = 0
        while(i < objects.size) {
            points.add(centerFigure(objects[i]))
            i = i + 1
        }

    }
    fun rotate(angle, pivot = center()) {
        i = 0
        while(i < objects.size) { rotateFigure(this, angle, pivot); i = i + 1 }
    }
}

fun insCircles(c1, c2) {

}

fun cosAngleBetweenSegments(a, b) {
    return double(a.dotProduct(b)) / a.length() / b.length()
}

fun rotateFigure(fig, angle, pivot) {
    props = fig.properties
    i = 0
    while(i < props.size) {
        if(props[i] is Point)
            props[i] = props[i].rotate(angle, pivot)
        i = i + 1
    }
}

fun translateFigure(fig, vec) {
    props = fig.properties
    i = 0
    while(i < props.size)
        if(props[i] is Point)
            props[i] = props[i].plus(vec)
}

fun scaleArrayFromLine(points, segment, coeff) {
//    i = 0
//    while(i < points.size) {
//        if(points[i] is Point) {
//            vec = points[i].minus(point).scale(coeff)
//            points[i] = point.plus(vec)
//        }
//        i = i + 1
//    }
}

fun scaleArrayFromPoint(points, point, coeff) {
    i = 0
    while(i < points.size) {
        if(points[i] is Point) {
            vec = points[i].minus(point).scale(coeff)
            points[i] = point.plus(vec)
        }
        i = i + 1
    }
}

fun centerFigure(fig) {
    props = array(fig.properties)
    array = []
    i = 0
    while(i < props.size) {
        if(props[i]["value"] is Point)
            array.add(props[i]["value"])
        i = i + 1
    }
    return centerFromPointsArray(array)
}

fun centerFromPointsArray(array) {
    res = Point()
    i = 0
    while(i < array.size) {
        res.x = res.x + array[i].x
        res.y = res.y + array[i].y
        i = i + 1
    }
    res.x = double(res.x) / array.size
    res.y = double(res.y) / array.size
    return res
}

    """
    )
}

fun addMath() {
    write(
        "std/math.rgn", """
       fun rotate(point, angle, pivot) {
    return point
}

// solve equation of type a_0 + a_1x + ... + a_{n-1}x^{n-1} = 0
// args is an array [a_{n-1},..., a_1, a_0]
fun solve(args) {
    args = removeFirstZeros(args)
    if (args.size <= 1 || args.size >= 6) {
        except("Expected from 2 to 5 elements in input array, not counting preceding zeros")
        }
    // a = 0
    if(args.size == 1)
        return [if(args[0] == 0) [1, 2, 3, 4, 5] else []]
    args = firstToOne(args)
    if(args.size == 2)
        // x + a = 0
        return [-args[1]]
    if(args.size == 3)
        return solveSquare(args[1], args[2]).sorted()
    if(args.size == 4)
        return solveCubic(args[1], args[2], args[3]).sorted()
    if(args.size == 5)
        return solveQuadratic(args[1], args[2], args[3], args[4]).sorted()
}

// remove all zeros from start of the array
fun removeFirstZeros(array) {
    if (array.size > 0 && array[0] == 0) {
        array.removeAt(0)
        return removeFirstZeros(array)
    }
    return array
}

fun firstToOne(array) {
    i = 0
    coeff = array[0]
    while(i < array.size) {
        array[i] = array[i] / double(coeff)
        i = i + 1
    }
    return array
}

// x^4 + ax^3 + bx^2 + cx + d = 0
fun solveQuadratic(a, b, c, d) {
    if (a == 0)
        return solveQuadraticZeroCube(b, c, d)
    if(d == 0)
        return (solveCubic(a, b, c) + 0.0).sorted()
    e = a / 4
    h = e * e
    p = -6 * h + b
    q =  8 * h * e - 2 * b * e + c
    r = -3 * h * h + b * h - c * e + d
    offset = solveQuadraticZeroCube(p, q, r)
    i = 0
    while(i < offset.size) {
        offset[i] = offset[i] - e
        i = i + 1
    }
    return offset
}

fun m1(p, r) {
    n = solveSquare(p, r).sorted()
    if (n.size == 0) return []
    if (n[0] >= 0)
    {
        n[0] = sqrt(n[0])
        n[1] = -n[0]
    }
    else
        n = []
    if (n[1] >= 0)
    {
        n.add(sqrt(n[1]))
        n.add(-n[n.size - 1])
    }
    return n
}

// x^4 + ax^2 + bx + c = 0
fun solveQuadraticZeroCube(a, b, c) {
    if(c == 0) // x^3 + 0x^2 + ax + b
        return solveCubic(0, a, b) + 0.0
    if(b == 0)
        return m1(a, c)
    n = solveCubic(2 * a, a * a - 4.0 * c, -b * b)
    p = n[0]
    if(n.size == 3)
    {
        if(p < n[1]) p = n[1]
        if(p < n[2]) p = n[2]
    }
    if (p <= 0)
        return m1(a, c)
    a = a + p
    p = sqrt(p)
    ba = b / p
    sol = solveSquare(p, 0.5 * (a - ba))
    sol = sol + solveSquare(-p, 0.5 * (a + ba))
    return sol
}

// x^3 + ax + b = 0
fun newton(a, b) {
    s = 1.0
    while (b + a > -1) {
        a = a * 4.0
        b = b * 8.0
        s = s * 0.5
    }
    while (b + 2 * a < -8.0) {
        a = a * 0.25
        b = b * 0.125
        s = s * 2.0
    }
    x = 1.5
    i = 0
    while (i < 9) {
        x = x - (x * ( x * x + a ) + b) / (3.0 * x * x + a)
        i = i + 1
    }
    return x * s
}

// x^3 + ax^2 + bx + c = 0
fun solveCubic(a, b, c) {
    d = 42
    // #stop
    if (c == 0) {
        solution = 0.0
    } else {
        a3 = a / 3.0
        p = b - a3 * a
        q = c - (a3 * a3 + p) * a3
        if (q < 0) {
            solution = newton(p, q)
        } else if (q > 0) {
            solution = -newton(p, -q)
        } else {
            solution = 0
        }
        solution = solution - a3
        t = solution * (solution * 3.0 + a * 2.0) + b
        if (t.abs() > 0.001) {
            solution = solution - (solution * (solution * (solution + a ) + b ) + c ) / t
        }
        a = a + solution
        b = b + solution * a
    }
    return (solveSquare(a, b) + solution).sorted()
}
// x^2 + ax + b = 0
fun solveSquare(a, b) {
   // #stop
    if (b == 0)
        return if(a == 0) [0.0, 0.0] else [0.0, -double(a)].sorted()
    solution = []
    a = a * -0.5
    d = a * a - b
    if (d < 0)
        return []
    d = sqrt(d)
    if(d == 0)
        return [a, a]
    return [a + d, a - d].sorted()
}

// x + a = 0
fun solve1(a) {
    if (a == 0)
        return if (b == 0) [1, 2] else []
    return [-b / a]
}

// Return array of doubles as a solution of ax^2 + bx + c = 0
fun solveSquare(a, b, c) {
    solution = [0.0, 0.0]
    if (a == 0) {
        if (c == 0) {
            if (b == 0) { return [1, 2, 3] }
            return [0.0]
        } else {
            if ( b == 0 ) { return [] }
            return [0.0]
        }
    } else {
        if ( c == 0 ) {
            return [0.0, -double(b) / a]
        } else {
            b = b * -0.5
            d = b * b - a * c
            if (d < 0) { return 0 }
            d = sqrt(d)
            t = double(if(b > 0) b + d else b - d)
            return [c / t, t / a]
        }
    }
}
 
    """
    )
}

fun addSvg() {
    write(
        "std/svg.rgn", """
       /**
    exportArgs:
        export: Bool = true - export or not
        type: String = "element" - "attribute" or "element"
        inParent: String = false - nest element inside parent element,
            if both instances' type is "element"
        name: String - name of exported attribute/element
        value: Any - value of attribute node
        attributes - Dictionary<String, Any> - all attributes are
            added from dictionary to element

*/
object usedValues {
    containers = ["group", "svg"]
}

// should have a name
class Node {
    attributes = []
    children = []

    fun toString() {
        res = StringBuilder()
        res.add("<" + this.name)
        i = 0
        while(i < attributes.size) {
            res.add(" ")
            res.add(attributes[i].toString())
            i = i + 1
        }
        res.add(if(children.size == 0) "/>" else ">")
        i = 0
        while(i < children.size) {
            res.add(children[i].toString())
            i = i + 1
        }
        if(children.size != 0) {
            res.add("<" + this.name + "/>")
        }
    }
}
// should have a name and a value
class Attribute {
    fun toString() {
        return name + "=\"" + value + "\""
    }
}

fun add(node, sb) {
    containers = ["group, svg"]
    added = node.exportArgs["exported"]
    propertiesArray = array(node.properties)
    attributes = array(node.exportArgs["attributes"])
    if(added != 0) {
        sb.add("<" + added)
        i = 0
        while(i < attributes.size) {

            sb.add(attributes[i]["key"] + "=\"" + attributes[i]["value"] + "\"")
            i = i + 1
        }
        sb.add(if(containers.has(added)) ">" else "/>")
    }
    if(containers.has(added)) {
        i = 0
        #stop
        while(i < propertiesArray.size) {
            if(propertiesArray[i]["value"].exportArgs["type"] == "node" \
                && propertiesArray[i]["value"].exportArgs["inParent"]) {
                add(propertiesArray[i]["value"], sb)
                propertiesArray.removeAt(i)
                i = i - 1
            }
            i = i + 1
        }
        sb.add("<" + added + "/>\n")
    }
    i = 0

    while(i < propertiesArray.size) {
        if((propertiesArray[i]["value"].properties["exportArgs"] != 0) \
            && propertiesArray[i]["value"].exportArgs["type"] == "node" \
            && (!propertiesArray[i]["value"].exportArgs["inParent"] && !containers.has(added))) {
            if(propertiesArray[i]["key"]!="parent" && propertiesArray[i]["key"] != "this") {
                add(propertiesArray[i]["value"], sb)
               }
            propertiesArray.removeAt(i)
            i = i - 1
        }
        i = i + 1
    }
}

fun addAttribute(node, sb) {
    sb.add(" ")
    sb.add(node.name)
    sb.add("=\"" + node.properties["value"] + "\"")
}

fun create(root, width, height) {
    sb = StringBuilder()
    sb.add("<svg \"" + width + "\" height \"" + height + "\">\n")
    add(root, sb)
    sb.add("</svg>")
    log(sb.toString())
}

class StringBuilder {
    string = []
    fun add(s) {
        string.add(s)
    }
    fun toString() {
        return string.joinToString("")
    }
}

// class A : B -> ["exported":"svg", "inParent": true] {} 
    """
    )
}

fun addMathTest(){
    write("src/commonTest/resources/std/mathTest.rgn", """
        import std.math as math

fun main() {
    testSolve2()
    testSolve3()
    testSolve4()
}

fun testSolve2() {
    test(solveSquare(0, 0) == [0, 0])
    test(solveSquare(1, 0) == [-1, 0])
    test(solveSquare(0, 1) == [])
    test(solveSquare(2, 1) == [-1, -1])
    test(solveSquare(0, -1) == [-1, 1])
    test(solve([1, -1, 0]) == [0, 1])
}

fun testSolve3() {
    test(solve([1,2,1,0]) == [-1,-1, 0])
    test(solve([1,3,3,1]) == [-1, -1, -1])
    test(solve([1,-6,11,-6])==[1,2,3])
    //test(solve([1,-6,11,-6, 0])==[0,1,2,3])
}

fun testSolve4() {
    test(solve([1, 0, 0, 0, 0]) == [0, 0, 0, 0])
    test(solve([1, -1, 0, 0, 0]) == [0, 0, 0, 1])
    log(solve([2, -1, 0, 0, 0]))// == [0, 0, 0, 0.5])
    test(solve([1, -4, 6, -4, 1]) == [1, 1, 1, 1])
    test(roundArr(solve([1, 4, -4, -20, -5])) == [-3.7321, -2.2361, -0.2679, 2.2361])
    test(solve([1, -6, 11, -6, 0]) == [0, 1, 2, 3])
    test(solve([5, -30, 55, -30, 0]) == [0, 1, 2, 3])
    test(roundArr(solve([3, -2, -9, -4, 12])) == [1, 2])
}

fun roundArr(array) {
    i = 0
    while(i < array.size) {
        array[i] = array[i].round(4)
        i = i + 1
    }
    return array
}

    """)
}

fun addGeometryTest() {
    write("src/commonTest/resources/std/geometry2DTest.rgn", """
        import std.geometry2D as geometry2D

        fun main() {
            testPoint()
            testSegment()
        }

        fun testPoint() {
            m1 = Point(x=5, y=5)
            m2 = Point(x=2, y=1)
            minus_res = m1.minus(m2)
            test(minus_res.x == 3)
            test(minus_res.y == 4)

            rotated = Point(y = 1)
            test(rotated.y == 1)
            rotateResult = rotated.rotate(90)
            test(floatEquals(rotateResult.x, -1))
            test(floatEquals(rotateResult.y, 0))
        }

        fun testSegment() {
            s = Segment(p1=Point(y=1, x=0), p2=Point(y=3, x=3))
            line = s.getLine() // 2x - 3y + 3 = 0
            test(line.a == 2)
            test(line.b == -3)
            test(line.c == 3)

            p = s.center()
            test(p.x == 1.5)
            test(p.y == 2)

            rotated = s.rotate(45)
            log(rotated)
        }

    """)
}

fun addGenericTest() {
    write("src/commonTest/resources/testCode.rgn", """
        import src.commonTest.resources.imported as imported
        import std.svg as svg
        import std.math as math
        import std.geometry2D as geom

        fun checkMultipleCalls(a)
        {
            b = a + 0
            if(a > 1)
            {
                res = 1 + checkMultipleCalls(a - 1)
            } else {
                res = 1
            }
            test(a == b)
            log("res: " +res + ", a: "+ a)
            return res
        }

        class TestClass {
            // sb.add(attributes[i]["key"] + "=\"" + attributes[i]["value"] + "\"")
            c = b
            b = a
            a = 1
        }

        class Position {
            x = 0
            y = 0
        }

        class Nothing {
            exportArgs = {"type":"node","attributes":{}}
        }

        class Segment {
            iter = if(parent == 0) 0 else (1 + parent.iter)
            root = if(iter < 2) Segment() else Nothing()
            start = Position(x = if(parent == 0) 0 else parent.end.x, y = if(parent == 0) 0 else parent.end.y)
            end = math.rotate(Position(x = start.x, y = start.y + 10), 30, start)
            exportArgs = {"exported":"line","type":"node","attributes":{"x1": start.x, "y1": start.y}}
        }

        class DoubleSegment {
            iter = 1
           // left = Segment()
           // right = Segment()
        }

        fun main() {
            s = Segment()
            attributes = array(s.exportArgs["attributes"])
            log([1,2,3].has(1))
            testing = 1
            if(1) {
                testing = 2
                }
            log(testing)
            prim = 123.123
            r = prim
            r = r.round()
            log(r)
            log(A() is B)
            a = Point(x = 5, y = 3)
            b = a.rotate(90)
            b.x = 2
            log(a.x)
            log(a.x + " " + a.y)
            a = [1,2,3,4]
            log(a.joinToString(""))



            B() is A
            m = {"a":1,2:"c"}
            log(m)
            //a = []
            //a.add(0,a)
            //a = Something()
            a = Segment()
            svg.create(a,100,100)
            log(a)
            a = DoubleSegment()
            log(a)
            a = TestClass()
            log(a)
            a = Something()
            log(a)
            assignmentTest()


            test(addition(2, 3) == 5)
            a = [2]
            a.add([1])
            a.add(a)
            log(a)
            checkLink()
            checkType()
            test(checkMultipleCalls(3) == 3)
            test(checkNestedReturn() == 1)
            primitiveTest()
          //  b = i()
        //	log([0] + 1)
        //	log(("t" + "t")[0])
        //	log(5 / 2)
        //	a = toRadians(32)
        //	log(rndInt(64,999))
        //	log(rndInt(64,999))
        //	c = Something()
        //	arr = [c,"a",1]
        //	log(arr)
        //	add(arr,2)
        //	add(arr,1,2)
        //	log(arr)
        //	log(remove(arr,c))
        //	log(arr)
        //	removeAt(arr,1)
        //	log(arr)
        //	log(has(arr,2))
        //	a = []
        //	b = []
        //	log(a == b)
        //	log([0] == [0.0])
        //	whileFun()
        	//Root()
        	//c = Something()
        	//d = c
        	//c.e = Something()
        //	log(c)
        //	log(d)

        	a = [5,2]
        	a[0] = "fads"
        	log(a)
        	testMath()
        }

        fun testMath() {
            log(solveSquare(2,1))
        }

        fun checkLink() {
            // no variable links
            test(-0.2.abs() == -0.2)
            test((-0.2).abs() == 0.2)
            test(1.max(2) == 2)
            test(0.1.max(0.01) == 0.1)
            test(1.MAX_VALUE == 2147483647)

            test("abc".reversed() == "cba")

            test((if(1) -2 else 0).abs() == 2)
            // file links

            test(imported.getFileName() == "imported")

            //a = A()
            //a.s = 10
            //test(a.s == 10)
            //test(a.t == 0)
            b = 2
        }

        fun assignmentTest() {
            a = b = c = 2
            test(a == 2 && b == 2 && c == 2)
            a = A()
            a.s = 2
            a.s = 3
            test(a.s == 3)
        }

        fun primitiveTest() {

        }

        fun checkNestedReturn() {
            if(1 == 1) {
                while(1 == 1) {
                    if(1 == 1) {
                        return 1
                    }
                }
            }
        }

        fun checkType() {
        	test(0.0 is Int)
        	test(0 is Int)
        	test("" is String)
        	test([] is Array)
        	test(A() is A)
        	log(B() is A)
        	test(B() is A)
        	test(0 !is Array)
        }

        class Something {
        	c = a + b + a
        	iter = 1
        	a = 5
        	b = a
        }

        fun whileFun() {
        	a = 1
        	while(a < 5) {
        		if(a % 2 == 0){
        			log(a)
        		} else{
        			log(a + 1)
        		}
        		a = a + 1
        	}
        	log(a)
        }

        class A {
            a = 1
        }

        class B : A {}

        class D:imported.A {}

        class Colors {
        	BLACK = "000000"
        	WHITE = "FFFFFF"
        	GRAY = "929292"
        	RED = "BF3011"
        	ORANGE = "DF8022"
        	YELLOW = "E5B527"
        	MINT = "26D07E"
        	GREEN = "06694A"
        	BLUE = "3896D4"
        	INDIGO = "253092"
        	PURPLE = "820D7C"
        	CHERRY = "FD5FF0"
        	// CHERRY = "69122D"
        }

        class Root {
        	segm = Something()
        	iter = 0
        }

        fun toDegrees(rad) {
        	return
        	a = b.c.d.e.f.g.h.parent.t
        }


        class Math {
        	RANDOM_SEED = 42
        	PI = 3.1415926
        }

        fun rotate(point, pivot, angle) {
        break}

        fun abs(x) {
        	return if (x < 0) -x else x
        }

        // here two blocks inside each other. Return won't be passed.
        fun toRadians(deg) {
            log("FIEFNEEFIN")
        	if(deg > 3){
        		return abs( \
        		-deg) * 3 / 180
        } else {return 0}
        }

        // returns random int in range [a,b]
        fun rndInt(a,b)  {
        	t = int(rnd() * ((b - a) + 1))
        	log("t:" + t)
        	log(t)
        	return a + t
        }

        // get all classes of classRef type
        fun all(classRef) {
        	res = []
        	stack = [Root]
        	while(stack.size > 0) {
        		current = stack.remove(i)
        		if(current is classRef) {
        			res.add(current)
        		} else {
        		}
        	}
        	stack.add(res.properties !is Value)
        }
    """)
    write("src/commonTest/resources/imported.rgn", """
        import src.commonTest.resources.same as same

class A {}

fun getFileName() {
    return "imported"
}

fun addition(a, b) {
    get()
    test(same.addition(a,b) \
        == a + b + 1)
    log(getFileName() \
        == "imported")
    return a + b
}
    """)
    write("src/commonTest/resources/same.rgn", """
        fun getFileName() {
            return "same"
        }

        fun addition(a, b) {
            test(getFileName() \
                == "same")
            return a + b + 1
        }

        fun get() {
            log("from Same")
        }
    """)
}