# EASYPUSH-CLI
### An easier way to exchange texts between your devices


This is the CLI (terminal only) client of EasyPush.<br>
EasyPush is a multi-platform and serverless (decentralized) utility to easily exchange text messages between devices through any given UDP port.<br>
No login or any account required! Not even an internet connection is necessary (as long as your devices are on the same local network).<br>
On one end, run EasyPush on the devices you want to receive notifications by just selecting any UDP port to listen to. On the other end, use EasyPush to send messages to the desired recipients by their IPs/hostnames and the port you previously chose, and all listeners of that port will be notified.

#### Use cases:
- Easily share a link between PC and phone:
Tired of copying/pasting (and then copying/pasting again) online notes to share links or texts between your devices?
EasyPush allows you to quickly move texts from one device to another.
Once a message is sent, PCs and Android phones running EasyPush are able to show a notification with the new message, along with the options to quickly copy it or browse it directly. No hassle about creating accounts or manually switching between apps or sites.

- Server automation:
With EasyPush your server can easily send you warnings even when no internet is available. You can also go the other way around, sending commands for the server to read and start your tasks.
Tired of going through SSH everytime just to start trivial jobs? Program your server to act upon the text it receives through EasyPush. You can probably achieve the same result by using REST APIs or netcat/telnet, but EasyPush makes it more simple because it's focused on only exchanging regular messages, without all the overhead and configurations needed on those other solutions. Plus, since it's multi-platform you can easily integrate Linux/Windows/Android devices with barely no configuration needed.

#### Installation:
EasyPush's CLI client can easily be installed via snap (Linux) with
```
sudo snap install easypush
```
This way, it already has a built-in Java jvm and is ready to go by simply calling "easypush" as a command.<br>
If you prefer to save a few megabytes and run it with your own jvm, simply download the JAR file from the [latest release](https://github.com/leandrocm86/easypush-cli/releases/latest) and run it with
```
java -jar easypush-cli.jar
```

#### EasyPush is also available for PCs with Java (Windows/Linux/Mac) and phones with Android.
Desktop (GUI) client: https://github.com/leandrocm86/easypush/<br>
Android client: Download  the [app on GooglePlay](https://play.google.com/store/apps/details?id=lcm.easypush), or check the [project on github](https://github.com/leandrocm86/easypush-android)<br>
All projects are free, open sourced and open to suggestions.

#### Usage:
EasyPush's CLI client can be used in two ways: sender mode and reader mode.<br>
Sender mode: **easypush \<IPs\>@\<PORT\> \<message\>** <br>
Example:
```
easypush 192.168.0.255@1050 'Hello world!'
```
On Sender mode, clients with the IPs informed (comma separated) will receive the text if they are listening on the port informed. Consider using a broadcast IP if you have many devices or if you don't know their addresses. <br>

Receiver mode: **easypush --listen \<PORT\>** <br>
Example:
```
easypush --listen 1050
```
On listener mode, EasyPush will print to standard output every text received on the port informed, until a termination by the user. You can also set the optional "--wait" parameter so the listener will wait for a given time and stop if no message arrives.<br>

