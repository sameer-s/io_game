```
 ___ _ __   __ _  ___| | __ (_) ___  
/ __| '_ \ / _` |/ __| |/ / | |/ _ \ ™
\__ \ | | | (_| | (__|   < _| | (_) |
|___/_| |_|\__,_|\___|_|\_(_)_|\___/
```

snack.io (pronounced sanic.io) is the most innovative bestest garne on the black murket

##What is Snack.io?

Snack.io is an android garne inspired by web based .io garnes like agar.io & slither.io. You can connect with up to 7 friends over the  _**EXCLUSIVE**_ WiFi Direct service to play Snack.io. The Snack.io is simple but fun, a team of chasers try to catch a team of runners in a big room before time runs out; Snack.io also has a vareity of powerups to add a balance and spectacle. 

# Packages:
## `io.github.sunsetsucks.iogame`
The main package. Includes the `MainActivity`, which is the starting place for our code and governs all the other classes in our code. It also contains the `Util` class, which has lots of utility methods to allow us to perform certain actions outside of the `MainActivity` class.

## `io.github.sunsetsucks.iogame.network`
The package which handles, mainly, the four networking threads for each connection, namely `UDPReadThread`, `UDPWriteThread`, `TCPReadThread`, `TCPWriteThread`. These are all combined and handled by the `NetworkConnection` class, which sends any incoming message from the read threads to an instance of `NetworkHandler`. In this specific case, `MainActivity implements NetworkHandler`. Additionally, the server (host device) initially spawns a `ServerListenerThread`, which opens a TCP `ServerSocket` to the network, and accepts input from client sockets and creates a TCP/UDP `NetworkConnection` instance.

## `io.github.sunsetsucks.iogame.rendering`
Handles the creation and rendering of certain shapes, or, more generally `GameObjects`. Currently, the `GameObject` class holds the state (translation, rotation, and scale) of a shape, while its subclass, `Shape` describes how to render it to OpenGL ES, using vertices, vertex indices, and UV coordinates, for texturing. `Square` is the only `Shape` subclass we used, and is a very simple subclass of `Shape`, with only four vertices. `IOGameGLSurfaceView`is the heart of the garne, which uses the rendering information provided by its constituent `GameObject`s to render the game. It also applies transforms to these objects, depending on user touch input and information provided over the network.

## `io.github.sunsetsucks.iogame.rendering.player`
The main class in this package, is, aptly, `Player`. It mainly handles the powering up of players, but, even more importantly, provides the necessary bitmaps, to reduce the player information UDP packets sent to nine bytes. Its two subclasses, `Chaser` and `Runner` mainly take care of texturing the players and also the timers for when they were powered up

## `io.github.sunsetsucks.iogame.rendering.powerup`
In the garne, the chasers and runners are able to pick up powerups. The available powerups are pizza (`SpeedUpPowerup`), tomatoes (‘SpeedDownPowerup’), burgers (‘GrowthPowerup’), and cookies (‘InvincibilityPowerup’). The pizza and cookies can only be picked up by the runners, while the tomatoes and burgers are for the chasers.

#Note from the authors:
Linux rules, Windows drools
