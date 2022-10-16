import FileSystem.write

fun addGeometry2D() {
    write(
        "std/geometry2D.rgn",
        """
        // also a vector class
class Point {
    x = 0
    y = 0

    fun plus(other) { return Point(x=x + other.x, y=y + other.y) }
    fun minus(other) { return Point(x=x - other.x, y=y - other.y) }
    fun crossProduct(other) { return x * other.y - y * other.x }
    fun scale(coeff) { return Point(x=x * coeff, y=y * coeff) }

    // angle in degrees // TODO invocation in function args
    fun rotate(angle, pivot=Point()) {
        s = toRadians(angle).sin()
        c = toRadians(angle).cos()
        this.x = this.x - pivot.x
        this.y = this.y - pivot.y

        xnew = this.x * c - this.y * s
        ynew = this.x * s + this.y * c
        this.x = pivot.x + xnew
        this.y = pivot.y + ynew
        return this
    }

    fun translate(vec) {
        this.x = this.x + vec.x
        this.y = this.y + vec.y
        return this
    }

    fun scale(coeff, origin) {
        this.x = this.x + (this.x - origin.x) * coeff
        this.y = this.y + (this.y - origin.y) * coeff
        return this
    }

    fun distance(p) {
        if(p is Point)
            return sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y))
    }

    fun toString() { return str(x) + " " + y}
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
    stroke = "#000000"
    strokeWidth = 1

    fun length() { return p1.distance(p2) }
    fun vec() { return p2.minus(p1) }
    fun isPerpendicular(other) { return dotProduct(other) == 0 }

    fun center() { return centerFromPointsList([p1, p2]) }
    fun rotate(angle, pivot=center()) {
        rotatePointsList([p1, p2], angle, pivot)
        return this
    }
    fun scale(coeff, origin=Point()) {
        scalePointsList([p1, p2], coeff, origin)
        return this
    }
    fun translate(vec) {
        translatePointsList([p1, p2], vec)
    }

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

    svgArgs = {"name":"line", \
        "attributes": {"x1":p1.x, "y1":p1.y, \
        "x2":p2.x, "y2":p2.y,\
        "stroke":stroke,\
        "stroke-width":strokeWidth}}
}

class Line {
    // ax + by + c = 0
    a = 0
    b = 1
    c = 0

    fun inLine(point) {
        return a * point.x + b * point.y + c == 0
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
/* Base class for geometry with `points` property */
class ArrayGeom {
    svgName = "g"
    points = []
    fill = "#000000"
    stroke = "none"

    fun center() {return centerFromPointsList(points)}
    fun translate(vec) { translatePointsList() }

    fun rotate(angle, pivot=Point()) {
        rotatePointsList(points, angle, pivot)
    }

    fun scale(coeff, origin=Point()) {
        scalePointsList(list, coeff, origin)
    }

    svgArgs = {"name":svgName, "attributes":\
        {"points":points.joinToString(", "),\
        "stroke":stroke,"fill":fill}}
}

/* Polygon and polyline */
class Polyline: ArrayGeom {
    svgName = "polygon"
    points = []
    isClosed = false
}

class RegularPolygon: ArrayGeom {
    r = 35
    q = 4 // >= 3
    svgName = "polygon"
    convex = true // works for odd q
    center = Point(x=0, y=0)
    points = createPolygon()

    fun createPolygon() {
        res = []
        angle = 360 / q
        cur = 0
        foreach(i in range(1, q)) {
            p = Point(x = center.x + r, y = center.y)
            res.add(p.rotate(cur, center))
            cur = cur + (if(convex) angle else 2 * angle)
        }
        return res
    }
}
/* Similar to <g> tag in svg. */
class Plane {
    objects = []

    fun center() {
        points = []
        i = 0
    }

    fun rotate(angle, pivot = center()) {
        foreach(i in objects) { rotateFigure(i, angle, pivot) }
    }
}

/*
    Classes below can be rotated with svg.transform only
*/
class PointGeom {
    center = Point()
    fill = "#000000"
    stroke = "none"
    fun rotate(angle, pivot=Point()) {} // does nothing
    fun translate(vec) { this.center.translate(vec) }
    fun scalePointGeom(coeff, origin=center) {
        this.center.scale(coeff, origin)
    }
}

class Rectangle: PointGeom {
    width = 1
    height = 1

    fun center() { return center }
    fun length() { return }
    fun scale(coeff, origin=center) {
        scalePointGeom(coeff, origin)
        this.width = this.width * coeff
        this.height = this.height * coeff
    }

    fun downLeft() {
        return Point(x=center.x-width/2,y=center.y+height/2)
    }
    fun upLeft() {
        return Point(x=center.x-width/2,y=center.y-height/2)
    }
    fun upRight() {
        return Point(x=center.x+width/2,y=center.y-height/2)
    }
    fun downRight() {
        return Point(x=center.x+width/2,y=center.y+height/2)
    }

    svgArgs = {"name":"rect", \
        "attributes":{"width":width, "height":height, \
        "x":center.x - width / 2,\
        "y":center.y - height / 2,\
        "stroke":stroke,"fill":fill}}
}

class Circle: PointGeom {
    r = 1

    fun scale(coeff, origin=center) {
        scale(coeff, origin)
        this.r = this.r * coeff
    }

    svgArgs = {"name":"circle", \
        "attributes":{"cx":center.x, "cy":center.y,\
        "r":r,\
        "stroke":stroke,"fill":fill}}
}

class Ellipse: PointGeom {
    width = 1
    height = 1
    fun scale(coeff, origin=center) {
        scalePointGeom(coeff, origin)
        this.width = this.width * coeff
        this.height = this.height * coeff
    }

    svgArgs = {"name":"ellipse", "attributes":\
        {"cx":center.x, "cy":center.y,\
        "rx":width, "ry":height,\
        "stroke":stroke,"fill":fill}}
}

fun insCircles(c1, c2) {

}

fun cosAngleBetweenSegments(a, b) {
    return double(a.dotProduct(b)) / a.length() / b.length()
}

fun rotatePointsList(list, angle, pivot=Point()) {
    foreach(point in list)
        point.rotate(angle, pivot)
}

fun translatePointsList(list, vec) {
    foreach(point in list)
        point.translate(vec)
}

fun scalePointsList(list, coeff, origin=Point()) {
    foreach(point in list)
        point.scale(origin, coeff)
}

fun scalePointsListFromLine(list, segment, coeff) {
    except("not implemented")
}

// get center mass of points
fun centerFromPointsList(list) {
    res = Point()
    i = 0
    while(i < list.size) {
        res.x = res.x + list[i].x
        res.y = res.y + list[i].y
        i = i + 1
    }
    res.x = double(res.x) / list.size
    res.y = double(res.y) / list.size
    return res
}
   """
    )
}

fun addMath() {
    write(
        "std/math.rgn",
        """
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
    coeff = array[0]
    foreach(i in range(0, array.size-1)) {
        array[i] = array[i] / double(coeff)
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
    foreach(i in range(0, offset.size - 1)) {
        offset[i] = offset[i] - e
    }
    return offset
}

fun m1(p, r) {
    n = solveSquare(p, r).sorted()
    if (n.size == 0) return []
    if (n[0] >= 0)
    {
        n[0] = n[0].sqrt()
        n[1] = -n[0]
    }
    else
        n = []
    if (n[1] >= 0)
    {
        n.add(n[1].sqrt())
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
    p = p.sqrt()
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
    if (c == 0) {
        solution = 0.0
    } else {
        a3 = a / 3
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
    if (b == 0)
        return if(a == 0) [0.0, 0.0] else [0.0, -double(a)].sorted()
    solution = []
    a = a * -0.5
    d = a * a - b
    if (d < 0)
        return []
    d = d.sqrt()
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
            d = d.sqrt()
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
        "std/svg.rgn",
        """
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
class SvgNode {
    name = "svg"
    header = StringBuilder()
    content = StringBuilder()
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

    fun addTextToHeader(text) {
    }

    fun addTextToContent(text, toStart = false) {

    }
}

class Transform {
    scale = ""
}

// should have a name and a value
class Attribute {
    fun toString() {
        return name + "=\"" + value + "\""
    }
}

fun add(node, sb) {
    containers = ["group, svg"]
    print(node)
    #stop
    added = node.exportArgs["exported"]
    propertiesArray = list(node.properties)
    attributes = list(node.exportArgs["attributes"])
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
        if((propertiesArray[i]["value"]?.properties["exportArgs"] != null) \
            && propertiesArray[i]["value"].exportArgs["type"] == "node" \
            && (propertiesArray[i]["value"].exportArgs["inParent"] != null && !containers.has(added))) {
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
    print(sb.toString())
}

class StringBuilder {
    string = []
    fun add(s, index=string.size) {
        string.add(s, index)
    }

    fun toString(sep="") {
        return string.joinToString(sep)
    }
}

// class A : B -> ["exported":"svg", "inParent": true] {}
    """
    )
}

fun addMathTest() {
    write(
        "src/commonTest/resources/std/mathTest.rgn",
        """
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
    print(solve([2, -1, 0, 0, 0]))// == [0, 0, 0, 0.5])
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

    """
    )
}

fun addGeometryTest() {
    write(
        "src/commonTest/resources/std/geometry2DTest.rgn",
        """
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
            print(rotated)
        }

    """
    )
}

fun addGenericTest() {
    write(
        "src/commonTest/resources/testCode.rgn",
        """
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
    print("res: " +res + ", a: "+ a)
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
    iter = (parent?.iter ?? 0) + 1
    root = if(iter < 2) Segment() else Nothing()
    start = Position(x = parent?.end.x ?? 0, y = parent?.end.y ?? 0)
    end = math.rotate(Position(x = start.x, y = start.y + 10), 30, start)
    exportArgs = {"exported":"line","type":"node","attributes":{"x1": start.x, "y1": start.y}}
}

class DoubleSegment {
    iter = 1
   // left = Segment()
   // right = Segment()
}

fun main() {
    print(geom.Constants.PI)
    a = imported.A()
    s = Segment()
    attributes = list(s.exportArgs["attributes"])
    print([1,2,3].has(1))
    testing = 1
    if(1) {
        testing = 2
        }
    print(testing)
    prim = 123.123
    r = prim
    r = r.round()
    print(r)
    print(A() is B)
    a = Point(x = 5, y = 3)
    b = a.rotate(90)
    b.x = 2
    print(a.x)
    print(str(a.x) + " " + a.y)
    a = [1,2,3,4]
    print(a.joinToString(""))



    B() is A
    m = {"a":1,2:"c"}
    print(m)
    //a = []
    //a.add(0,a)
    //a = Something()
    a = Segment()
    svg.create(a,100,100)
    print(a)
    a = DoubleSegment()
    print(a)
    a = TestClass()
    print(a)
    a = Something()
    print(a)
    assignmentTest()


    test(addition(2, 3) == 5)
    a = [2]
    a.add([1])
    a.add(a)
    print(a)
    checkLink()
    checkType()
    test(checkMultipleCalls(3) == 3)
    test(checkNestedReturn() == 1)
    primitiveTest()
  //  b = i()
//	print([0] + 1)
//	print(("t" + "t")[0])
//	print(5 / 2)
//	a = toRadians(32)
//	print(rndInt(64,999))
//	print(rndInt(64,999))
//	c = Something()
//	arr = [c,"a",1]
//	print(arr)
//	add(arr,2)
//	add(arr,1,2)
//	print(arr)
//	print(remove(arr,c))
//	print(arr)
//	removeAt(arr,1)
//	print(arr)
//	print(has(arr,2))
//	a = []
//	b = []
//	print(a == b)
//	print([0] == [0.0])
//	whileFun()
	//Root()
	//c = Something()
	//d = c
	//c.e = Something()
//	print(c)
//	print(d)

	a = [5,2]
	a[0] = "fads"
	print(a)
	testMath()
}

fun testMath() {
    print(solveSquare(2,1))
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

    a = A()
    a.s = 10
    test(a.s == 10)
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
	test(0.0 is Double)
	test(0 is Int)
	test("" is String)
	test([] is List)
	test(A() is A)
	print(B() is A)
	test(B() is A)
	test(0 !is List)
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
			print(a)
		} else{
			print(a + 1)
		}
		a = a + 1
	}
	print(a)
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

fun rotate(point, pivot, angle) {}

fun abs(x) {
	return if (x < 0) -x else x
}

// here two blocks inside each other. Return won't be passed.
fun toRadians(deg) {
    print("FIEFNEEFIN")
	if(deg > 3){
		return abs( \
		-deg) * 3 / 180
} else {return 0}
}

// returns random int in range [a,b]
fun rndInt(a,b)  {
	t = int(rnd() * ((b - a) + 1))
	print("t:" + t)
	print(t)
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

    """
    )
    write(
        "src/commonTest/resources/imported.rgn",
        """
        import src.commonTest.resources.same as same

class A {}

fun getFileName() {
    return "imported"
}

fun addition(a, b) {
    get()
    test(same.addition(a,b) \
        == a + b + 1)
    print(getFileName() \
        == "imported")
    return a + b
}

class ForCheck {}
    """
    )
    write(
        "src/commonTest/resources/same.rgn",
        """
        fun getFileName() {
            return "same"
        }

        fun addition(a, b) {
            test(getFileName() \
                == "same")
            return a + b + 1
        }

        fun get() {
            print("from Same")
        }
    """
    )
}

fun addIsTest() {
    write(
        "src/commonTest/resources/isTest.rgn",
        """
        import src.commonTest.resources.imported as imported

fun main() {
    fch = ForCheck()
    print(fch is ForCheck)
    a = A()
    test(a !is imported.A)
    test(a !is B)
    test(a is A)
    test(imported.A() is imported.A)
    b = B()
    test(b is A)
    test(b is B)
    test(b !is imported.A)
}

class A {}

class B:A {}
    """
    )
}

fun addAnimal() {
    write(
        "src/commonTest/resources/animal.rgn",
        """
        //import std.geometry2D as geom
        import std.math as math
        import std.svg as svg
        import std.geometry2D as geom


        class A {
            iter = (parent?.iter ?? 0) + 1
            next = if(iter < 3) A() else null

            fun n() {
                return "a"
            }
        }

        fun main() {
          //  r = RegularPolygon(center=Point(x=50,y=50), q=7, convex=false)
           // r.svgArgs["attributes"]["fill"] = "goldenrod"
           // write(svg.createSVG(r), "result.svg")


            b = Body(center=Point(x=50,y=50))
           // b.head.eye.iris.svgArgs["attributes"]["fill"] = "beige"

           //b= Test()
         //   a = svg.createSVG(b)
           // write(a, "result.svg")
        }

        class Test {
            b = Testing(f=a)
            a = 2

            fun testing(t) {
                return t
            }
        }

        class Testing {}

        object Constants {
            palette = {"black":"#000000",\
                "blue":"#253092",\
                "red":"#bf3011",\
                "orange":"#df8022",\
                "yellow":"#e5b527",\
                "green":"#06694a",\
                "purple":"#820d7c",\
                "white":"#fffafb"}
        }

        class Body: Rectangle {
            fill = Constants.palette.values[\
                rndInt(0, Constants.palette.size-1)]
            width = rndInt(25, 50)
            height = 25
            head = if(rndInt(0,1)) Head() else RoundHead()
            leg = Limb(start = downLeft())
        }

        class Head: Polyline {
            fill = parent.fill
            points = [Point(x=parent.center.x+parent.width*rndDouble(1, 2),y=parent.center.y),\
                parent.downRight(),\
                parent.upRight()]
            eye = Eye(center=center())
        }

        class RoundHead: Polyline {
            center = parent.center.plus(\
                Point(x=parent.width/2))
            eye = Eye(center=center)
            circle = Circle(center=center,\
                r=parent.height/2,\
                fill=parent.fill)
        }

        class Eye {
            center = Point()
            pupil = Circle(r=5,center=center)
            iris = Circle(r=7,center=center,\
            fill=Constants.palette["white"])
        }

        class Limb {
            start = Point()
            first = Segment(p1=start,p2=Point(),\
                stroke=parent.fill,strokeWidth=5)\
                .rotate(rndInt(-15,15),start)
            second = Segment(p1=first.p2,\
            p2=Point(x=first.p2.x+rndInt(5,10),\
            y=first.p2.y),\
                stroke=parent.fill,strokeWidth=5)
        }

        class Flower {

        }

        object Globals {
            petalsNum = rndInt(3, 10)
        }
        class FlowerHead {
            svgArgs = {"group": true}
            iter = parent?.iter + 1

            fun createPetals() {
                this.polygon = Polyline()
                circle = []
                bigCircle = []
                bigRadius = rndInt(7, 10)
              //  foreach(i in range(1, Globals.petalsNum)) {
               //     circle.add(Point(x=parent.x + 5, y=parent.y))
               //     bigCircle.add(Point(x=parent.x + bigRadius, y=parent.y))
               //     circle[circle.size - 1].rotate(i * (360 / Global.petalsNum))
               //     bigCircle[circle.size - 1].rotate(i * (360 / Global.petalsNum))
             //   }
            }
        }

        // The maximum and the minimum are inclusive
        fun rndInt(min, max) {
            min = min.ceil()
            max = max.floor() + 1
            return (rnd() * (max - min) + min).floor()
        }

        fun rndDouble(min, max) {
            return (rnd() * (max - min) + min)
        }
    """
    )
}
