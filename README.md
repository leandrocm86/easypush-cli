# LANPUSH-CLI
### Share texts through LAN


This is the CLI (terminal only) client of LANPUSH.<br>
LANPUSH is a utility to exchange text messages between devices in the same local network.<br>
No internet required! No login! Just send a message and all configured devices in the same LAN will be notified with it.

#### LANPUSH is available for PCs with Java (Windows/Linux/Mac) and phones with Android.
Desktop client: https://github.com/leandrocm86/lanpush/<br>
Android client: Download  the [app on GooglePlay](https://play.google.com/store/apps/details?id=lcm.lanpush), or check the [project on github](https://github.com/leandrocm86/lanpush-android)<br>
All projects are free, open sourced and open to suggestions.

#### Use cases:
- Share a link between PC and phone:
Tired of copying/pasting (and then copying/pasting again) online notes to share links or texts between your devices?
LANPUSH allows you to quickly move texts from one device to another.
Once a message is sent, all PCs and Android phones on the same network will be able to show a notification with the new message, along with the option to copy it or browse it.

- Server automation:
With LANPUSH your server can easily send you warnings even when no internet is available.
You can also go the other way around, sending commands for the server to read and start your tasks.
Tired of going through SSH everytime just to start trivial jobs? Make your server act upon the messages it receives. You can configure lanpush to log messages on a predefined file, and also choose IPs and ports to listen to.

#### Usage:

LANPUSH's CLI client can be used in two ways: sender mode and reader mode.<br>
Sender mode: lanpush-cli <IPs>:<PORT> <message>. Example: <br>
```
lanpush-cli 192.168.0.255:1050 'Hello world!'
```
On Sender mode, clients with the IPs informed (comma separated) will receive the text if they are listening on the port informed. Consider using a broadcast IP if you have many devices. <br>

Receiver mode: lanpush-cli --listen <PORT>. Example: <br>
```
lanpush-cli --listen 1050
```
On listener mode, lanpush will print to standard output every text received on the port informed, until a termination by the user.<br>

