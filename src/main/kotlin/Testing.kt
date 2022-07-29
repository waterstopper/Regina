import kotlin.random.Random

const val SEED = 42

fun main() {
    val rnd = Random(SEED)
    val a = hashMapOf<Int,String>()
    println(rnd.nextInt())
    a[1] ="1"
    println(rnd.nextInt())
//972016666
//1740578880
}