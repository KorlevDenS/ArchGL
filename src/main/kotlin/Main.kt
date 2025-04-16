package domain.specific

import domain.specific.lang.generator.ArchGenerator
import domain.specific.lang.lexer.*
import domain.specific.lang.model.Application
import domain.specific.lang.model.EOF
import domain.specific.lang.model.Token
import domain.specific.lang.parser.Parser
import domain.specific.lang.uml.UmlGenerator


fun main() {

    val input = """
        app MyTestService {
        
          
              
            data SomeDataName {
                
            }
            
            actor SomeUserName {
                
            }
    
            fr SomeFR_name {
                actions: (
                    accept request from SomeUserName,
                    read SomeDataName,
                    send it to SomeUserName
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
                    work with it obtaining Image,
                    send it to Server1 obtaining MyWebPage, 
                    work with it,
                    save it,
                    return
                )
                frequency: 1100
            }
            
            fr SecondFR_name {
                actions: (
                    accept request from PCClient,
                    return
                )
                frequency: 2100
            }
        }
    """

    val input2 = """
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
                    accept SMS from PCClient,
                    work with it obtaining Image,
                    send it to Server1 obtaining MyWebPage, 
                    work with it,
                    save it,
                    return
                )
                frequency: 1100
            }
           
        }
    """

    val input3 = """
        app NewsFeedApp {
            usersNumber: 10000000
            scaleVertically: "no"
            scaleHorizontally: "yes"
            latency: 99.999
            dayUsersNumber: 500000
            availability: 98.5
            
            actor User {
                type: "web-client"
            }
            
            data Post {
                type: "text"
                retention: 157680000000
                unitVolume: 16000
            }
            
            data NewsFeed {
                type: "text"
                retention: 60000
                unitVolume: 480000
            }
            
            data Notification {
                type: "text"
                retention: 0
                unitVolume: 320
            }
            
            fr PublishNewPost {
                actions: (
                    accept Post from User,
                    save it,
                    return
                )
                frequency: 1100
            }
            
            fr UpdateUsersNewsFeed {
                actions: (
                    accept Post from User,
                    work with it obtaining NewsFeed,
                    update User related it
                )
                frequency: 1100
            }
            
            fr SendNotificationAfterNewPost {
                actions: (
                    accept Post from User,
                    work with it obtaining Notification,
                    send it to User
                )
                frequency: 1100
            }
            
        }
    """

    val tokens: MutableList<Token> = mutableListOf()
    val tokenTextPositions: MutableList<Int> = mutableListOf()

    val lexer = Lexer(input3)
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


    val parser = Parser(tokens)
    val semanticTree: Application = parser.parse()
    println(semanticTree)

    val generator = ArchGenerator(semanticTree)
    val graph = generator.generate()
    val umlGenerator = UmlGenerator(graph)
    umlGenerator.generateUml()
    println(graph)


}