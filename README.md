PacMan
======

Pac-Man demo in Clojure

Based in part on the Pong! sample by Justin Grant
and information gleaned from the Pac-Man Dossier by Jamey Pittman.
Some fruit graphics by Mikkel Christensen.

Instructions:

* 5 - insert coin
* 1 - start game
* arrow keys - move
* P - pause
* F1 or N - skip to next level
* S - toggle sound
* A - toggle anti-aliasing
* T - toggle ghost telemetry and grid
* Esc or Q - quit

To run:
* java -cp clojure-1.4.0.jar clojure.main pacman.clj

Changing the definition of maxspeed near the beginning will adjust the speed of the game to your system.

Mazes and levels can be customized using the tables near the top.