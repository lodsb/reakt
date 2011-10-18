/*
 * +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 10 - 14 :: 7 : 15
 *    >>  Origin: eventgraphtest (project) / reakt (module)
 *    >>
 *  +3>>
 *    >>  Copyright (c) 2011:
 *    >>
 *    >>     |             |     |
 *    >>     |    ,---.,---|,---.|---.
 *    >>     |    |   ||   |`---.|   |
 *    >>     `---'`---'`---'`---'`---'
 *    >>                    // Niklas Klügel
 *    >>
 *  +4>>
 *    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.lodsb.reakt.graph
// stuff you should be able to do
// have some nodes, register them with a String as "local service"
// send message to several nodes ala
//
// ("service1", 123123, "service2", "test").send
// (("service1", 123123), ("service2", "test")).send
//

// trait to be derived from for clients
trait GlobalMessage {

}


trait GlobalMessageReceiver {
	def registerMeAs(name: String) : None = {


	}

	def receive(message: Any) : None

}


// message singleton
object GlobalMessageService {
	def registerMessageReceiver(name: String, receiver: GlobalMessageReceiver) : None = {

	}

	def send(name: String, value: Any) : None = {}

}
