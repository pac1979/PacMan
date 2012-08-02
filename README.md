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
* F2 - reset
* S - toggle sound
* A - toggle anti-aliasing
* T - toggle ghost telemetry and grid
* Esc or Q - quit

To run:
* java -cp clojure-1.4.0.jar clojure.main pacman.clj

You can pass in a numeric parameter from 1.0 to 20.00 to adjust the speed of the game for your system. Smaller numbers for fast machines, higher number for slower machines. Example:

* java -cp clojure-1.4.0.jar clojure.main pacman.clj 7.5


Mazes and levels can be customized using the tables near the top of pacman.clj.