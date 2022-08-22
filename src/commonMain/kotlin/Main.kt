import evaluation.Evaluation.eval

fun main(args: Array<String>) {
    eval(args[0], args.toList().subList(1, args.size))
}