package domain.specific

import domain.specific.lang.lexer.*
import domain.specific.lang.parser.Actor
import domain.specific.lang.parser.Parser

abstract class Animal {
    abstract fun sound(): String
}

class Dog : Animal() {
    override fun sound() = "Woof"
}

class Cat : Animal() {
    override fun sound() = "Meow"
}

class Bird : Animal() {
    override fun sound() = "Chirp"
}

fun main() {

    val input = """
        app MyTestService {
        
            prop1: ""
            prop2: 13.34
              
            data SomeDataName {
                volume: 150
                amount: 12
                speed: 240
            }
            
            actor SomeUserName {
                type: "http"
                auth: "true"
            }
    
            fr SomeFR_name {
                actions: (
                    accept request from SomeUserName,
                    read SomeDataName,
                    send SomeDataName to SomeUserName
                )
                frequency: 1100
            }
        }
    """

    val input1 = """
        app MyTestService {
            usersNumber: 10000000
            scaleVertically: "yes"
            latency: 99.999
            
            actor PCClient {
                type: "web-client"
            }
            actor Server1 {
                type: "service"
            }
            data SMS {
                type: "text"
                retention: 12
                unitVolume: 240
            }
            data Image {
                type: "image"
                retention: 1500
                unitVolume: 432000
            }
            data MyWebPage {
                type: "html"
                retention: 1500
                unitVolume: 432000
            }
            
            fr SomeFR_name {
                actions: (
                    accept request from PCClient,
                    read SMS,
                    work with SMS obtaining Image,
                    send Image to Server1 obtaining MyWebPage, 
                    work with MyWebPage,
                    save MyWebPage,
                    return
                )
                frequency: 1100
            }
        }
    """

    val tokens: MutableList<Token> = mutableListOf()
    val tokenTextPositions: MutableList<Int> = mutableListOf()

    val lexer = Lexer(input1)
    while (true) {
        val token = lexer.nextToken()
        if (token.second == EOF) {
            break
        } else {
            tokenTextPositions.add(token.first)
            tokens.add(token.second)
            //println(token.second.toString())
        }
    }

//    tokens.forEach {
//        println(it.toString())
//    }

    val parser = Parser(tokens)
    println(parser.parse())



}