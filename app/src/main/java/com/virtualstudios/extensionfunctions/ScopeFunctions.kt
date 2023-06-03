package com.virtualstudios.extensionfunctions


//1.let: This function is used to perform operations on a nullable object or execute a block of code only if the object is not null. The result of the operation is returned. The syntax for using let is as follows:

/*
object?.let {
    // perform operations on the object
}

fun main() {
    val message = "Hello, world!"
    val result = message.let {
        val reversedMessage = it.reversed()
        reversedMessage.toUpperCase()
    }
    println(result) // output: "!DLROW ,OLLEH"
}*/


//2. also: This function is used to perform some additional operation on an object without changing its state, and then return the object itself. The syntax for using also is as follows:

/*
object.also {
    // perform additional operation on the object
}

fun main() {
    val message = "Hello, world!"
    message.also {
        println("The original message is: $it")
    }.run {
        toUpperCase()
    }.also {
        println("The uppercase message is: $it")
    }
}*/


//3. apply: This function is used to configure the properties of an object and then return the object itself. The syntax for using apply is as follows:

/*object.apply {
    // configure the properties of the object
}
data class Person(var name: String, var age: Int)

fun main() {
    val person = Person("John", 25)
    person.apply {
        name = "Jane"
        age = 30
    }
    println(person) // output: "Person(name=Jane, age=30)"
}*/

//4. run: This function is used to perform a block of code on an object, and return the result of the block. The syntax for using run is as follows:

/*object.run {
    // perform the block of code on the object
}
fun main() {
    val message = "Hello, world!"
    val upperCaseMessage = message.run {
        toUpperCase()
    }
    println(upperCaseMessage) // output: "HELLO, WORLD!"
}*/

//5. with: This function is used to perform a block of code on an object, without the need to call the object again within the block. The syntax for using with is as follows:

/*with(object) {
    // perform the block of code on the object
}
data class Person(val name: String, var age: Int)

fun main() {
    val person = Person("John", 25)
    val greeting = with(person) {
        "Hello, $name! You are $age years old."
    }
    println(greeting) // output: "Hello, John! You are 25 years old."
}*/


//Here’s a comparison of the different scope functions:

/*
+----------+-----------------------+------------------------+------------------+
| Function | Use Case              | Return Value           | Object Reference |
+----------+-----------------------+------------------------+------------------+
| let      | Nullable objects      | Result of the operation| it               |
| also     | Side effects          | Same object            | it               |
| apply    | Object initialization | Same object            | this             |
| run      | Object operations     | Result of the block    | this             |
| with     | Object operations     | Result of the block    | this             |
+-----------+--------------------+--------------------------+------------------+*/


/*
// let
val name: String? = "John"
name?.let {
    println("The length of the name is ${it.length}")
}

// also
val list = mutableListOf<Int>()
val result = list.also {
    it.add(1)
    it.add(2)
    it.add(3)
}.sum()
println("The result is $result")

// apply
val person = Person().apply {
    name = "John"
    age = 30
}
println("The person's name is ${person.name}, and age is ${person.age}")

// run
val number = 42.run {
    println("The number is $this")
    this + 10
}
println("The result is $number")

// with
val user = User("John", "Doe")
with(user) {
    println("The user's full name is $firstName $lastName")
}*/
