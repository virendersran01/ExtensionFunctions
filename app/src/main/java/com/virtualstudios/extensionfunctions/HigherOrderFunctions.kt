package com.virtualstudios.extensionfunctions

//Lambda


//Basic Usage of Lambda expression
var  lambdaVariable1 = {  println("Inside of the Lambda Expression")  }

//Lambda Expressions can take arguments and return values.
val lambdaVariable2: (Int, String) -> String = { a: Int, b: String -> "$a + $b" }

//There are two shorter syntax.

// 1- Skip the function type.
val lambdaVariable3 = { a: Int, b: String -> "$a + $b" }

// 2- Skip the data types inside the curly brackets
val lambdaVariable4: (Int, String) -> String = { a, b -> "$a + $b" }

//There are four function types, varying based on parameters and return types.

// 1-With Parameters and No Return Value:
val lambdaVariable5: (Int, String) -> Unit = { a: Int, b: String -> println("$a + $b") }

// 2-With Parameters and Return Value:
val lambdaVariable6: (Int, String) -> String = { a: Int, b: String -> "$a + $b" }

// 3-No Parameters and No Return Value:
val lambdaVariable7: () -> Unit = { println("No Parameters and No Return Value") }

// 4-No Parameters and Return Value:
val lambdaVariable8: () -> String = { "Return String" }

// A variable isn't always necessary, as lambda expressions can be used directly.
//println( {a: String, b: String -> "$a $b"} ("Kotlin", "Java") )


//Anonymous Function

//Syntax of Anonymous Function
/*val variableName: (FirstDataType,SecondDataType) -> ReturnType =
    fun(firstParameter,secondParameter): ReturnType { MethodBody }*/

//Example of Anonymous Function:
val variable: (String, String) -> String = fun(a, b): String {
    return "$a $b"
}

// Shorter Syntax Of Anonymous Function
/*val myVariableName = fun(FirstDataType,SecondDataType) : ReturnType { MethodBody }*/

// Shorter Syntax Of Anonymous Function:
val variable2 = fun(a:String,b:String): String { return "$a + $b" }

// When the method body contains just one statement,
// the return keyword and braces can be omitted.
val variable3 = fun(a:String,b:String): String = "$a + $b"

// Let's explore various anonymous function formats,
// depending on the parameters and return type.

// 1- With Parameters and No Return Value:
val variable4 = fun(a: String, b: String): Unit {
    println("$a $b")
}

// 2- With Parameters and Return Value:
val variable5 = fun(a: String, b: String): String {
    return "$a $b"
}

// 3- No Parameters and No Return Value:
val variable6 = fun(): Unit {
    println("No Parameters and No Return Value:")
}

// 4- No Parameters and Return Value:
val variable7 = fun(): String {
    return "Hi!"
}

fun main() {
    variable4("Hüseyin","Özkoç")
    println(variable5("Orkun","Ozan"))
    variable6()
    println(variable7())
}