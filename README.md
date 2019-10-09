# StreamPi Server

This repository has the source code for StreamPi Server, which runs on the Computer, to communicate and talk to [StreamPi Client](https://github.com/ladiesman6969/streampi_client)

#### What is StreamPi?

StreamPi is an opensource, free alternative to the [Elgato Stream Deck](https://www.elgato.com/en/gaming/stream-deck), which is a fully customizable and powerful multi-purpose keyboard. 

#### Why StreamPi?

The original Stream Deck can serve as an extremely useful and powerful tool for Streamers, gamers, and professional users. However, it is not a budget friendly option for the average user. Hence, we decided to work on an opensource and cheaper alternative to the Stream Deck - The StreamPi

#### How to use StreamPi?

You can browse through a comprehensive list of tutorials [here](google.com).

**ON LINUX SYSTEMS, THE MINIMUM PORT MUST BE 1024, OR ELSE SERVER MUST BE RUN AS ROOT**

StreamPi has two branches of software - [Server](https://github.com/ladiesman6969/streampi_server) and [Client](https://github.com/ladiesman6969/streampi_client)

This repository contains the Server Side code, which can run on any desktop/laptop running Windows or Linux. 

The [client](https://github.com/ladiesman6969/streampi_client/) was originally written for the [Raspberry Pi](https://www.raspberrypi.org/), which is a very cheap and powerful Single Board Computer. However, since this is written completely in Java, it can run virtually anywhere, even on another PC!

TL:DR you can run StreamPi Server, and Client on Linux, Windows and MacOS!

Since the Raspberry Pi is a small form factor motherboard, it can be used as a keyboard, if paired with any touch screen.

#### How to install StreamPi Server?

Configuring Server is pretty easy, grab the latest build from [Releases](https://github.com/ladiesman6969/streampi_server/releases) according to your preferred Operating System, and then run the server.bat (Windows), server.sh (Linux and MacOS) to start the Server! No Installation required!

#### Requirements

* A network connection to connect to the Client, via a router, or LAN.

* **This software has been written with Java, and is designed to work with Java 11+. If you want to compile this on your own, then you need to have Java 11+ installed on your computer, along with JavaFX 11+ installed. ** [BellSoft's Liberica JDK 11](https://bell-sw.com/pages/java-11.0.4/) is preferred as it comes pre-bundled JavaFX 11.

#### Contribution

If you would like to contribute, or talk to us, or suggest anything, you can raise and issue in the repository, or if you want to talk to us, then you can simply join our [Discord Server](https://discord.gg/BExqGmk)

#### About

This was originally thought of by [Samuel Quinones](https://twitter.com/SamuelQuinones1). [I](https://twitter.com/ladiesman36069) was the one who wrote the software.

#### License

[MIT License](https://github.com/ladiesman6969/streampi_server/blob/master/LICENSE)

