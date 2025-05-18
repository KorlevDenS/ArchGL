package domain.specific

import domain.specific.lang.analyzer.UsageAnalyzer
import domain.specific.lang.expander.Expander
import domain.specific.lang.generator.ArchGenerator
import domain.specific.lang.lexer.*
import domain.specific.lang.model.Application
import domain.specific.lang.model.EOF
import domain.specific.lang.model.Token
import domain.specific.lang.parser.Parser
import domain.specific.lang.uml.UmlGenerator


fun main() {

    val input1 = """
        app NewsFeedApp {
            
            usersNumber: 10000000
            latency: "low"
            onlineUsersNumber: 5000
            availability: "high"
            faultTolerance: "yes"
            
            actor User {
                type: "web-client"
            }
            
            data Message {
                type: "structuredText"
                retention: 157680000000
                unitVolume: 16033
            }
            
            fr SendMessage {
                actions: (
                    accept Message from User,
                    send it to User,
                    generate Message
                )
                frequency: 500
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
            
            fr SomeFR_name2 {
                actions: (
                    accept SMS from Server1,
                    process it obtaining Image,
                    save it
                )
                frequency: 1100
            }
            
            fr SomeFR_name {
                actions: (
                    accept SMS from PCClient,
                    process it obtaining Image,
                    send it to Server1 obtaining MyWebPage, 
                    process it,
                    save it
                )
                frequency: 1100
            }
           
        }
    """

    val input3 = """
        app NewsFeedApp {
            usersNumber: 10000000
            latency: "low"
            onlineUsersNumber: 5000
            availability: "high"
            faultTolerance: "yes"
            
            actor User {
                type: "web-client"
            }
            
            actor Admin {
                type: "service"
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
            
            fr DeletePost {
                actions: (
                    accept request from Admin,
                    read User related Post,
                    delete it
                )
                frequency: 130
            }
            
            fr PublishNewPost {
                actions: (
                    accept Post from User,
                    save it,
                    process it obtaining NewsFeed,
                    update User related it
                )
                frequency: 1100
            }
           
            fr PublishNewPost {
                actions: (
                    accept Post from User,
                    process it obtaining Notification,
                    send it to User 
                )
                frequency: 1100
            }
            
        }
    """

    val input4 = """
        app NewsFeedApp {
            
            usersNumber: 10000000
            latency: "low"
            onlineUsersNumber: 5000
            availability: "high"
            faultTolerance: "yes"
            
            actor User {
                type: "web-client"
            }
            
            data Post {
                type: "text"
                retention: 157680000000
                unitVolume: 16033
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
                    save User related it,
                    process it,
                    save User related it,
                    process it,
                    save it
                )
                frequency: 1100
            }
            
            
        }
    """

    val input5 = """
        app NewsFeedApp {
            
            usersNumber: 10000000
            latency: "low"
            onlineUsersNumber: 5000
            availability: "high"
            faultTolerance: "yes"
            
            actor User {
                type: "web-client"
            }
            
            data Message {
                type: "structuredText"
                retention: 157680000000
                unitVolume: 16033
            }
            
            data Notification {
                type: "notification"
                retention: 0
                unitVolume: 8000
            }
            
            data Photo {
                type: "image"
                retention: 157680000000
                unitVolume: 40000000
            }
            
            data Music {
                type: "audioStream"
                retention: 157680000000
                unitVolume: 60000000
            }
            
            fr SendMessage {
                actions: (
                    accept Message from User,
                    save User related it,
                    send it to User 
                )
                frequency: 500
            }
            
            fr SendMessage {
                actions: (
                    accept Message from User,
                    process it obtaining Notification,
                    send it to User
                )
                frequency: 500
            }
            
            fr SendPhoto {
                actions: (
                    accept Photo from User,
                    save User related it,
                    send it to User
                )
                frequency: 300
            }
            
            fr WatchProfilePhoto {
                actions: (
                    accept request from User,
                    read User related Photo
                )
                frequency: 30
            }
            
            fr ListenToMusic {
                actions: (
                    accept request from User,
                    read Music
                )
                frequency: 30
            }
            
        }
    """

    val input6 = """
        app MyFirstApp {
            
            usersNumber: 10000000
            latency: "low"
            onlineUsersNumber: 5000
            availability: "high"
            faultTolerance: "yes"
            
            actor User {
                type: "web-client"
            }
            
            actor Admin {
                type: "service"
            }
            
            data Message {
                type: "structuredText"
                retention: 157680000000
                unitVolume: 16033
            }
            
            data Notification {
                type: "notification"
                retention: 0
                unitVolume: 8000
            }
            
            data Music {
                type: "audioStream"
                retention: 157680000000
                unitVolume: 60000000
            }
            
            fr SendMessage {
                actions: (
                    accept Message from User,
                    save User related it,
                    send it to User 
                )
                frequency: 500
            }
            
            fr SendMessage {
                actions: (
                    accept Message from User,
                    process it obtaining Notification,
                    send it to User
                )
                frequency: 500
            }
            
            fr ListenToMusic {
                actions: (
                    accept request from User,
                    read Music
                )
                frequency: 3000
            }
            
            fr DeleteMusic {
                actions: (
                    accept request from Admin,
                    read Music,
                    delete it
                )
                frequency: 3000
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


    val parser = Parser(tokens)
    val semanticTree: Application = parser.parse()
    println(semanticTree)

    val generator = ArchGenerator(semanticTree)
    val graph = generator.generate()

    val usageAnalyzer = UsageAnalyzer(graph)
    usageAnalyzer.analyze()

    val expander = Expander(graph, semanticTree)
    expander.expand()

    val umlGenerator = UmlGenerator(graph)
    umlGenerator.generateUml()
    println(graph)


}