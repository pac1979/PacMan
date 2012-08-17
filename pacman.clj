;;;; 
;;;; Pac-Man
;;;;

(ns arcade.pacman
  (:require clojure.java.io
            [clojure.set :refer :all])
  (:import (java.awt Color Toolkit Font GraphicsEnvironment Graphics2D RenderingHints BasicStroke Rectangle Polygon Cursor Robot)
           (java.awt.image BufferStrategy BufferedImage)
           (java.awt.event ActionListener MouseMotionListener KeyListener MouseEvent KeyEvent)
           (javax.imageio ImageIO)
           (javax.swing JFrame Timer)
           (java.applet Applet)
           (java.io File FileInputStream BufferedInputStream)
           (java.net URL)
           (javax.sound.sampled AudioSystem AudioInputStream DataLine$Info SourceDataLine Clip
                              AudioFormat AudioFormat$Encoding AudioFileFormat$Type)
           (java.util Date)))

(def customspeed (first (map read-string *command-line-args*)))
(def ^:const maxspeed (if customspeed (double customspeed) 5.7)) ;; higher for slow machines, lower for fast machines

(def mazes [[   ; X,x = in pen, ^ = forbidden up for ghosts, * = forbidden with dot, < = horizontal tunnel exits, # = slow areas
		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄▄▄╗"
		"▐············║│············▌"
		"▐·┌══┐·┌═══┐·║│·┌═══┐·┌══┐·▌"
		"▐●║██│·║███│·║│·║███│·║██│●▌"
		"▐·└──┘·└───┘·└┘·└───┘·└──┘·▌"
		"▐··························▌"
		"▐·┌══┐·┌┐·┌══════┐·┌┐·┌══┐·▌"
		"▐·└──┘·║│·└──╖┼──┘·║│·└──┘·▌"
		"▐······║│····║│····║│······▌"
		"╚▀▀▀▀┐·║╘══┐ ║│ ┌══╬│·┌▀▀▀▀╝"
		"     ▐·║┼──┘ └┘ └──╖│·▌     "
		"     ▐·║│   ^  ^   ║│·▌     "
		"     ▐·║│ ┌▀►--◄▀┐ ║│·▌     "
		"▄▄▄▄▄┘·└┘ ▌xxXXxx▐ └┘·└▄▄▄▄▄"
		"<#####·   ▌xXXXXx▐   ·#####<"
		"▀▀▀▀▀┐·┌┐ ▌xxxxxx▐ ┌┐·┌▀▀▀▀▀"
		"     ▐·║│ └▄▄▄▄▄▄┘ ║│·▌     "
		"     ▐·║│          ║│·▌     "
		"     ▐·║│ ┌══════┐ ║│·▌     "
		"╔▄▄▄▄┘·└┘ └──╖┼──┘ └┘·└▄▄▄▄╗"
		"▐············║│············▌"
		"▐·┌══┐·┌═══┐·║│·┌═══┐·┌══┐·▌"
		"▐·└─╖│·└───┘·└┘·└───┘·║┼─┘·▌"
		"▐●··║│······*  *······║│··●▌"
		"╙═┐·║│·┌┐·┌══════┐·┌┐·║│·┌═╜"
		"╟─┘·└┘·║│·└──╖┼──┘·║│·└┘·└─╢"
		"▐······║│····║│····║│······▌"
		"▐·┌════╬╘══┐·║│·┌══╬╘════┐·▌"
		"▐·└────────┘·└┘·└────────┘·▌"
		"▐··························▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄╗"
		"▐······║│··········║│······▌"
		"▐●┌══┐·║│·┌══════┐·║│·┌══┐●▌"
		"▐·└──┘·└┘·└──────┘·└┘·└──┘·▌"
		"▐··························▌"
		"╚▀┐·┌┐·┌═══┐·┌┐·┌═══┐·┌┐·┌▀╝"
		"  ▐·║│·║███│·║│·║███│·║│·▌  "
		"▄▄┘·║│·└───┘·║│·└───┘·║│·└▄▄"
		"<##·║│·······║│·······║│·##<"
		"▀▀┐·║╘══┐ ┌══╬╘══┐ ┌══╬│·┌▀▀"
		"  ▐·└───┘ └──────┘ └───┘·▌  "
		"  ▐·                    ·▌  "
		"  ▐·┌═══┐ ┌▀►--◄▀┐ ┌═══┐·▌  "
		"  ▐·║┼──┘ ▌xxXXxx▐ └──╖│·▌  "
		"  ▐·║│    ▌xXXXXx▐    ║│·▌  "
		"  ▐·║│ ┌┐ ▌xxxxxx▐ ┌┐ ║│·▌  "
		"▄▄┘·└┘ ║│ └▄▄▄▄▄▄┘ ║│ └┘·└▄▄"
		"<##·   ║│          ║│   ·##<"
		"▀▀┐·┌══╬╘══┐ ┌┐ ┌══╬╘══┐·┌▀▀"
		"  ▐·└──────┘ ║│ └──────┘·▌  "
		"  ▐·······   ║│   ·······▌  "
		"  ▐·┌═══┐·┌══╬╘══┐·┌═══┐·▌  "
		"╔▄┘·└───┘·└──────┘·└───┘·└▄╗"
		"▐············  ············▌"
		"▐·┌══┐·┌═══┐·┌┐·┌═══┐·┌══┐·▌"
		"▐·║██│·║┼──┘·║│·└──╖│·║██│·▌"
		"▐·║██│·║│····║│····║│·║██│·▌"
		"▐●║██│·║│·┌══╬╘══┐·║│·║██│●▌"
		"▐·└──┘·└┘·└──────┘·└┘·└──┘·▌"
		"▐··························▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄"
		"<######║│··········║│######<"
		"═════┐ ║│·┌══════┐·║│ ┌═════"
		"╟────┘ └┘·└──╖┼──┘·└┘ └────╢"
		"▐●···········║│···········●▌"
		"▐·┌═════┐·┌┐·║│·┌┐·┌═════┐·▌"
		"▐·║┼────┘·║│·║│·║│·└────╖│·▌"
		"▐·║│······║│·└┘·║│······║│·▌"
		"▐·║│·┌══┐ ║│····║│ ┌══┐·║│·▌"
		"▐·└┘·└─╖│ ║╘════╬│ ║┼─┘·└┘·▌"
		"▐······║│ └──────┘ ║│······▌"
		"╙════┐·║│          ║│·┌════╜"
		"╟────┘·║│ ┌▀►--◄▀┐ ║│·└────╢"
		"▐······║│ ▌xxXXxx▐ ║│······▌"
		"▐·┌══┐·└┘ ▌xXXXXx▐ └┘·┌══┐·▌"
		"▐·└─╖│·   ▌xxxxxx▐   ·║┼─┘·▌"
		"▐···║│·┌┐ └▄▄▄▄▄▄┘ ┌┐·║│···▌"
		"╚▀┐·║│·║│          ║│·║│·┌▀╝"
		"  ▐·║│·║╘═┐ ┌══┐ ┌═╬│·║│·▌  "
		"  ▐·└┘·└──┘ ║██│ └──┘·└┘·▌  "
		"  ▐·········║██│·········▌  "
		"  ▐·┌═════┐·║██│·┌═════┐·▌  "
		"▄▄┘·└──╖┼─┘·└──┘·└─╖┼──┘·└▄▄"
		"<##····║│···    ···║│····##<"
		"══┐·┌┐·║│·┌══════┐·║│·┌┐·┌══"
		"╟─┘·║│·└┘·└──╖┼──┘·└┘·║│·└─╢"
		"▐●··║│·······║│·······║│··●▌"
		"▐·┌═╬│·┌═══┐·║│·┌═══┐·║╘═┐·▌"
		"▐·└──┘·└───┘·└┘·└───┘·└──┘·▌"
		"▐··························▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄╗"
		"▐·········║│····║│·········▌"
		"▐·┌═════┐·║│·┌┐·║│·┌═════┐·▌"
		"▐●║┼────┘·└┘·║│·└┘·└────╖│●▌"
		"▐·║│·········║│·········║│·▌"
		"▐·└┘·┌┐·┌══┐·║│·┌══┐·┌┐·└┘·▌"
		"▐····║│·║██│·║│·║██│·║│····▌"
		"╙══┐·║│·└──┘·└┘·└──┘·║│·┌══╜"
		"───┘·║│··············║│·└───"
		"<····║╘═┐ ┌══════┐ ┌═╬│····<"
		"┐·┌┐ └──┘ └──────┘ └──┘ ┌┐·┌"
		"▐·║│                    ║│·▌"
		"▐·║╘═┐ ┌┐ ┌▀►--◄▀┐ ┌┐ ┌═╬│·▌"
		"▐·└──┘ ║│ ▌xxXXxx▐ ║│ └──┘·▌"
		"▐·     ║│ ▌xXXXXx▐ ║│     ·▌"
		"▐·┌┐ ┌═╬│ ▌xxxxxx▐ ║╘═┐ ┌┐·▌"
		"▐·║│ └──┘ └▄▄▄▄▄▄┘ └──┘ ║│·▌"
		"▐·║│                    ║│·▌"
		"▐·║╘═┐ ┌═══┐ ┌┐ ┌═══┐ ┌═╬│·▌"
		"▐·└──┘ ║┼──┘ ║│ └──╖│ └──┘·▌"
		"▐······║│····║│····║│······▌"
		"╙═┐·┌┐·║│·┌══╬╘══┐·║│·┌┐·┌═╜"
		"╟─┘·║│·└┘·└──────┘·└┘·║│·└─╢"
		"▐●··║│·······  ·······║│··●▌"
		"▐·┌═╬│·┌═══┐·┌┐·┌═══┐·║╘═┐·▌"
		"▐·└──┘·║┼──┘·║│·└──╖│·└──┘·▌"
		"▐······║│····║│····║│······▌"
		"▐·┌══┐·║│·┌══╬╘══┐·║│·┌══┐·▌"
		"▐·└──┘·║│·└──────┘·║│·└──┘·▌"
		"▐······║│··········║│······▌"
		"╚▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄╗"
		"▐··························▌"
		"▐·┌┐·┌══┐·┌══════┐·┌══┐·┌┐·▌"
		"▐●║│·║██│·║┼────╖│·║██│·║│●▌"
		"▐·║│·└──┘·║│····║│·└──┘·║│·▌"
		"▐·║│······║│·┌┐·║│······║│·▌"
		"▐·║╘═┐·┌┐·║│·║│·║│·┌┐·┌═╬│·▌"
		"▐·└──┘·║│·└┘·║│·└┘·║│·└──┘·▌"
		"▐······║│····║│····║│······▌"
		"╚▀┐·┌══╬╘══┐ ║│ ┌══╬╘══┐·┌▀╝"
		"  ▐·└──╖┼──┘ └┘ └──╖┼──┘·▌  "
		"  ▐····║│          ║│····▌  "
		"▄▄┘ ┌┐·║│ ┌▀►--◄▀┐ ║│·┌┐ └▄▄"
		"<###║│·└┘ ▌xxXXxx▐ └┘·║│###<"
		"════╬│·   ▌xXXXXx▐   ·║╘════"
		"────╖│·┌┐ ▌xxxxxx▐ ┌┐·║┼────"
		"<###║│·║│ └▄▄▄▄▄▄┘ ║│·║│###<"
		"▀▀┐ └┘·║│          ║│·└┘ ┌▀▀"
		"  ▐····║╘══┐ ┌┐ ┌══╬│····▌  "
		"  ▐·┌┐·└───┘ ║│ └───┘·┌┐·▌  "
		"  ▐·║│······ ║│ ······║│·▌  "
		"  ▐·║╘══┐·┌┐ ║│ ┌┐·┌══╬│·▌  "
		"╔▄┘·└───┘·║│ └┘ ║│·└───┘·└▄╗"
		"▐·········║│    ║│·········▌"
		"▐·┌══┐·┌┐·║╘════╬│·┌┐·┌══┐·▌"
		"▐·║┼─┘·║│·└──────┘·║│·└─╖│·▌"
		"▐·║│···║│··········║│···║│·▌"
		"▐●║│·┌═╬╘══┐·┌┐·┌══╬╘═┐·║│●▌"
		"▐·└┘·└─────┘·║│·└─────┘·└┘·▌"
		"▐············║│············▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"╔▄▄▄▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄▄▄▄╗"
		"▐·············║│··········║│··········║│·············▌"
		"▐·┌┐·┌═══┐·┌┐·║│·┌┐ ┌═══┐·║│·┌═══┐ ┌┐·║│·┌┐·┌═══┐·┌┐·▌"
		"▐·║│·└───┘·║│·└┘·║│ └──╖│·║│·║┼──┘ ║│·└┘·║│·└───┘·║│·▌"
		"▐●║│·······║│····║│····║│·║│·║│····║│····║│·······║│●▌"
		"▐·║│·┌═══┐·║│·┌══╬│·┌┐·║│·║│·║│·┌┐·║╘══┐·║│·┌═══┐·║│·▌"
		"▐·└┘·└───┘·║│·║┼──┘·║│·└┘·└┘·└┘·║│·└──╖│·║│·└───┘·└┘·▌"
		"▐·  ·······║│·║│····║│··········║│····║│·║│·······  ·▌"
		"▐·┌┐·┌═══┐·║│·║│·┌══╬│ ┌══════┐ ║╘══┐·║│·║│·┌═══┐·┌┐·▌"
		"▐·║│·└──╖│·└┘·└┘·└───┘ └──────┘ └───┘·└┘·└┘·║┼──┘·║│·▌"
		"▐·║│····║│·  ·······              ·······  ·║│····║│·▌"
		"▐·║│·┌┐·║│·┌┐·┌═══┐·┌┐ ┌▀►--◄▀┐ ┌┐·┌═══┐·┌┐·║│·┌┐·║│·▌"
		"▐·└┘·║│·└┘·║│·└───┘·║│ ▌xxXXxx▐ ║│·└───┘·║│·└┘·║│·└┘·▌"
		"▐····║│····║│···●···║│ ▌xXXXXx▐ ║│···●···║│····║│····▌"
		"▐·┌══╬╘══┐·║│·┌═══┐·║│ ▌xxxxxx▐ ║│·┌═══┐·║│·┌══╬╘══┐·▌"
		"▐·└──────┘·║│·└───┘·└┘ └▄▄▄▄▄▄┘ └┘·└───┘·║│·└──────┘·▌"
		"▐·  ·······║│·······              ·······║│·······  ·▌"
		"▐·┌┐·┌═══┐·║│·┌┐ ┌═══┐ ┌══════┐ ┌═══┐ ┌┐·║│·┌═══┐·┌┐·▌"
		"▐·║│·└──╖│·║│·║│ └───┘ └──╖┼──┘ └───┘ ║│·║│·║┼──┘·║│·▌"
		"▐·║│····║│·║│·║│   ·······║│·······   ║│·║│·║│····║│·▌"
		"▐·║╘══┐·║│·║│·║╘══┐·┌═══┐·║│·┌═══┐·┌══╬│·║│·║│·┌══╬│·▌"
		"▐·└───┘·└┘·└┘·└───┘·└───┘·└┘·└───┘·└───┘·└┘·└┘·└───┘·▌"
		"▐·························  ·························▌"
		"▐·┌┐·┌═══┐ ┌═══┐·┌┐·┌┐ ┌══════┐ ┌┐·┌┐·┌═══┐ ┌═══┐·┌┐·▌"
		"▐·║│·║┼──┘ └───┘·║│·║│ └──╖┼──┘ ║│·║│·└───┘ └──╖│·║│·▌"
		"▐●║│·║│·······  ·║│·║│····║│····║│·║│·  ·······║│·║│●▌"
		"▐·║│·║│·┌═══┐·┌┐·║│·║│·┌┐·║│·┌┐·║│·║│·┌┐·┌═══┐·║│·║│·▌"
		"▐·└┘·└┘·└───┘·║│·└┘·└┘·║│·└┘·║│·└┘·└┘·║│·└───┘·└┘·└┘·▌"
		"▐·············║│·······║│····║│·······║│·············▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀╛╧▀▀▀▀╛╧▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"   ╔▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄╗"
		"   ▐··············································▌"
		"   ▐·┌══════┐·┌┐·┌═══┐·┌══════┐·┌═══┐·┌┐·┌══════┐·▌"
		"╔▄▄┘·└─────╖│·║│·└───┘·└──╖┼──┘·└───┘·║│·║┼─────┘·└▄▄╗"
		"▐··········║│·║│··········║│··········║│·║│··········▌"
		"▐·┌══════┐·║│·║╘════════┐·║│·┌════════╬│·║│·┌══════┐·▌"
		"▐●└─────╖│·└┘·└─────────┘·║│·└─────────┘·└┘·║┼─────┘●▌"
		"▐·······║│················║│················║│·······▌"
		"▐·┌═══┐·║│·┌┐·┌══════┐·┌══╬╘══┐·┌══════┐·┌┐·║│·┌═══┐·▌"
		"▐·└──╖│·║│·║│·└─────╖│·└──────┘·║┼─────┘·║│·║│·║┼──┘·▌"
		"▐····║│·║│·║│·······║│·        ·║│·······║│·║│·║│····▌"
		"▐·┌┐·║│·║│·║│·┌┐·┌┐·║│·┌▀►--◄▀┐·║│·┌┐·┌┐·║│·║│·║│·┌┐·▌"
		"▐·║│·└┘·└┘·└┘·║│·║│·║│·▌xxXXxx▐·║│·║│·║│·└┘·└┘·└┘·║│·▌"
		"▐·║│··········║│●║│·║│·▌xXXXXx▐·║│·║│●║│··········║│·▌"
		"▐·║╘══┐·┌═══┐·║│·║│·║│·▌xxxxxx▐·║│·║│·║│·┌═══┐·┌══╬│·▌"
		"▐·└──╖│·└──╖│·║│·║│·└┘·└▄▄▄▄▄▄┘·└┘·║│·║│·║┼──┘·║┼──┘·▌"
		"▐····║│····║│·║│·║│····        ····║│·║│·║│····║│····▌"
		"▐·┌┐·║╘══┐·║│·║│·║╘═════┐ ┌┐ ┌═════╬│·║│·║│·┌══╬│·┌┐·▌"
		"▐·║│·└──╖│·└┘·║│·└──────┘ ║│ └──────┘·║│·└┘·║┼──┘·║│·▌"
		"▐·║│····║│····║│·······   ║│   ·······║│····║│····║│·▌"
		"▐·║│·┌┐·║╘══┐·║│·┌═══┐·┌══╬╘══┐·┌═══┐·║│·┌══╬│·┌┐·║│·▌"
		"▐·└┘·║│·└───┘·└┘·║███│·└──────┘·║███│·└┘·└───┘·║│·└┘·▌"
		"▐····║│··········║███│····  ····║███│··········║│····▌"
		"╙══┐·║│·┌═══┐·┌┐·║███│·┌┐·┌┐·┌┐·║███│·┌┐·┌═══┐·║│·┌══╜"
		"╟──┘·║│·║┼──┘·║│·└───┘·║│·║│·║│·└───┘·║│·└──╖│·║│·└──╢"
		"▐····║│·║│····║│·······║│·║│·║│·······║│····║│·║│····▌"
		"▐●┌══╬│·║│·┌┐·║╘══┐·┌══╬│·║│·║╘══┐·┌══╬│·┌┐·║│·║╘══┐●▌"
		"▐·└───┘·└┘·║│·└───┘·└───┘·└┘·└───┘·└───┘·║│·└┘·└───┘·▌"
		"▐··········║│····························║│··········▌"
		"╚▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"╔▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄╗"
		"▐················║│·······║│·······║│················▌"
		"▐·┌═══┐·┌┐·┌┐·┌┐·║│·┌┐·┌┐·║│·┌┐·┌┐·║│·┌┐·┌┐·┌┐·┌═══┐·▌"
		"▐·║┼──┘·║│·║│·└┘·└┘·║│·└┘·└┘·└┘·║│·└┘·└┘·║│·║│·└──╖│·▌"
		"▐●║│····║│·║│·······║│··········║│·······║│·║│····║│●▌"
		"▐·║│·┌══╬│·║╘══┐·┌══╬│ ┌══════┐ ║╘══┐·┌══╬│·║╘══┐·║│·▌"
		"▐·└┘·└──╖│·└───┘·└──╖│ └──╖┼──┘ ║┼──┘·└───┘·║┼──┘·└┘·▌"
		"▐·······║│······●   ║│    ║│    ║│   ●······║│·······▌"
		"▐·┌═══┐·║╘══┐·┌┐·┌┐ ║╘══┐ ║│ ┌══╬│ ┌┐·┌┐·┌══╬│·┌═══┐·▌"
		"▐·└──╖│·└───┘·║│·║│ └───┘ └┘ └───┘ ║│·║│·└───┘·║┼──┘·▌"
		"▐····║│·······║│·║│                ║│·║│·······║│····▌"
		"▐·┌┐·║╘══┐·┌══╬│·║╘══┐ ┌▀►--◄▀┐ ┌══╬│·║╘══┐·┌══╬│·┌┐·▌"
		"▐·║│·║┼──┘·└──╖│·└───┘ ▌xxXXxx▐ └───┘·║┼──┘·└──╖│·║│·▌"
		"▐·║│·║│·······║│····   ▌xXXXXx▐   ····║│·······║│·║│·▌"
		"▐·║│·║│·┌═══┐·║╘══┐·┌┐ ▌xxxxxx▐ ┌┐·┌══╬│·┌═══┐·║│·║│·▌"
		"▐·└┘·└┘·└──╖│·└───┘·║│ └▄▄▄▄▄▄┘ ║│·└───┘·║┼──┘·└┘·└┘·▌"
		"▐··········║│·······║│          ║│·······║│··········▌"
		"▐·┌┐·┌═══┐·║╘══┐·┌══╬│ ┌══════┐ ║╘══┐·┌══╬│·┌═══┐·┌┐·▌"
		"▐·║│·└──╖│·║┼──┘·└──╖│ └──╖┼──┘ ║┼──┘·└──╖│·║┼──┘·║│·▌"
		"▐·║│····║│·║│·······║│    ║│    ║│·······║│·║│····║│·▌"
		"▐·║╘══┐·║│·║│·┌═══┐·║╘══┐ ║│ ┌══╬│·┌═══┐·║│·║│·┌══╬│·▌"
		"▐·└───┘·└┘·└┘·└──╖│·└───┘ └┘ └───┘·║┼──┘·└┘·└┘·└───┘·▌"
		"▐················║│·······  ·······║│················▌"
		"╙══┐·┌┐·┌┐·┌═══┐·║╘══┐·┌┐·┌┐·┌┐·┌══╬│·┌═══┐·┌┐·┌┐·┌══╜"
		"╟──┘·║│·║│·└──╖│·║┼──┘·║│·└┘·║│·└──╖│·║┼──┘·║│·║│·└──╢"
		"▐····║│·║│····║│·║│····║│····║│····║│·║│····║│·║│····▌"
		"▐●┌══╬│·║╘══┐·║│·║│·┌══╬│·┌┐·║╘══┐·║│·║│·┌══╬│·║╘══┐●▌"
		"▐·└───┘·└───┘·└┘·└┘·└───┘·║│·└───┘·└┘·└┘·└───┘·└───┘·▌"
		"▐·························║│·························▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"   ╔▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╗"
		"   ▐··········║│······················║│··········▌"
		"   ▐·┌┐·┌┐ ┌┐·║│·┌┐·┌┐·┌┐·┌┐·┌┐·┌┐·┌┐·║│·┌┐ ┌┐·┌┐·▌"
		"╔▄▄┘·║│·║│ └┘·└┘·└┘·║│·║│·║│·║│·║│·└┘·└┘·└┘ ║│·║│·└▄▄╗"
		"▐●···║│·║│····  ····║│·║│·║│·║│·║│····  ····║│·║│···●▌"
		"▐·┌══╬│·║│·┌════════╬│·║│·║│·║│·║╘════════┐·║│·║╘══┐·▌"
		"▐·└───┘·║│·║┼────────┘·└┘·║│·└┘·└────────╖│·║│·└───┘·▌"
		"▐·······║│·║│·············║│·············║│·║│·······▌"
		"╙═════┐·║│·║│·┌═════════┐ ║│ ┌═════════┐·║│·║│·┌═════╜"
		"╟────╖│·║│·└┘·└─────╖┼──┘ └┘ └──╖┼─────┘·└┘·║│·║┼────╢"
		"▐····║│·║│··········║│          ║│··········║│·║│····▌"
		"▐·┌┐·║│·║╘══┐·┌┐·┌┐·║│ ┌▀►--◄▀┐ ║│·┌┐·┌┐·┌══╬│·║│·┌┐·▌"
		"▐·║│·└┘·└───┘·║│·║│·└┘ ▌xxXXxx▐ └┘·║│·║│·└───┘·└┘·║│·▌"
		"▐·║│·········●║│·║│·   ▌xXXXXx▐   ·║│·║│●·········║│·▌"
		"▐·║╘════════┐·║│·║│·┌┐ ▌xxxxxx▐ ┌┐·║│·║│·┌════════╬│·▌"
		"▐·└────────╖│·║│·└┘·║│ └▄▄▄▄▄▄┘ ║│·└┘·║│·║┼────────┘·▌"
		"▐··········║│·║│····║│          ║│····║│·║│··········▌"
		"▐·┌══════┐·║│·║╘════╬╘══┐ ┌┐ ┌══╬╘════╬│·║│·┌══════┐·▌"
		"▐·└─────╖│·║│·└────────╖│ ║│ ║┼────────┘·║│·║┼─────┘·▌"
		"▐·······║│·║│··········║│ ║│ ║│··········║│·║│·······▌"
		"╙═════┐·║│·║│·┌══════┐·║│ ║│ ║│·┌══════┐·║│·║│·┌═════╜"
		"╟─────┘·└┘·└┘·└─────╖│·└┘ └┘ └┘·║┼─────┘·└┘·└┘·└─────╢"
		"▐·············  ····║│····  ····║│····  ·············▌"
		"▐·┌┐·┌┐·┌┐·┌┐·┌┐·┌┐·║│ ┌┐·┌┐·┌┐ ║│·┌┐·┌┐·┌┐·┌┐·┌┐·┌┐·▌"
		"▐·║│·║│·║│·║│·║│·║│·║│ ║│·║│·║│ ║│·║│·║│·║│·║│·║│·║│·▌"
		"▐·║│·║│·║│·║│·║│·║│·║│ ║│·║│·║│ ║│·║│·║│·║│·║│·║│·║│·▌"
		"▐·║│·║│·║│·║│·║│·║│·║│ ║│·║│·║│ ║│·║│·║│·║│·║│·║│·║│·▌"
		"▐·└┘·└┘·└┘·└┘·║│·└┘·└┘ └┘·║│·└┘ └┘·└┘·║│·└┘ └┘·└┘·└┘·▌"
		"▐●············║│··········║│··········║│············●▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"╔▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╗"
		"▐··········║│····║│················║│····║│··········▌"
		"▐·┌═══┐·┌┐·║│·┌┐·║│·┌═══┐·┌┐·┌═══┐·║│·┌┐·║│·┌┐·┌═══┐·▌"
		"▐·└───┘·║│·└┘·║│·└┘·└───┘·║│·└───┘·└┘·║│·└┘·║│·└───┘·▌"
		"▐···●···║│····║│··········║│··········║│····║│···●···▌"
		"▐·┌┐·┌┐·║│·┌══╬│·┌═══┐ ┌┐·║│·┌┐ ┌═══┐·║╘══┐·║│·┌┐·┌┐·▌"
		"▐·║│·║│·└┘·└───┘·└───┘ ║│·└┘·║│ └───┘·└───┘·└┘·║│·║│·▌"
		"▐·║│·║│·  ·  ·······   ║│····║│   ·······  ·  ·║│·║│·▌"
		"▐·║│·║│·┌┐·┌┐·┌═══┐·┌┐ ║╘════╬│ ┌┐·┌═══┐·┌┐·┌┐·║│·║│·▌"
		"▐·└┘·└┘·║│·║│·└───┘·║│ └──────┘ ║│·└───┘·║│·║│·└┘·└┘·▌"
		"▐····  ·║│·║│·······║│          ║│·······║│·║│·  ····▌"
		"╙══┐·┌┐·║│·║│·┌┐·┌┐·║│ ┌▀►--◄▀┐ ║│·┌┐·┌┐·║│·║│·┌┐·┌══╜"
		"╟──┘·║│·└┘·└┘·║│·║│·└┘ ▌xxXXxx▐ └┘·║│·║│·└┘·└┘·║│·└──╢"
		"▐····║│·  ·  ·║│●║│·   ▌xXXXXx▐   ·║│●║│·  ·  ·║│····▌"
		"▐·┌┐·║│·┌┐·┌┐·║│·║│·┌┐ ▌xxxxxx▐ ┌┐·║│·║│·┌┐·┌┐·║│·┌┐·▌"
		"▐·║│·└┘·║│·║│·└┘·└┘·║│ └▄▄▄▄▄▄┘ ║│·└┘·└┘·║│·║│·└┘·║│·▌"
		"▐·║│·  ·║│·║│·······║│          ║│·······║│·║│·  ·║│·▌"
		"▐·║│·┌┐·║│·║│·┌═══┐·║╘══┐ ┌┐ ┌══╬│·┌═══┐·║│·║│·┌┐·║│·▌"
		"▐·└┘·║│·└┘·└┘·└───┘·└───┘ ║│ └───┘·└───┘·└┘·└┘·║│·└┘·▌"
		"▐····║│·  ·  ·······      ║│      ·······  ·  ·║│····▌"
		"╙══┐·║│·┌┐·┌═══┐ ┌═══┐ ┌══╬╘══┐ ┌═══┐ ┌═══┐·┌┐·║│·┌══╜"
		"╟──┘·└┘·║│·└───┘ └───┘ └──────┘ └───┘ └───┘·║│·└┘·└──╢"
		"▐·······║│················  ················║│·······▌"
		"▐·┌═══┐·║│·┌┐·┌═══┐·┌┐·┌══════┐·┌┐·┌═══┐·┌┐·║│·┌═══┐·▌"
		"▐·║┼──┘·└┘·║│·└───┘·║│·└──╖┼──┘·║│·└───┘·║│·└┘·└──╖│·▌"
		"▐·║│······●║│·  ····║│····║│····║│····  ·║│●······║│·▌"
		"▐·║│·┌═══┐·║│·┌┐·┌══╬╘══┐·║│·┌══╬╘══┐·┌┐·║│·┌═══┐·║│·▌"
		"▐·└┘·└───┘·└┘·║│·└──────┘·└┘·└──────┘·║│·└┘·└───┘·└┘·▌"
		"▐·············║│······················║│·············▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"╔▄▄▄▄╕╒▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄╕╒▄▄▄▄╗"
		"▐····║│·······║│··········║│··········║│·······║│····▌"
		"▐·┌┐·║│·┌┐·┌┐·║│·┌┐·┌┐·┌┐·║│·┌┐·┌┐·┌┐·║│·┌┐·┌┐·║│·┌┐·▌"
		"▐·║│·└┘·║│·║│·└┘·║│·║│·║│·└┘·║│·║│·║│·└┘·║│·║│·└┘·║│·▌"
		"▐·║│····║│·║│····║│·║│·║│····║│·║│·║│····║│·║│····║│·▌"
		"▐·║╘════╬│·║╘════╬│·║│·║╘════╬│·║│·║╘════╬│·║╘════╬│·▌"
		"▐·└──╖┼──┘·└──╖┼──┘·└┘·└──╖┼──┘·└┘·└──╖┼──┘·└──╖┼──┘·▌"
		"▐····║│···●···║│·······   ║│   ·······║│···●···║│····▌"
		"▐·┌┐·║│·┌┐·┌┐·║│·┌┐·┌═══┐ ║│ ┌═══┐·┌┐·║│·┌┐·┌┐·║│·┌┐·▌"
		"▐·║│·└┘·║│·║│·└┘·║│·║┼──┘ └┘ └──╖│·║│·└┘·║│·║│·└┘·║│·▌"
		"▐·║│····║│·║│····║│·║│          ║│·║│····║│·║│····║│·▌"
		"▐·║╘════╬│·║╘════╬│·║│ ┌▀►--◄▀┐ ║│·║╘════╬│·║╘════╬│·▌"
		"▐·└──╖┼──┘·└──╖┼──┘·└┘ ▌xxXXxx▐ └┘·└──╖┼──┘·└──╖┼──┘·▌"
		"▐····║│·······║│····   ▌xXXXXx▐   ····║│·······║│····▌"
		"╙══┐·║│·┌═══┐·║│·┌═══┐ ▌xxxxxx▐ ┌═══┐·║│·┌═══┐·║│·┌══╜"
		"╟──┘·└┘·║┼──┘·└┘·└──╖│ └▄▄▄▄▄▄┘ ║┼──┘·└┘·└──╖│·└┘·└──╢"
		"▐·······║│····  ····║│          ║│····  ····║│·······▌"
		"▐·┌┐·┌══╬│·┌══════┐·║╘══┐ ┌┐ ┌══╬│·┌══════┐·║╘══┐·┌┐·▌"
		"▐·║│·└──╖│·└──────┘·║┼──┘ ║│ └──╖│·└──────┘·║┼──┘·║│·▌"
		"▐·║│····║│····  ····║│    ║│    ║│····  ····║│····║│·▌"
		"▐·║│·┌┐·║╘══┐·┌┐·┌══╬│ ┌══╬╘══┐ ║╘══┐·┌┐·┌══╬│·┌┐·║│·▌"
		"▐·└┘·║│·└───┘·║│·└───┘ ║┼────╖│ └───┘·║│·└───┘·║│·└┘·▌"
		"▐····║│●······║│·······║│    ║│·······║│······●║│····▌"
		"▐·┌══╬│·┌┐·┌══╬╘══┐·┌┐·║│ ┌┐ ║│·┌┐·┌══╬╘══┐·┌┐·║╘══┐·▌"
		"▐·└───┘·║│·║┼────╖│·║│·└┘ ║│ └┘·║│·║┼────╖│·║│·└───┘·▌"
		"▐·······║│·║│····║│·║│····║│····║│·║│····║│·║│·······▌"
		"▐·┌═══┐·║│·║│·┌┐·║│·║│·┌══╬╘══┐·║│·║│·┌┐·║│·║│·┌═══┐·▌"
		"▐·└───┘·║│·└┘·║│·└┘·║│·└──────┘·║│·└┘·║│·└┘·║│·└───┘·▌"
		"▐·······║│····║│····║│··········║│····║│····║│·······▌"
		"╚▀▀▀▀▀▀▀╛╧▀▀▀▀╛╧▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀╛╧▀▀▀▀╛╧▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"╔▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╗"
		"▐··········║│····║│····║│····║│····║│····║│··········▌"
		"▐·┌══════┐·║│·┌┐·║│·┌┐·║│·┌┐·║│·┌┐·║│·┌┐·║│·┌══════┐·▌"
		"▐·║┼────╖│·║│·║│·└┘·║│·└┘·║│·└┘·║│·└┘·║│·║│·║┼────╖│·▌"
		"▐·║│····║│·║│·║│·  ·║│····║│····║│·  ·║│·║│·║│····║│·▌"
		"▐·║│·┌┐·║│·║│·║│·┌┐·║╘══┐·║│·┌══╬│·┌┐·║│·║│·║│·┌┐·║│·▌"
		"▐·║│·└┘·└┘·║│·└┘·║│·└───┘·║│·└───┘·║│·└┘·║│·└┘·└┘·║│·▌"
		"▐·║│···●···║│····║│·······║│·······║│····║│···●···║│·▌"
		"▐·║╘══┐·┌┐·║│·┌══╬│·┌═══┐ ║│ ┌═══┐·║╘══┐·║│·┌┐·┌══╬│·▌"
		"▐·└───┘·└┘·└┘·└──╖│·║┼──┘ └┘ └──╖│·║┼──┘·└┘·└┘·└───┘·▌"
		"▐················║│·║│          ║│·║│················▌"
		"╙════════┐·┌┐·┌┐·║│·║│ ┌▀►--◄▀┐ ║│·║│·┌┐·┌┐·┌════════╜"
		"╟───────╖│·║│·└┘·└┘·└┘ ▌xxXXxx▐ └┘·└┘·└┘·║│·║┼───────╢"
		"▐·······║│·║│·······   ▌xXXXXx▐   ·······║│·║│·······▌"
		"▐·┌═══┐·║│·║│·┌══════┐ ▌xxxxxx▐ ┌══════┐·║│·║│·┌═══┐·▌"
		"▐·└───┘·└┘·║│·└──────┘ └▄▄▄▄▄▄┘ └──────┘·║│·└┘·└───┘·▌"
		"▐··········║│·······              ·······║│··········▌"
		"╙════════┐·║│·┌┐·┌┐·┌═══┐ ┌┐ ┌═══┐·┌┐·┌┐·║│·┌════════╜"
		"╟────────┘·└┘·└┘·║│·║┼──┘ ║│ └──╖│·║│·└┘·└┘·└────────╢"
		"▐················║│·║│    ║│    ║│·║│················▌"
		"▐·┌═══┐·┌┐·┌┐·┌══╬│·║│ ┌══╬╘══┐ ║│·║╘══┐·┌┐·┌┐·┌═══┐·▌"
		"▐·║┼──┘·└┘·║│·└──╖│·║│ └──────┘ ║│·║┼──┘·║│·└┘·└──╖│·▌"
		"▐·║│···●···║│····║│·║│····  ····║│·║│····║│···●···║│·▌"
		"▐·║│·┌┐·┌┐·║│·┌┐·║│·║╘══┐·┌┐·┌══╬│·║│·┌┐·║│·┌┐·┌┐·║│·▌"
		"▐·║│·└┘·║│·║│·║│·└┘·└───┘·║│·└───┘·└┘·║│·║│·║│·└┘·║│·▌"
		"▐·║│····║│·║│·║│·  ·······║│·······  ·║│·║│·║│····║│·▌"
		"▐·║╘════╬│·║│·║│·┌┐·┌═════╬╘═════┐·┌┐·║│·║│·║╘════╬│·▌"
		"▐·└──────┘·║│·└┘·║│·└────────────┘·║│·└┘·║│·└──────┘·▌"
		"▐··········║│····║│················║│····║│··········▌"
		"╚▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]

[		"                                                      "
		"                                                      "
		"                                                      "
		"                                                      "
		"╔▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╗"
		"▐··········║│··········║│····║│··········║│··········▌"
		"▐·┌┐·┌┐·┌┐·║│·┌┐·┌┐·┌┐·║│·┌┐·║│·┌┐·┌┐·┌┐·║│·┌┐·┌┐·┌┐·▌"
		"▐·║│·║│·└┘·└┘·└┘·║│·║│·└┘·║│·└┘·║│·║│·└┘·└┘·└┘·║│·║│·▌"
		"▐·║│·║│··········║│·║│····║│····║│·║│··········║│·║│·▌"
		"▐·║│·║╘══┐·┌┐·┌══╬│·║╘══┐·║│·┌══╬│·║╘══┐·┌┐·┌══╬│·║│·▌"
		"▐·└┘·└───┘·║│·└───┘·└───┘·║│·└───┘·└───┘·║│·└───┘·└┘·▌"
		"▐···●······║│·············║│·············║│······●···▌"
		"▐·┌┐·┌═══┐·║│·┌═══┐·┌═══┐ ║│ ┌═══┐·┌═══┐·║│·┌═══┐·┌┐·▌"
		"▐·║│·║┼──┘·└┘·└──╖│·║┼──┘ └┘ └──╖│·║┼──┘·└┘·└──╖│·║│·▌"
		"▐·║│·║│··········║│·║│          ║│·║│··········║│·║│·▌"
		"▐·║│·║│·┌┐·┌┐·┌┐·║│·║│ ┌▀►--◄▀┐ ║│·║│·┌┐·┌┐·┌┐·║│·║│·▌"
		"▐·└┘·└┘·└┘·║│·└┘·└┘·└┘ ▌xxXXxx▐ └┘·└┘·└┘·║│·└┘·└┘·└┘·▌"
		"▐··········║│·······   ▌xXXXXx▐   ·······║│··········▌"
		"▐·┌═══┐·┌══╬╘══┐·┌═══┐ ▌xxxxxx▐ ┌═══┐·┌══╬╘══┐·┌═══┐·▌"
		"▐·└───┘·└──╖┼──┘·└───┘ └▄▄▄▄▄▄┘ └───┘·└──╖┼──┘·└───┘·▌"
		"▐··········║│·······              ·······║│··········▌"
		"▐·┌┐·┌┐·┌┐·║│·┌┐·┌┐·┌┐ ┌══════┐ ┌┐·┌┐·┌┐·║│·┌┐·┌┐·┌┐·▌"
		"▐·║│·║│·└┘·└┘·└┘·║│·║│ └──╖┼──┘ ║│·║│·└┘·└┘·└┘·║│·║│·▌"
		"▐·║│·║│··········║│·║│    ║│    ║│·║│··········║│·║│·▌"
		"▐·║│·║╘══┐·┌┐·┌══╬│·║╘══┐ ║│ ┌══╬│·║╘══┐·┌┐·┌══╬│·║│·▌"
		"▐·└┘·└───┘·║│·└───┘·└───┘ └┘ └───┘·└───┘·║│·└───┘·└┘·▌"
		"▐···●······║│·············  ·············║│······●···▌"
		"▐·┌┐·┌═══┐·║│·┌═══┐·┌═══┐·┌┐·┌═══┐·┌═══┐·║│·┌═══┐·┌┐·▌"
		"▐·║│·║┼──┘·└┘·└──╖│·║┼──┘·║│·└──╖│·║┼──┘·└┘·└──╖│·║│·▌"
		"▐·║│·║│··········║│·║│····║│····║│·║│··········║│·║│·▌"
		"▐·║│·║│·┌┐·┌┐·┌┐·║│·║│·┌══╬╘══┐·║│·║│·┌┐·┌┐·┌┐·║│·║│·▌"
		"▐·└┘·└┘·└┘·║│·└┘·└┘·└┘·└──────┘·└┘·└┘·└┘·║│·└┘·└┘·└┘·▌"
		"▐··········║│····························║│··········▌"
		"╚▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╛╧▀▀▀▀▀▀▀▀▀▀╝"
		"                                                      "
		"                                                      "]])

 ;; Images for fruits and artwork
(def image-files {:pac-art "pacman.png"
									:monster-art "monster.png"
									:cherry "cherryhd.png"
									:strawberry "strawberryhd.png"
									:orange "orangehd.png"
									:apple "applehd.png"
									:greenapple "greenapplehd.png"
									:lime "limehd.png"
									:lemon "lemonhd.png"
									:kiwi "kiwihd.png"
									:donut "donuthd.png"
									:coffee "coffeehd.png"
									:galaxian "galaxianhd.png"
									:bell "bellhd.png"
									:fruitkey "keyhd.png"
									:pretzel "pretzelhd.png"
									:pear "pearhd.png"
									:banana "bananahd.png"
									:trike "trikehd.png"
									:kite "kitehd.png"
									:drum "drumhd.png"
									:balloon "balloonhd.png"
									:train "trainhd.png"
									:kitty "kittyhd.png"
									:beer "beerhd.png"})

(defn get-current-directory []
  (str (. (java.io.File. ".") getCanonicalPath) "/"))

(defn load-image [f]
	(javax.imageio.ImageIO/read (clojure.java.io/file (str (get-current-directory) f))))

(def images (into {} (for [[name filename] image-files] [name (load-image (str "./graphics/" filename))])))

;; Custom colors
(def game-colors {:mazeblue (new Color 33 32 222)
									:ghostblue (new Color 33 32 222)
									:peach (new Color 255 186 148)
									:pinkypink (new Color 255 184 222)
									:mspacblue (new Color 66 186 222)
									:mspacbrown (new Color 222 150 66)
									:jrpacbrown (new Color 184 104 0)
									:jrpacblue (new Color 71 184 222)
									:jrpacgreen (new Color 0 104 0)
									:graydots (new Color 222 223 222)
									:clydeorange (new Color 255 184 0)
									:yellorange (new Color 255 184 71)
									:purple (new Color 110 0 222)
									:darkgray (new Color 31 31 31)
									:brown (new Color 123 65 27)
									:kelly (new Color 38 203 13)
									:woody (new Color 162 68 4)
									:glow (new Color 99 255 204)
									:yellow Color/YELLOW
									:red Color/RED
									:cyan (new Color 0 255 222)
									:black Color/BLACK
									:white Color/WHITE
									:green Color/GREEN
									:blue Color/BLUE})

(def levels [;; Pac-Man
             {:level  1, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :cherry,     :fruitpoints  100, :pacspeed 0.80, :pacdotspeed 0.71, :ghostspeed 0.75, :ghosttunnelspeed 0.40, :elroy1dots  20 :elroy1speed 0.80 :elroy2dots 10 :elroy2speed 0.85 :frightpacspeed 0.90 :frightpacdotspeed 0.79 :frightghostspeed 0.50 :frighttime 6 :intermission nil :intervals [7 20 7 20 5 20 5]}
             ;{:level  2, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :strawberry, :fruitpoints  300, :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  30 :elroy1speed 0.90 :elroy2dots 15 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 5 :intermission nil :intervals [7 20 7 20 5 1033 1/60]}
             ;{:level  3, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :orange,     :fruitpoints  500, :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 4 :intermission 1   :intervals [7 20 7 20 5 1033 1/60]}
             ;{:level  4, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :orange,     :fruitpoints  500, :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 3 :intermission nil :intervals [7 20 7 20 5 1033 1/60]}
             ;{:level  5, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :apple,      :fruitpoints  700, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}

             ;; Ms. Pac-Man
             ;;{:level  2,  :style :pacman :maze  1, :mazecolor :red,           :solidcolor :peach,    :dotcolor :graydots,       :fruit :cherry,      :fruitpoints  100, :pacspeed 0.90, :pacdotspeed 0.83, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  30 :elroy1speed 0.90 :elroy2dots 15 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 5 :intermission nil :intervals [7 20 7 20 5 20 5]}
             {:level  2,  :style :mspacman :maze  1, :mazecolor :red,           :solidcolor :peach,    :dotcolor :graydots,       :fruit :strawberry,  :fruitpoints  200, :pacspeed 0.90, :pacdotspeed 0.83, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  30 :elroy1speed 0.90 :elroy2dots 15 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 5 :intermission nil :intervals [7 20 7 20 5 20 5]}
             {:level  3,  :style :mspacman :maze  2, :mazecolor :white,         :solidcolor :mspacblue,    :dotcolor :yellow,   :fruit :orange,      :fruitpoints  500, :pacspeed 0.92, :pacdotspeed 0.85, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 4 :intermission 1   :intervals [7 20 7 20 5 1033 1/60]}
             ;;{:level  3,  :style :mspacman :maze  2, :mazecolor :white,         :solidcolor :mspacblue,    :dotcolor :yellow,   :fruit :pretzel,     :fruitpoints  700, :pacspeed 0.90, :pacdotspeed 0.83, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 4 :intermission nil :intervals [7 20 7 20 5 1033 1/60]}
             ;;{:level  3,  :style :mspacman :maze  2, :mazecolor :white,         :solidcolor :mspacblue,    :dotcolor :yellow,   :fruit :greenapple,  :fruitpoints  1000, :pacspeed 0.90, :pacdotspeed 0.83, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 3 :intermission nil :intervals [7 20 7 20 5 1033 1/60]}
             {:level  4,  :style :mspacman :maze  3, :mazecolor :white,         :solidcolor :mspacbrown,   :dotcolor :red,      :fruit :pear,        :fruitpoints  2000, :pacspeed 0.95, :pacdotspeed 0.88, :ghostspeed 0.90, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.89 :frightghostspeed 0.55 :frighttime 3 :intermission 2   :intervals [5 20 5 20 5 1037 1/60]}
             ;;{:level  4,  :style :mspacman :maze  3, :mazecolor :white,         :solidcolor :mspacbrown,   :dotcolor :red,      :fruit :banana,      :fruitpoints  5000, :pacspeed 0.95, :pacdotspeed 0.88, :ghostspeed 0.90, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.89 :frightghostspeed 0.55 :frighttime 3 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level  5,  :style :mspacman :maze  4, :mazecolor :yellorange,   :solidcolor :mazeblue,  :dotcolor :graydots,       :fruit :banana,      :fruitpoints  5000, :pacspeed 1.00, :pacdotspeed 0.91, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.96 :frightghostspeed 0.60 :frighttime 2 :intermission 3   :intervals [5 20 5 20 5 1037 1/60]}

             ;; Jr. Pac-Man
             {:level  6, :style :jrpacman :maze  5, :mazecolor :clydeorange,   :solidcolor :mazeblue,  :dotcolor :peach,       :fruit :trike,      :fruitpoints  100,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [7 20 7 20 5 20 5]}
	         {:level  7, :style :jrpacman :maze  6, :mazecolor :jrpacblue,    :solidcolor :jrpacbrown,  :dotcolor :yellow,       :fruit :kite,      :fruitpoints  200,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [7 20 7 20 5 1033 1/60]}
             {:level  8, :style :jrpacman :maze  7, :mazecolor :clydeorange,    :solidcolor :jrpacblue,  :dotcolor :yellow,       :fruit :drum,      :fruitpoints  500,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [7 20 7 20 5 1033 1/60]}
             {:level  9, :style :jrpacman :maze  8, :mazecolor :yellow,        :solidcolor :jrpacgreen,  :dotcolor :white,       :fruit :balloon,      :fruitpoints  700,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [7 20 7 20 5 1033 1/60]}
             {:level 10, :style :jrpacman :maze  9, :mazecolor :cyan,        :solidcolor :mazeblue,  :dotcolor :white,       :fruit :train,      :fruitpoints  1000,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 11, :style :jrpacman :maze 10, :mazecolor :jrpacblue,    :solidcolor :jrpacbrown,  :dotcolor :yellow,       :fruit :kitty,      :fruitpoints  2000,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 12, :style :jrpacman :maze 11, :mazecolor :clydeorange,    :solidcolor :mazeblue,  :dotcolor :peach,       :fruit :beer,      :fruitpoints  5000,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}

             ;; Custom
             {:level 13, :style :jrpacman :maze 12, :mazecolor :purple,    :solidcolor :darkgray,  :dotcolor :glow,       :fruit :coffee,      :fruitpoints  5000,  :pacspeed 1.80, :pacdotspeed 1.58, :ghostspeed 1.70, :ghosttunnelspeed 0.90, :elroy1dots  30 :elroy1speed 1.80 :elroy2dots 20 :elroy2speed 1.90 :frightpacspeed 1.90 :frightpacdotspeed 1.66 :frightghostspeed 1.10 :frighttime 7 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}

             ;; More classic
             {:level  6, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :apple,      :fruitpoints  700, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  50 :elroy1speed 1.00 :elroy2dots 25 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission 2   :intervals [5 20 5 20 5 1037 1/60]}
             {:level  7, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :lime,       :fruitpoints 1000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  50 :elroy1speed 1.00 :elroy2dots 25 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level  8, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :lime,       :fruitpoints 1000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  50 :elroy1speed 1.00 :elroy2dots 25 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level  9, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :galaxian,   :fruitpoints 2000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  60 :elroy1speed 1.00 :elroy2dots 30 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 10, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :galaxian,   :fruitpoints 2000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  60 :elroy1speed 1.00 :elroy2dots 30 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission 3   :intervals [5 20 5 20 5 1037 1/60]}
             {:level 11, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :bell,       :fruitpoints 3000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  60 :elroy1speed 1.00 :elroy2dots 30 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 12, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :bell,       :fruitpoints 3000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  80 :elroy1speed 1.00 :elroy2dots 40 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 13, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  80 :elroy1speed 1.00 :elroy2dots 40 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 14, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  80 :elroy1speed 1.00 :elroy2dots 40 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 3 :intermission 3   :intervals [5 20 5 20 5 1037 1/60]}
             {:level 15, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 16, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 17, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 18, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission 3   :intervals [5 20 5 20 5 1037 1/60]}
             {:level 19, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 120 :elroy1speed 1.00 :elroy2dots 60 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 20, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 120 :elroy1speed 1.00 :elroy2dots 60 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             {:level 21, :style :pacman :maze  0, :mazecolor :mazeblue,      :solidcolor :black,  :dotcolor :peach,         :fruit :fruitkey,   :fruitpoints 5000, :pacspeed 0.95, :pacdotspeed 0.79, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 120 :elroy1speed 1.00 :elroy2dots 60 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil :intervals [5 20 5 20 5 1037 1/60]}
             ])

;; Basic math functions
(defn abs [i] (if (< i 0.0) (- 0.0 i) i))
(defn round [i] (int (+ i 1/2)))
(defn bround [bi] (bigint (+ bi 1/2)))

;; Sound effects
(def sound-files {:pacman { :intro "intro.wav"
														:intermission "intermission.wav"
														:dot1 "dot1.wav"
														:dot2 "dot2.wav"
														:fruit "fruit.wav"
														:ghost "ghost.wav"
														:bonus "bonus.wav"
														:death "death.wav"
														:siren "siren.wav"
														:eyes "eyes.wav"
														:energizer "energizer.wav"
														:credit "credit.wav"}
								:mspacman { :intro "msintro.wav"
														:intermission "intermission.wav"
														:dot1 "msdot1.wav"
														:dot2 "msdot1.wav"
														:fruit "fruit.wav"
														:ghost "msghost.wav"
														:bonus "bonus.wav"
														:death "msdeath.wav"
														:siren "mssiren.wav"
														:eyes "mseyes.wav"
														:energizer "msenergizer.wav"
														:credit "credit.wav"}
								:jrpacman { :intro "jrintro.wav"
														:intermission "intermission.wav"
														:dot1 "jrdot1.wav"
														:dot2 "jrdot1.wav"
														:fruit "fruit.wav"
														:ghost "jrghost.wav"
														:bonus "bonus.wav"
														:death "jrdeath.wav"
														:siren "jrsiren.wav"
														:eyes "jreyes.wav"
														:energizer "jrenergizer.wav"
														:credit "credit.wav"}})

(defn wav-file
  "Returns the pathname of a specific wav file for a given level style and sound effect"
  [ls s]
  (str "./sounds/" (s (ls sound-files))))

(defn play-file
  "Plays an audio file"
   [file-name]
  (let [the-file (File. file-name)
        audio-format (.getFormat (AudioSystem/getAudioFileFormat the-file))
        buffer-stream (BufferedInputStream. (FileInputStream. the-file))
        audio-stream (AudioInputStream. buffer-stream audio-format (.length the-file))
        clip (AudioSystem/getLine (DataLine$Info. Clip audio-format))]
    (doto clip (.open audio-stream) (.start))))

(defn play-sound
  "Plays a specific sound effect in a background thread"
  [g the-sound]
  (when (@g :sound)
    (future (play-file (wav-file (@g :style) the-sound)))))

(def ^:const invalid-target 999999)

(defn dots
  "Constructs a list of row,column vectors for the dots on the maze of the given level.
	The game keeps a copy of the list that is updated as dots are eaten."
  [level]
  (for [[rowindex row] (map vector (iterate inc 0) (nth mazes (:maze (nth levels (dec level)))))
        [colindex ch] (map vector (iterate inc 0) row)
        :when (let [c (str ch)] (or (= c "·") (= c "*") (= c "●")))]
    [rowindex colindex]))

(defn maze
  "Returns the maze for a given level"
  [level]
  (nth mazes (:maze (nth levels (dec level)))))

(defn maze-rows [m] (count m))
(defn maze-columns [m] (count (first m)))

;; Time functions
(defn now [] (. (new Date) getTime))

(defn every
  "Returns true every given milliseconds"
  [g milliseconds]
  (= (mod (bround (/ (- (@g :clock) (now)) milliseconds)) 2) 0))

(defn elapsed
  "Returns the elapsed seconds on the given clock"
  [g c]
  (/ (- (now) (@g c)) 1000))

(defn mode-interval
  "Defines the scatter and chase intervals based on level"
  [g a s]
  (let [level (nth levels (dec (@g :level)))
        intervals (:intervals level)
        es (- s 3)
        wander (- (@g :midc) 4)]
	  (cond (and (<= (elapsed g :blueclock) (:frighttime level)) (= (@a :mode) :frightened)) :frightened
	        (= (@a :mode) :reincarnate) :reincarnate
	        (and (not= (@g :style) :pacman) (or (= (@a :nickname) :speedy) (= (@a :nickname) :shadow)) (< es wander)) :random ; speedy & shadow wander at start of level
          (and (not= (@g :style) :pacman)
               (or (< es wander)
			             (and (>= es 30) (< es 30.5))
			             (and (>= es 57) (< es 57.5))
			             (and (>= es 84) (< es 84.5)))) :scatter ; Ms. and Jr. Pac-Man ghosts switch direction but don't really scatter
          (and (= (@g :style) :pacman) (= (@g :level) 1)
               (or (< es 10)
			             (and (>= es 30) (< es 37))
			             (and (>= es 57) (< es 64))
			             (and (>= es 84) (< es 89)))) :scatter
	        (and (= (@g :style) :pacman) (>= (@g :level) 2)
               (<= (@g :level) 4)
               (or (< es 10)
			             (and (>= es 30) (< es 37))
			             (and (>= es 57) (< es 64))
			             (and (>= es 1097) (< es 1098)))) :scatter
	        (and (= (@g :style) :pacman) (>= (@g :level) 5)
               (or (< es 7.5)
			             (and (>= es 28) (< es 33))
			             (and (>= es 53) (< es 58))
			             (and (>= es 1095) (< es 1096)))) :scatter
	        :default :chase)))

(defn xoffset [d mult]
  (cond
    (= d :left) (- mult)
    (= d :right) mult
    :default 0))

(defn yoffset [d mult]
  (cond
    (= d :up) (- mult)
    (= d :down) mult
    :default 0))

(defn actor-xoffset [actor mult]
  (xoffset (@actor :d) mult))

(defn actor-yoffset [actor mult]
  (yoffset (@actor :d) mult))

(defn coltox
  "Convert column index to screen coordinate x.
  Returns center coordinate of tile."
  [g colindex]
  (let [size (@g :tilesize)
        offset (- (@g :midx) (* size (round (/ (@g :mazecolumns) 2))))]
    (+ (* colindex size) (@g :halftile) offset)))

(defn rowtoy
  "Convert row index to screen coordinate y.
  Returns center coordinate of tile."
  [g rowindex]
  (let [size (@g :tilesize)]
    (+ (* rowindex size) (@g :halftile))))

(defn xtocol
  "Convert screen coordinate x to column index"
  [g x]
  (let [x (double x)
        size (@g :tilesize)
        offset (- (@g :midx) (* size (round (/ (@g :mazecolumns) 2))))]
    (int (/ (- x offset) size))))

(defn ytorow
  "Convert screen coordinate y to row index"
  [g y]
  (let [y (double y)
        size (@g :tilesize)]
    (int (/ y size))))

(defn opposite-direction
  "Returns the opposite direction"
  [d]
  (cond
    (= d :left) :right
    (= d :right) :left
    (= d :up) :down
    (= d :down) :up))

(defn opposite-direction?
  "Returns whether the given directions are opposite"
  [d1 d2]
    (= d1 (opposite-direction d2)))

(defn path-tile?
  "Returns whether the given tile value is a navigable part of the maze"
  [t]
  (some #(= t %) ["·" " " "X" "#" "●" "^" "*" "<" "-"]))

(defn maze-tile
  "Returns the contents of the maze tile at row, col"
  [m r c]
  (if (and (>= r 0) (>= c 0) (< r (count m)) (< c (count (first m)))) ; bounds check
    (str (nth (nth m r) c))
    "<")) ; tunnel if off-grid

(defn in-gate?
  "Returns whether actor is passing through pen gate"
  [g a]
  (let [r (ytorow g (@a :y))
        c (xtocol g (@a :x))
        t (maze-tile (@g :maze) r c)]
	  (= t "-")))

(defn in-pen?
  "Returns whether actor is in pen"
  [g a]
  (let [r (ytorow g (@a :y))
        c (xtocol g (@a :x))
        t (maze-tile (@g :maze) r c)]
	  (or (= t "X") (= t "x"))))

(defn entering-pen? [g d r c]
  "Determines whether actor is attempting to enter pen"
  (let [t (maze-tile (@g :maze) (inc r) c)]
	  (and (= d :down) (= t "-"))))

(defn leaving-pen?
  "Determines whether ghost is leaving pen"
  [g a gs]
  (and (not= (@a :mode) :reincarnate) (in-pen? g a) (> (@a :x) (- (@g :midx) gs)) (< (@a :x) (+ (@g :midx) gs))))

(defn forbidden?
  "Returns whether ghost is forbidden to turn this way"
  [m d r c]
  (let [t (maze-tile m r c)]
    (and (= d :up) (or (= t "^") (= t "*")))))

(defn set-game-score [g s p]
  (if (= p 1)
	  (swap! g assoc :score1 s)
	  (swap! g assoc :score2 s))
  (when (> s (@g :highscore))
    (swap! g assoc :highscore s)))

(defn remove-dot!
  "Removes dot at r,c from list d"
  [d r c]
  (replace {[r c] []} d))

(defn eat-dot!
  "Removes dot from board"
  [g a r c t]
	(set-game-score g (+ (if (not= t "●")
                          10 ; 10 points per dot
                          50) ; 50 points per energizer
                  (@g (if (= (@a :nickname) :pacman) :score1 :score2))) (if (= (@a :nickname) :pacman) 1 2)) ; player one or two
  (swap! a assoc :waka (not (@a :waka)) :dotr r :dotc c) ; toggle "waka-waka" munching sound
  (play-sound g (if (@a :waka) :dot1 :dot2))
  ;; Replace gameboard dots
  (println (str "Pac-Man ate " (if (not= t "●") "a dot" "an energizer") " at " r "," c))
  (swap! g update-in [:dotsleft] dec)
	(swap! g update-in [:dots] remove-dot! r c))

(defn chomping-ghosts? [g]
	(some #(= (@g :paused) %) [:eat-blinky :eat-pinky :eat-inky :eat-clyde]))

(defn elroy-level
  "Returns the Cruise Elroy speed for Blinky, based on board level and dots eaten.
  0 - Not active, 1 - Faster, 2 - Fastest"
  [g]
  (let [d (@g :dotsleft)
        level (nth levels (dec (@g :level)))]
    (cond (< d (:elroy1dots level)) 1
          (< d (:elroy2dots level)) 2
          :default 0)))

(defn translate-direction
  "Translates an actor's relative direction to a physical maze direction"
  [a d]
  (let [ad (@a :d)] ; actor direction
		(cond (= d :forward) ad ; translated direction
          (= d :backward) (opposite-direction ad)
					(= d :left) (cond (= ad :up) :left
					                  (= ad :down) :right
					                  (= ad :left) :down
					                  (= ad :right) :up)
					(= d :right) (cond (= ad :up) :right
					                   (= ad :down) :left
					                   (= ad :left) :up
					                   (= ad :right) :down))))

(defn next-tile
  "Returns the contents of the next adjacent tile in the actor's given direction"
  [g a d]
  (let [c (xtocol g (@a :x))
        r (ytorow g (@a :y))
        nc (+ c (xoffset d 1))
        nr (+ r (yoffset d 1))]
	  (maze-tile (@g :maze) nr nc)))

(defn center-of-tile-x
  "Returns the center x pixel of the tile at x"
  [g x]
  (double (coltox g (xtocol g x))))

(defn center-of-tile-y
  "Returns the center y pixel of the tile at y"
  [g y]
  (double (rowtoy g (ytorow g y))))

(defn center-of-tile?
  "Returns whether given actor is currently at the center of its tile"
  [g a]
  (let [d (@a :d)
        x (@a :x)
        y (@a :y)]
		(or (and (or (= d :up)   (= d :down))  (= y (center-of-tile-y g y)))
		    (and (or (= d :left) (= d :right)) (= x (center-of-tile-x g x))))))

(defn center-tolerance?
  "Returns whether coordinate within tolerance of center for given directions"
  [d1 d2 d v cv n]
	(and (or (= d d1) (= d d2))
			 (and (< v (+ cv n))
            (> v (- cv n)))))

(defn near-center-of-tile?
  "Returns whether given actor is currently within n pixels of the center of its tile"
  [g a n]
  (let [d (@a :d)
        x (@a :x)
        y (@a :y)
        cx (center-of-tile-x g x)
        cy (center-of-tile-y g y)]
		(or (center-tolerance? :up :down d y cy n)
	      (center-tolerance? :left :right d x cx n))))

(defn ghost-valid-tile?
  "Returns whether the tile is valid for the ghost to travel on"
  [g a d r c n]
  (and (path-tile? n) ; Only defined tiles
       (or (= (@a :mode) :frightened) (= (@a :mode) :reincarnate) (not (forbidden? (@g :maze) d r c))) ; Not forbidden path for regular ghosts
       (or (= (@a :mode) :reincarnate) (not (entering-pen? g d r c))))) ; Do not let ghosts re-enter pen

(defn ghost-my-openpath?
  "Returns whether path open for ghost's given relative direction"
  [g a d]
  (let [r (ytorow g (@a :y))
        c (xtocol g (@a :x))
        td (translate-direction a d)
        nr (+ r (yoffset (@a :d) 1)) ; Coordinates of tile in current traveling direction
        nc (+ c (xoffset (@a :d) 1))
        nrt (+ nr (yoffset td 1)) ; Coordinates of tile in given direction
        nct (+ nc (xoffset td 1))
        n (maze-tile (@g :maze) nrt nct)] ; The contents of the tile
    (ghost-valid-tile? g a td nr nc n)))

(defn ghost-openpath?
  "Returns whether path open for actor's given direction"
  [g a d]
  (let [n (next-tile g a d)
        r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
    (ghost-valid-tile? g a d r c n)))

(defn pacman-openpath?
  "Returns whether path open for actor's given direction"
  [g a d]
  (let [n (next-tile g a d)
        r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
    (and (or (or (= d :left) (= d :right)) (and (> c 0) (< c (@g :mazecolumns)))) ; cannot turn up and down when deep in tunnel
         (path-tile? n) ; Only defined tiles
         (not (entering-pen? g d r c))))) ; Do not let Pac-Man enter pen

(defn set-actor-position [a x y]
	(swap! a assoc :x x :y y))
(defn set-actor-direction [a d]
  (println (str "Turning " (name (@a :nickname)) " " (name d)))
  (swap! a assoc :d d))

(defn center-pacman!
  "Centers actor within tile"
  [g a]
  (when (= (@g :paused) :none)
    (set-actor-position a (coltox g (xtocol g (@a :x))) (rowtoy g (ytorow g (@a :y))))))

(defn park-pacman!
  "Parks Pac-Man and centers within tile."
  [g a]
  (when (not (@a :parked))
	  (center-pacman! g a)
	  (println "Pac-Man is parked")
	  (swap! a assoc :parked true)))

(defn straighten?
  "Centers actor within path"
  [g a]
	(let [x (@a :x)
			  y (@a :y)
        d (@a :d)
        r (ytorow g y)
        c (xtocol g x)
        mid (@g :midc)]
	  (when (some #(= (@g :paused) %) [:none :eat-blinky :eat-pinky :eat-inky :eat-clyde])
		  (if (or (= d :left) (= d :right))
				(set-actor-position a x (rowtoy g r))
	      (set-actor-position a (if (and (not= (@a :mode) :reincarnate) (>= r 15) (<= r 17) (or (= c mid) (= c (dec mid))))
																(@g :midx) ; special case for leaving pen
								                (coltox g c)) y)))))

(defn set-ghost-direction?
  "Tries to turn ghost in given direction if path open"
  [g a d]
  (when (ghost-openpath? g a d)
    (set-actor-direction a d)
    (straighten? g a)))

(defn set-pacman-direction?
  "Tries to turn Pac-Man in given direction if path open and near center of tile"
  [g a d]
  (let [r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
	  (when (and (pacman-openpath? g a d) ; Path must be open to turn that way
				       (or (opposite-direction? d (@a :d)) (near-center-of-tile? g a (* (@a :s) 3)))) ; Must be near center of tile to turn, unless reversing
	    (set-actor-direction a d)
      (straighten? g a))))

(defn new-ghost-tile?
  "Determines whether the ghost is in a new tile now"
  [g a]
  (or (not= (@a :lastrow) (ytorow g (@a :y))) (not= (@a :lastcol) (xtocol g (@a :x)))))

(defn reverse-ghost-direction? [g a]
	(let [r (ytorow g (@a :y))
        c (xtocol g (@a :x))
        od (@a :d)
        nd (opposite-direction (@a :d))
        mid (@g :midc)]
	(when (and (not (in-pen? g a)) ; don't reverse in pen
             (not (in-gate? g a))) ; don't reverse if just leaving pen
    (when (not (@a :reverse))
      (swap! a assoc :reverse true :lastcol c :lastrow r)) ; set reverse flag, remember current tile
    (when (or (near-center-of-tile? g a (* (@a :s) 2)) (and (@a :reverse) (new-ghost-tile? g a))) ; don't actually reverse until next tile
      #_(swap! a assoc :reverse false :d nd :nd nd :lastrow 0 :lastcol 0)
			(cond (ghost-openpath? g a nd) (swap! a assoc :reverse false :d nd :nd nd :lastrow 0 :lastcol 0) ; force recalc at next tile center
		        (ghost-my-openpath? g a :left) (swap! a assoc :reverse false :d (translate-direction a :left) :nd (translate-direction a :left) :lastrow 0 :lastcol 0)
		        (ghost-my-openpath? g a :right) (swap! a assoc :reverse false :d (translate-direction a :right) :nd (translate-direction a :right) :lastrow 0 :lastcol 0)
            :default (swap! a assoc :reverse false)) ; clear flag if no reverse possible
	    (println (str (name (@a :nickname)) " reversed direction from " (name od) " to " (name (@a :nd))))))))

(defn set-ghost-mode
  "Valid modes are :scatter, :chase, :frightened, :random, or :reincarnate"
  [g a mode]
  (let [current-mode (@a :mode)]
    (when (not= mode current-mode)
		  (println (str (name (@a :nickname)) " changed to " (name mode) " mode"))
		  (when (or (and (= (@g :style) :pacman) (= current-mode :scatter) (= mode :chase))
		            (and (= current-mode :chase) (= mode :scatter))
                (and (= current-mode :random) (= mode :chase)))
		    (reverse-ghost-direction? g a)) ; reverse when switching between scatter/chase
		  (swap! a assoc :mode mode))))

(defn frighten-ghost!
  "Reverses ghost and puts into frightened mode"
  [g a]
  (when (not= (@a :mode) :reincarnate) ; leave reincarnating ghosts alone
	  (set-ghost-mode g a :frightened)
	  (reverse-ghost-direction? g a)))

(defn eat-dots!
  "Checks to see if a dot can be eaten"
  [g pacman blinky pinky inky clyde]
  (let [x (@pacman :x)
	      r (ytorow g (@pacman :y))
	      c (xtocol g x)
        dl (@g :dotsleft)
        level (nth levels (dec (@g :level)))
	      center (@g :midx)
	      half (@g :halftile)]
	   (when (and (not= (@g :paused) :death) (some {[r c] []} (@g :dots))) ; uneaten dot here?
	      (let [t (maze-tile (@g :maze) r c)]
					(eat-dot! g pacman r c t)
				  
	        ;; Keep track of dots eaten for pen exit
				  (swap! g assoc :dotclock (now))
				  (when (< (@g :globaldots) 0)
	          (doseq [a [pinky inky clyde]]
							(swap! a update-in [:dotcount] dec)))
	        (swap! g update-in [:globaldots] inc)
					
	        (when (= t "●") ; turn ghosts blue and reverse when energizer is eaten
	          (swap! g assoc :ghostpoints 100 :blueclock (now) :sirenclock (- (now) 2000))
	          (doseq [a [blinky pinky inky clyde]]
	            (frighten-ghost! g a)))
	
	        ;; check level complete
	        (when (and (not= (@g :paused) :level)
	                   (= (@g :dotsleft) 0))
		        (println "Level complete!")
	          (swap! g assoc :paused :level :pauseclock (now)))))
	
		 ;; eat fruit
	   (when (and (> (elapsed g :bonusclock) 11) (< (elapsed g :fruitclock) 11) (<= dl 174) (= (@g :fruit) :uneaten) (= r 20) (>= x (- center half)) (<= x (+ center half)))
	     (set-game-score g (+ (:fruitpoints level) (@g :score1)) (if (= (@pacman :nickname) :pacman) 1 2))
			 (play-sound g :fruit)
			 (swap! g assoc :fruit :eaten :bonusclock (now)))))

(defn tile-distance
  "Returns the distance in tiles to a tile at r,c from the tile in the ghost's given relative direction"
  [g a d r c]
  (let [td (translate-direction a d)
        nc (+ (xtocol g (@a :x)) (xoffset (@a :d) 1)) ; next tile forward
        nr (+ (ytorow g (@a :y)) (yoffset (@a :d) 1))
        ac (+ nc (xoffset td 1)) ; tile in given direction
        ar (+ nr (yoffset td 1))]
    (if (ghost-my-openpath? g a d)
      (Math/sqrt (+ (* (- ac c) (- ac c)) (* (- ar r) (- ar r)))) ; calculate hypotenuse
      invalid-target))) ; else crazy distance for unviable tile

(defn distance-to-target
  "Employs unique targeting modes relative to Pac-Man from the tile in the ghost's given relative direction.
   This function is the core of the ghost AI, implementing specific rules about how ghosts choose
   their target location. The return value is a simple cartesian distance calculation in tiles."
  [g a d p b]
  (let [td (translate-direction a d) ; converts from relative to absolute
        pc (xtocol g (@p :x))
        pr (ytorow g (@p :y))
        pd (@p :d)
        n (@a :nickname)
        m (@a :mode)
        mid (@g :midc)
        
        ;; Calculate target tile. Default is Pac-Man himself. All other cases are special.
        ttc (cond (= m :reincarnate) (dec mid) ; Return to pen
                  ;; Home corners
                  (and (= n :shadow) (= m :scatter) (or (= (elroy-level g) 0) (< (elapsed g :boardclock) 10))) (@a :homec) ; Go to home corner every so often (except Cruise Elroy)
                  (and (not= n :shadow) (= m :scatter)) (@a :homec)

                  ;; Individual "personalities"
                  (and (= n :speedy) (= pd :up)) (- pc 4) ; Reproduces Pinky's erratic bug, targeting four tiles to the left if Pac-Man faces up
                  (= n :speedy) (+ pc (xoffset pd 4)) ; Pinky targets tile 4 squares ahead of Pac-Man
                  (and (= n :bashful) (= pd :up)) (- pc (- (xtocol g (@b :x)) (- pc 2))) ; Reproduces Inky's erratic bug, targeting two tiles to the left if Pac-Man faces up
                  (= n :bashful) (- pc (- (xtocol g (@b :x)) (+ pc (xoffset pd 2)))) ; Inky targets blinky's mirrored tile relative to 2 squares ahead of Pac-Man
                  (= n :pokey) (let [tiledist (tile-distance g a d pr pc)]
                                 (if (< tiledist 8) 0 pc)) ; Clyde seeks his corner within radius of 8 tiles of Pac-Man
                  :default pc) ; Shadow targets Pac-Man directly
        ttr (cond (= m :reincarnate) 17

                  (and (= n :shadow) (= m :scatter) (or (= (elroy-level g) 0) (< (elapsed g :boardclock) 10))) (@a :homer)
                  (and (not= n :shadow) (= m :scatter)) (@a :homer)

                  (= n :speedy) (+ pr (yoffset pd 4))
                  (= n :bashful) (- pr (- (ytorow g (@b :y)) (+ pr (yoffset pd 2))))
                  (= n :pokey) (let [tiledist (tile-distance g a d pr pc)]
                                 (if (< tiledist 8) 34 pr))
                  :default pr)
        tc (cond (< ttc 0) 0
                 (> ttc (dec (@g :mazecolumns))) (dec (@g :mazecolumns))
                 :default ttc)
        tr (cond (< ttr 0) 0
                 (> ttr (dec (@g :mazerows))) (dec (@g :mazerows))
                 :default ttr)
				distance (tile-distance g a d tr tc)
        tx (coltox g tc)
        ty (rowtoy g tr)]
    (swap! a assoc :tx tx :ty ty)
    (println (str "Measuring " (@a :nickname) "'s distance to " (name d) " target at " tr "," tc " : (" distance ") tiles"))
    distance))

(defn tunnel-exit-check?!
  "Checks for tunnel exit; moves actor to other end if needed"
  [g a]
  (let [x (@a :x)
        y (@a :y)
        r (ytorow g y)
        c (xtocol g x)
        d (@a :d)
        tunnel-depth 3] ; tunnel invisibly extends 3 tiles off-grid
	  (when (= (maze-tile (@g :maze) r c) "<")
	   (if (and (= d :left) (= c (- tunnel-depth)))
	     (do (set-actor-position a (coltox g (dec (+ tunnel-depth (dec (@g :mazecolumns))))), y)
		       (println (str (name (@a :nickname)) " went through the left tunnel")))
		   (when (and (= d :right) (= c (+ tunnel-depth (dec (@g :mazecolumns)))))
		     (set-actor-position a (inc (- (coltox g 0) tunnel-depth)), y)
	       (println (str (name (@a :nickname)) " went through the right tunnel")))))))

(defn stuck-in-wall?
  "Determines if actor is stuck in wall"
  [g a]
  (when-let [stuck (not (path-tile? (maze-tile (@g :maze) (ytorow g (@a :y)) (xtocol g (@a :x)))))]
    (println (str (name (@a :nickname)) " is stuck in a wall!"))
    stuck))

(defn random-direction
  "Returns a random, translated, open direction for the ghost: left, right, or forward.
  Used in frightened mode."
  [g a]
  (let [r (rand-int 3)
        td (cond (= r 0) :left
				         (= r 1) :right
                 :default :forward)]
    (if (ghost-my-openpath? g a td)
      (translate-direction a td)
      (if (some #(ghost-my-openpath? g a %) [:left :right :forward])
				(random-direction g a) ; guess another direction if that way blocked
        (@a :d))))) ; keep going into the void (tunnel case!)

(defn tie?
  "Checks for a tie between two directions"
  [a td d d1 d2 d3]
	(and (= (translate-direction a td) d)
			 (not= d1 invalid-target)
			 (or (= d1 d2)
				   (= d1 d3))))

(defn tiebreak?
  "Determines whether a tie needs breaking between two directions"
  [a d d1 d2 d3]
  (or (tie? a :forward d d1 d2 d3)
      (tie? a :right d d2 d1 d3)
      (tie? a :left d d3 d1 d2)))

(defn turn-ghost
  "Potentially turns ghost based on decision AI.
   This is a top level AI that measures whether
   left, right, or forward is the best way to go."
  [g a p b]
  (let [d (@a :d)
        x (@a :x)
        y (@a :y)
        r (ytorow g y)
        c (xtocol g x)]
    (println (str "Potentially turning " (name (@a :nickname)) ". lastrow=" (@a :lastrow) " lastcol=" (@a :lastcol) " currow=" r " curcol=" c " curdir=" (name d) " nextdir=" (name (@a :nd))))
    (set-ghost-direction? g a (@a :nd)) ; turn ghost to previously decided direction
    (println (str (name (@a :nickname)) "'s direction is now " (name d) ". nextdir=" (name (@a :nd))))
    (swap! a assoc :lastrow r :lastcol c) ; wait until next tile to turn again

    ;; Make ghost decide which way to turn at next tile
    (let [d (@a :d)
          ld (distance-to-target g a :left p b)
          rd (distance-to-target g a :right p b)
          fd (distance-to-target g a :forward p b)
          mid (@g :midc)
          new-direction (cond (and (= d :up) (or (= r 14) (= r 15)) (or (= c mid) (= c (dec mid)))) (do (println "Leaving pen") (if (< (elapsed g :modeclock) 15) :left :right)) ; always turn left or right when leaving pen
	                            
                              (or (= (@a :mode) :frightened) (= (@a :mode) :random)) (random-direction g a)
                              
                              (every? #(= % invalid-target) [fd ld rd]) d ; special case for tunnel

                              (and (< ld rd) (< ld fd)) (translate-direction a :left) ; turn left
															(and (< rd ld) (< rd fd)) (translate-direction a :right) ; turn right
															(and (< fd ld) (< fd rd)) d ; keep going forward

                              ;; Break ties, favoring up, left, down, right in that order
                              (tiebreak? a :up fd rd ld) :up
                              (tiebreak? a :left fd rd ld) :left
                              (tiebreak? a :down fd rd ld) :down
                              (tiebreak? a :right fd rd ld) :right

															:default :up)]

      (when (not= (@a :nd) new-direction)
        (swap! a assoc :nd new-direction)))))

(defn update-ghost-position!
  "Either turns ghost or moves ghost in current direction and speed.
   This is essentially a controller function that decides when to make decisions
   and when to update the physical ghost location."
  [g a p b]
  (let [c (xtocol g (@a :x))
        r (ytorow g (@a :y))
        level (nth levels (dec (@g :level)))
        t (maze-tile (@g :maze) r c)
        mid (@g :midc)
        gs (cond (in-pen? g a) (* maxspeed (:ghosttunnelspeed level))  ; 40% slow down in pen
                 (= (@a :mode) :reincarnate) (* maxspeed 1.05) ; speed up in reincarnate mode
								 (= (@a :mode) :frightened) (* maxspeed (:frightghostspeed level)) ; 50% slow down in frightened mode
                 (some #(= t %) ["#" "<" "X" "x" "-"]) (* maxspeed (:ghosttunnelspeed level))  ; 40% slow down in tunnels and pen
                 (and (= (@a :nickname) :shadow) (= (elroy-level g) 1)) (* maxspeed (:elroy1speed level)) ; speed-up in Cruise Elroy 1 mode
                 (and (= (@a :nickname) :shadow) (= (elroy-level g) 2)) (* maxspeed (:elroy2speed level)) ; speed-up in Cruise Elroy 2 mode
                 :default (@a :s))
        pen-timeout (or (and (< (@g :level) 5) (> (elapsed g :dotclock) 4))
                        (and (>= (@g :level) 5) (> (elapsed g :dotclock) 3)))]
  
  (when (near-center-of-tile? g a (* (@a :s) 2)) ; must be near center of tile
	  (when (@a :reverse)
	    (reverse-ghost-direction? g a)) ; allow reverse if flag set
	  ;; Evaluate forced turns or potential turns at intersection
	  (when (and (new-ghost-tile? g a) ; must be in a different tile than last time
	             (> c 0) (< c (dec (@g :mazecolumns)))) ; must not be in tunnel
	    (turn-ghost g a p b)))

  ;; Move ghost
  (if (or pen-timeout
            (>= (@g :globaldots) (@a :dotlimit))
            (and (< (@g :globaldots) 0) (<= (@a :dotcount) 0))) ; Don't leave pen until Pac-Man has eaten enough dots
    (do
	    (when pen-timeout
	      (println "Releasing the next ghost!")
	      (swap! g update-in [:globaldots] + 10) ; force the ghosts out of the pen over time if not enough dots eaten
	      (swap! g assoc :dotclock (now)))
	    (set-actor-position a
												  (+ (@a :x) (actor-xoffset a gs))
												  (+ (@a :y) (actor-yoffset a gs)))
	
	    (when (stuck-in-wall? g a)
	      (while (stuck-in-wall? g a) ; Back up if embedded in wall
	        (set-actor-position a
	                            (- (@a :x) (actor-xoffset a 1))
	                            (- (@a :y) (actor-yoffset a 1))))
		    (println (str (name (@a :nickname)) " got unstuck!!!"))
        (swap! a update-in [:d] opposite-direction))

		  ;; Check for tunnel exit  
		  (tunnel-exit-check?! g a))
    (do
      nil)
   )

  ;; Force ghosts to turn up when leaving pen
  (when (leaving-pen? g a gs)
    (swap! a assoc :d :up :nd :up))))

(defn update-pacman-position?
  "Move Pac-Man in current direction and speed, if possible"
  [g a]
  (let [x (@a :x)
        y (@a :y)
        r (ytorow g y)
        c (xtocol g x)
        dr (@a :dotr) ; last eaten dot location
        dc (@a :dotc)
        d (@a :d)
        bs (elapsed g :blueclock)
        level (nth levels (dec (@g :level)))
        s (cond (and (= r dr) (= c dc) (< bs (:frighttime level))) (* maxspeed (:frightpacdotspeed level)) ; slow down less when eating dots during blue time
                (and (= r dr) (= c dc)) (* maxspeed (:pacdotspeed level))  ; slow down when eating dots
                (< bs (:frighttime level)) (* maxspeed (:frightpacspeed level))  ; speed up during blue time
                :default (@a :s))  ; normal speed
				td (translate-direction a :forward)
        nr (+ r (actor-yoffset a 1))
        nc (+ c (actor-xoffset a 1))
				n (maze-tile (@g :maze) nr nc)
			  clear-path (path-tile? n)] ; Only defined tiles
      (if (or clear-path (not (near-center-of-tile? g a 3))) ; Only stop if near center of tile
				(do
	         ;; Move forward
					(when (not (@a :parked))
						(let [xo (actor-xoffset a s)
			            yo (actor-yoffset a s)]
							(set-actor-position a
																	(+ x (if clear-path xo (min xo (abs (- (coltox g c) x)))))
																	(+ y (if clear-path yo (min yo (abs (- (rowtoy g r) y))))))))
	         
	         ;; Wall check
	         (when (stuck-in-wall? g a)
	           (while (stuck-in-wall? g a) ; Back up if embedded in wall
	             (set-actor-position a
	                                 (- (@a :x) (actor-xoffset a 1))
	                                 (- (@a :y) (actor-yoffset a 1))))
				     (park-pacman! g a))
	         ;; Tunnel check
		       (tunnel-exit-check?! g a)
		       (swap! a assoc :parked nil))
       (park-pacman! g a))))

(defn collide?
  "Determines if Pac-Man has collided with ghost"
  [g pacman a]
  (and (not= (@g :paused) :intermission) (= (xtocol g (@pacman :x)) (xtocol g (@a :x))) (= (ytorow g (@pacman :y)) (ytorow g (@a :y)))))

(defn eaten?
  "Determines if Pac-Man has been eaten by the given ghost"
  [g pacman a]
  (and (not= (@a :mode) :frightened) (not= (@a :mode) :reincarnate) (collide? g pacman a)))

(defn eat-ghost?
  "Determines if Pac-Man has eaten a ghost"
  [g pacman a]
  (and (= (@a :mode) :frightened) (collide? g pacman a)))

(defn reincarnated?
  "Determines if ghost achieved reincarnation (i.e. eyes returned to pen)"
  [g a]
  (and (= (@a :mode) :reincarnate) (in-pen? g a)))

(defstruct ghost :nickname :c :dotlimit :dotcount :x :y :lastrow :lastcol :d :nd :s :mode :tx :ty :homer :homec :eatpause :reverse)
(defn new-ghost [& [n c dotlimit dotcount x y lastrow lastcol d nd s mode tx ty homer homec eatpause]] (atom (struct ghost n c dotlimit dotcount x y lastrow lastcol d nd s mode tx ty homer homec eatpause)))

(defstruct pacman :nickname :x :y :lastrow :lastcol :d :s :dotr :dotc :parked :waka :joystick)
(defn new-pacman [& [n x y lastrow lastcol d s dotr dotc parked waka joystick]] (atom (struct pacman n x y lastrow lastcol d s dotr dotc parked waka joystick)))

(defstruct game :maze :dots :mazerows :mazecolumns :midx :midc :joystick :joystick2 :h :w :tilesize :halftile :actorsize :halfactor :credits :highscore :sound :clock :boardclock :modeclock :blueclock :pauseclock :paused :fruitclock :fruit :bonusclock :dotclock :timer :level :style :ghostpoints :players :player :score1 :score2 :lives :bonuslife :globaldots :dotsleft :antialias :telemetry :demo :sirenclock :started)
(defn new-game [& [maze dots mazerows mazecolumns midx midc joystick joystick2 h w tilesize halftile actorsize halfactor credits highscore sound clock boardclock modeclock blueclock pauseclock paused fruitclock fruit bonusclock dotclock timer level style ghostpoints players player score1 score2 lives bonuslife globaldots dotsleft antialias telemetry demo sirenclock started]]
  (atom (struct game maze dots mazerows mazecolumns midx midc joystick joystick2 h w tilesize halftile actorsize halfactor credits highscore sound clock boardclock modeclock blueclock pauseclock paused fruitclock fruit bonusclock dotclock timer level style ghostpoints players player score1 score2 lives bonuslife globaldots dotsleft antialias telemetry demo sirenclock started)))

(defn set-game-joystick [g j d]
  (println (str "Player moved joystick " j " " (name d)))
  (swap! g assoc (if (= j 1) :joystick :joystick2) d))

(defn initialize-all-clocks
	"Initializes the various timers used in the game"
	[g]
	(let [ct (now)]
		(swap! g assoc :clock ct
									 :boardclock ct
									 :modeclock ct
									 :blueclock (- ct 100000)
									 :pauseclock ct
									 :sirenclock (- ct 100000)
									 :bonusclock ct
									 :dotclock ct
									 :fruitclock ct)))

(defn stop-game!
  "Stops the game (i.e. GAME OVER)"
  [g]
  (swap! g assoc :started false) (let [#^Timer t (@g :timer)] (.stop t)))

(defn start-game!
  "Initiates the beginning of the game in response to player pressing 1 or 2 player start"
  [g p]
  (initialize-all-clocks g)
  (play-sound g :intro)
  (swap! g assoc :started true :players p) (let [#^Timer t (@g :timer)] (.start t)))

(defn reset-ghost!
  "Reincarnates the ghost in the pen"
  [g a]
  (println (str (name (@a :nickname)) " is reincarnated"))
  (swap! a assoc :x (@g :midx) :y (rowtoy g 17) :d :up :nd :up)
  (set-ghost-mode g a :scatter))

(defn reset-dot-counters
  "Sets pen exit times for ghosts based on level"
  [g blinky pinky inky clyde]
	(swap! g assoc :globaldots -999999)

  (swap! blinky assoc :dotcount 0)
  (swap! pinky assoc :dotcount 0)
  (swap! inky assoc :dotcount (if (= (@g :level) 1) 30 0)) ; Inky stays in pen longer on first level
  (swap! clyde assoc :dotcount (cond (= (@g :level) 1) 60 ; Clyde stays in pen longer on first two levels
					                           (= (@g :level) 2) 50
					                           :default 0)))

(defn reset-actors
  "Resets actors to default positions, speeds, and direction.
  Also resets the game clock and sets pause mode to start."
  [g pacman mspacman blinky pinky inky clyde]
  (let [half (@g :halftile)
        mid (@g :midx)
        py (rowtoy g 17)
        level (nth levels (dec (@g :level)))]
    (println (str "Setting up the actors for level " (@g :level)))
    
    ; Pac-Man settings
	  (swap! pacman assoc :x (+ (@g :midx) (if (= (@g :players) 2) (@g :tilesize) 0))
							          :y (rowtoy g 26)
			                  :lastrow 0
			                  :lastcol 0
				                :d (if (and (@g :demo) (= (rand-int 2) 0)) :right :left)
	                      :s (* maxspeed (:pacspeed level))
                        :joystick :joystick)
	  (swap! mspacman assoc :x (- (@g :midx) (@g :tilesize))
							          :y (rowtoy g 26)
			                  :lastrow 0
			                  :lastcol 0
				                :d :right
	                      :s (* maxspeed (:pacspeed level))
                        :joystick :joystick2)
  
    ; common ghost settings
    (doseq [a [blinky pinky inky clyde]]
			(swap! a assoc :s (* maxspeed (:ghostspeed level))
                     :lastrow 0 :lastcol 0
                     :reverse false
                     :mode :scatter
                     :tx 0 :ty 0))

    ; unique ghost settings
	  (swap! blinky assoc :x mid                                :y (rowtoy g 14) :homec (- (@g :mazecolumns) 3) :homer 0                    :d :left  :nd :left  :eatpause :eat-blinky)
    (swap! pinky assoc  :x mid                                :y py            :homec 2                       :homer 0                    :d :up    :nd :up    :eatpause :eat-pinky)
    (swap! inky assoc   :x (+ 2 (- mid (* (@g :tilesize) 2))) :y py            :homec (dec (@g :mazecolumns)) :homer (- (@g :mazerows) 2) :d :right :nd :right :eatpause :eat-inky)
    (swap! clyde assoc  :x (- (+ mid (* (@g :tilesize) 2)) 2) :y py            :homec 0                       :homer (- (@g :mazerows) 2) :d :left  :nd :left  :eatpause :eat-clyde)

    (when (not= (@g :paused) :intermission)
	    (swap! g assoc :paused :start :pauseclock (now)))
    (swap! g assoc :fruit :none ; clear any uneaten fruit from board
				           :joystick :left
				           :joystick2 :right
				           :modeclock (now)
		               :boardclock (now))))

(defn reset-board
  "Should be called at the start of each new level (except first) to initialize"
  [g pacman mspacman blinky pinky inky clyde]
  (let [maxlevel (count levels)
        inclevel (inc (@g :level))
        newlevel (if (> inclevel maxlevel) maxlevel inclevel)] ; make last level repeat forever

	  (println (str "Setting up a new board with " (count (dots newlevel)) " dots"))
    (swap! g assoc :level newlevel)
    (when (:intermission (nth levels (dec (@g :level))))
      (play-sound g :intermission)
      (swap! g assoc :paused :intermission :pauseclock (now)))
    (reset-dot-counters g blinky pinky inky clyde)
	  
    (let [m (maze (@g :level))]
		  (swap! g assoc :maze m :style (:style (nth levels (dec (@g :level)))) :mazecolumns (maze-columns m) :mazerows (maze-rows m) :midc (round (/ (maze-columns m) 2)) :dots (dots (@g :level)) :dotsleft (count (dots (@g :level)))))
    (reset-actors g pacman mspacman blinky pinky inky clyde)))

(defn reset-life
  "Called each time Pac-Man starts a new life (including first)"
  [g pacman mspacman blinky pinky inky clyde]
  (println "Getting Pac-Man a fresh life")
  (reset-actors g pacman mspacman blinky pinky inky clyde)
  (reset-dot-counters g blinky pinky inky clyde)
  (when (< (@g :dotsleft) 244)
	  (swap! g assoc :globaldots -7)) ; only reset global dot counter when life lost during level
  (if (not (@g :demo))
    (swap! g update-in [:lives] dec) ; use a spare life (except in demo mode)
    (when (< (@g :dotsleft) 20) ; skip to next level in demo mode
      (swap! g assoc :paused :level)
		  (reset-board g pacman mspacman blinky pinky inky clyde))))

(defn reset-game!
  "Called once at the start of a game"
  [g pacman mspacman blinky pinky inky clyde]
  (println "Starting a new game")
	(let [m (maze 1)]
    (swap! g assoc  :level 1 :lives 4 :bonuslife :unearned :score1 0 :score2 0 :antialias true :telemetry false :demo false
										:maze m :style (:style (first levels)) :mazecolumns (maze-columns m) :mazerows (maze-rows m) :midc (round (/ (maze-columns m) 2)) :dots (dots 1) :dotsleft (count (dots 1))))
	(reset-life g pacman mspacman blinky pinky inky clyde)
  (reset-dot-counters g blinky pinky inky clyde))

(defn draw-pacman [g gr pacman mult thick]
  (let [size (* (@g :actorsize) mult)
  		half (* (@g :halfactor) mult)
	  	x (- (@pacman :x) half)
	    y (- (@pacman :y) half)
        d (@pacman :d)
			  e125 (every g 125)
        parked (@pacman :parked)
        board-seconds (elapsed g :boardclock)
	      mouth-angle (cond (and parked (= d :right)) 15
	                        (and parked (= d :up)) 105
	                        (= d :right) (if (and (not parked) e125) 15  45)
	                        (= d :up)    (if (and (not parked) e125) 105 135)
	                        (= d :left)  (if (and (not parked) e125) 195 225)
	                        (= d :down)  (if (and (not parked) e125) 285 315))
	      ;; make mouth chomp every 1/16 second if Pac-Man is moving
	      mouth-wide (cond (or (and (not= (@g :paused) :intermission) (< board-seconds 5)) ; wait a few seconds at the start of each life
	                           (= (@g :paused) :death)
	                           (= (@g :paused) :level)
	                           (and (not parked)
	                                (every g 62))) 360 ; full circle (mouth closed)
	                       (or (and (not parked) e125)
	                           (and parked (or (= d :right) (= d :up)))) 330 ; slightly agape
	                       :default 270)] ; open wide
	  (.setColor gr (if (= (@pacman :nickname) :pacman) Color/YELLOW Color/MAGENTA))
		(cond (and (= (@g :paused) :death) (>= (elapsed g :pauseclock) 2.5))
		        (do (.setStroke gr thick)
		            (dotimes [n 8]
		              (.fillArc gr x, y size, size (- (* n 45) 5), 10))
		            (.setColor gr Color/BLACK)
		            (.fillOval gr (+ x (/ half 2)), (+ y (/ half 2)), half half)) ;draw "poof" effect
		      (and (= (@g :paused) :death) (>= (elapsed g :pauseclock) 1))
		        (let [n (round (/ (- (now) (@g :pauseclock)) 16))]
		          (.fillArc gr x, y size, size (+ 90 (* n 0.9)), (- 360 (* n 1.8)))) ; draw "withering" effect
		        
		      :default (.fillArc gr x, y size, size mouth-angle, mouth-wide)))) ; draw normal chomping mouth

(defn draw-ghost
  "Draws ghost. Eye locations should be at 1/3 and 2/3 across"
  [g gr ghost thick]
  (let [size (double (@g :actorsize))
  	    half (double (@g :halfactor))
  	    x (double (- (@ghost :x) half))
        y (double (- (@ghost :y) half))
        level (nth levels (dec (@g :level)))
        frighttime (:frighttime level)
        flashtime (if (< frighttime 3) frighttime (round (/ frighttime 3)))
        flashwhite (or (every g 250) (= (@g :paused) :level) (= (@g :paused) :death))
        flash (and (= (@ghost :mode) :frightened) (> (elapsed g :blueclock) (- frighttime flashtime)))
        color (cond flash (if flashwhite (:white game-colors) (:ghostblue game-colors))
		                (= (@ghost :mode) :frightened) (:ghostblue game-colors)
                    :default (@ghost :c))
        eyecolor (cond flash (if flashwhite (:red game-colors) (:peach game-colors))
											 (= (@ghost :mode) :frightened) (:peach game-colors)
											 :default (:white game-colors))
        thirdsize (round (* size 1/3))
        eye (round (/ size 3.4)) ; 3.3
        halfeye (round (/ eye 2))
        ball (max (/ (@g :tilesize) 4.0) (/ size 7.0))
        halfball (/ ball 2)
        ballx (double (xoffset (@ghost :nd) halfball))
        bally (double (yoffset (@ghost :nd) halfball))
        gown (round (/ size 7)) ; gown "length"
        gy (dec (- (+ y size) gown)) ; gown top coordinate
        gw (/ size 5) ; width of gown segment (top)
        gw2 (/ size 9) ; width of gown segment (bottom)
        gw3 (/ size 18) ; width of gown segment (bottom)
        taper (/ size 36)]
		(when (and (not (and (= (@g :paused) :level) (> (elapsed g :pauseclock) 1))) (not= (@ghost :mode) :reincarnate))
			 ;; head
			 (.setStroke gr (new BasicStroke 1))
			 (.setColor gr color)
	     (.fillArc gr (+ x taper), y (- size (* taper 2)), size 0, 180)
       (let [halfgown (round (/ gown 2))
             fourthsize (round (* size 4/9))
             poly (new Polygon)]
         ;; body
         (. poly (addPoint (+ x size) gy))
         (. poly (addPoint (- (+ x size) taper) (dec (+ y half))))
         (. poly (addPoint (+ x taper) (dec (+ y half))))
         (if (and (not= (@g :paused) :death) (not= (@g :paused) :level) (every g 142))
           (do
             ;; Four gown sections
		         (. poly (addPoint x gy))
		         (. poly (addPoint x (+ gy gown)))
		         (. poly (addPoint (+ x gw3) (+ gy gown)))
		         (. poly (addPoint (+ x gw) gy))
		
		         (. poly (addPoint (+ x gw) gy))
		         (. poly (addPoint (+ x gw gw2) (+ gy gown)))
		         (. poly (addPoint (+ x gw gw) (+ gy gown)))
		         (. poly (addPoint (+ x gw gw) gy))
		
		         (. poly (addPoint (- (+ x size) gw gw) gy))
		         (. poly (addPoint (- (+ x size) gw gw) (+ gy gown)))
		         (. poly (addPoint (- (+ x size) gw gw2) (+ gy gown)))
		         (. poly (addPoint (- (+ x size) gw) gy))
		
		         (. poly (addPoint (- (+ x size) gw3) (+ gy gown)))
		         (. poly (addPoint (+ x size) (+ gy gown))))
           (do
             ;; Three gown sections
		         (. poly (addPoint x gy))
		         (. poly (addPoint (+ x gw3) (+ gy gown)))
		         (. poly (addPoint (+ x gw2 gw3) (+ gy gown)))
		         (. poly (addPoint (+ x gw2 gw2 gw2) gy))
		
		         (. poly (addPoint (+ x gw2 gw2 gw2 gw2) (+ gy gown)))
		         (. poly (addPoint (+ x gw2 gw2 gw2 gw2 gw2) (+ gy gown)))
		         (. poly (addPoint (+ x gw2 gw2 gw2 gw2 gw2 gw2) gy))
		
		         (. poly (addPoint (- (+ x size) gw2 gw3) (+ gy gown)))
		         (. poly (addPoint (- (+ x size) gw3) (+ gy gown)))))
         (.fillPolygon gr poly)))

		 (when (not (and (= (@g :paused) :death) (> (elapsed g :pauseclock) 1)))
	     ;; eyes
			 (.setColor gr eyecolor)
       (let [topeye (round (/ size 5))
             sideways (if (or (= (@ghost :nd) :left) (= (@ghost :nd) :right)) 3 0)]
				 (if (not= (@ghost :mode) :frightened)
			     (do (.fillOval gr (- (- (+ x ballx thirdsize) halfeye) 2),           (dec (+ y bally topeye)) eye, (+ eye halfball))
							 (.fillOval gr (+ (- (+ x ballx thirdsize thirdsize) halfeye) 1), (dec (+ y bally topeye)) eye, (+ eye halfball))
							 ;; eyeballs
							 (.setColor gr (:ghostblue game-colors))
					     (.fillOval gr (- (- (+ x thirdsize (* ballx 2)) halfball) 2),           (- (+ y sideways bally halfeye topeye (* bally 1.45)) halfball) ball, ball)
							 (.fillOval gr (+ (- (+ x thirdsize thirdsize (* ballx 2)) halfball) 1), (- (+ y sideways bally halfeye topeye (* bally 1.45)) halfball) ball, ball))
			     ;; frightened ghost
	         (do (.fillRect gr (- (+ x 1 thirdsize) halfball),           (+ y (round (/ size 4))) ball, ball)
							 (.fillRect gr (- (+ x 1 thirdsize thirdsize) halfball), (+ y (round (/ size 4))) ball, ball)
		           ;; frown
		           (let [fx (+ x (round (/ size 8.8)))
		                 fy (+ y (* half 1.25))
	                     fy2 (+ y (* half 1.15))
		                 fs (* size 0.165)]
				         (.setStroke gr thick)
		             (.drawArc gr fx, fy fs, fs 30 150)
		             (.drawArc gr (+ fx fs), fy2 fs, fs 210 120)
				         (.drawArc gr (+ fx fs fs), fy fs, fs 30 120)
		             (.drawArc gr (+ fx fs fs fs), fy2 fs, fs 210 120)
				         (.drawArc gr (+ fx fs fs fs fs), fy fs, fs 0 150))))))))

(defn chomp-ghost
  "Checks to see if Pac-Man ate a ghost"
  [g pacman ghost]
	(when (and (not (chomping-ghosts? g)) (not= (@g :paused) (@ghost :eatpause)) (eat-ghost? g pacman ghost))
   (swap! g assoc :paused (@ghost :eatpause))
   (swap! g update-in [:ghostpoints] * 2)
   (set-game-score g (+ (@g :ghostpoints) (@g :score1)) 1)
	 (println (str "Pac-Man ate " (name (@ghost :nickname))))
	 (play-sound g :ghost)
   (swap! g update-in [:blueclock] + 1000)
   (swap! g assoc :pauseclock (now))))

(defn chomp-ghosts
  "Checks to see if Pac-Man ate any ghosts"
  [g pacman blinky pinky inky clyde]
  (when (not= (@g :paused) :death) ;an eaten Pac-Man consumes no ghosts
    (doseq [a [blinky pinky inky clyde]]
		  (chomp-ghost g pacman a))))

(defn capture-pacman
  "Checks to see if a ghost ate Pac-Man"
  [g pacman mspacman blinky pinky inky clyde]
	(when (and (not= (@g :paused) :death) ;not dead already
	           (some #(eaten? g pacman %) [blinky pinky inky clyde])) ;one of the ghosts ate him
	   (swap! g assoc :paused :death :pauseclock (now))
     (println "Pac-Man died")
		 (play-sound g :death))
	(when (and (= (@g :players) 2)
             (not= (@g :paused) :death) ;not dead already
	           (some #(eaten? g mspacman %) [blinky pinky inky clyde])) ;one of the ghosts ate him
	   (swap! g assoc :paused :death :pauseclock (now))
     (println "Ms. Pac-Man died")
		 (play-sound g :death))
	(when (and (= (@g :paused) :death) (>= (elapsed g :pauseclock) 3))
	   (reset-life g pacman mspacman blinky pinky inky clyde)))

(defn draw-levels
  "Displays the fruits at the bottom of the maze to indicate current level"
  [g gr]
  (let [size (@g :actorsize)
        half (@g :halfactor)
        l (if (= (@g :paused) :intermission) (dec (@g :level)) (@g :level))]
		(dotimes [n 7]
		  (let [fruitlevel (- l (- 7 n)) ; show most recent 7 levels
		        fruitoffset (if (< l 7) (* (- 7 l) (+ (@g :tilesize) half)) 0)]
		  (when (>= fruitlevel 0)
		    (.drawImage gr ((:fruit (nth levels fruitlevel)) images) (+ fruitoffset (- (coltox g (- (+ (@g :midc) 10) n)) (* n half))),  (- (rowtoy g (- (@g :mazerows) 2)) (/ (@g :tilesize) 4)) size, size nil))))))

(defn draw-target
  "Displays the ghost's target rectangle"
  [g gr a]
	(when (and (not= (@a :mode) :frightened) (not= (@a :mode) :random))
	  (.setColor gr (@a :c))
		(.fillRect gr (- (coltox g (xtocol g (@a :tx))) (@g :halftile)), (- (rowtoy g (ytorow g (@a :ty))) (@g :halftile)) (@g :tilesize), (@g :tilesize))))

(defn gameworld-frame
  "Represents one instant of time in the game"
  [g pacman mspacman blinky pinky inky clyde]
  (proxy [JFrame ActionListener MouseMotionListener KeyListener] []
    (paint [grf]
     (let [#^JFrame me this
           #^BufferStrategy bs (.getBufferStrategy me)
           #^Graphics2D gr (if (not= nil bs) (. bs getDrawGraphics) nil)
           thick (new BasicStroke (round (max 3 (/ (@g :tilesize) 10))))] ; Lines should be at least 3 pixels thick on low-res displays
       (when (not= nil gr)
         ;; clear screen
         (.setColor gr Color/BLACK)
         (.fillRect gr 0 0 (@g :w) (@g :h))
         (.setFont gr (Font. "ArcadeClassic" Font/BOLD (+ (round (@g :actorsize)) 3)))

         ;; Turn on anti-aliasing
         (if (@g :antialias)
           (.setRenderingHint gr RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
           (.setRenderingHint gr RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_OFF))

         ;; draw intermission
				 (if (= (@g :paused) :intermission)
		       (let [es (elapsed g :pauseclock)]
	           (cond (< es 5) (do 
	                             ;; Intermission I, part 1 - Blinky chases Pac-Man from right to left
															 (set-actor-position pacman (- (@g :w) (* (/ (@g :w) 5) es))
															                            (+ (/ (@g :h) 2) (@g :actorsize)))
															 (set-actor-direction pacman :left)
															 (swap! pacman assoc :parked nil)
															 (draw-pacman g gr pacman 1 thick)
															
															 (set-actor-position blinky (+ (- (@g :w) (* (/ (@g :w) 5) es)) (* (@g :actorsize) 2.2))
															                            (+ (/ (@g :h) 2) (@g :actorsize)))
															 (set-actor-direction blinky :left)
							                 (swap! blinky assoc :nd :left)
															 (draw-ghost g gr blinky thick)
															
															 (draw-levels g gr))
									(and (>= es 5) (< es 10)) (do 
	                             ;; Intermission I, part 2 - "Super" Pac-Man chases frightened Blinky from left to right
															 (let [pacx (- (* (* (/ (@g :w) 5) (- es 5)) 2) (@g :w))
							                       ghostdist (* (@g :actorsize) 2.2)] 
	                               (set-actor-position pacman pacx
																                            (+ (@g :halftile) (- (/ (@g :h) 2) (/ (@g :actorsize) 2))))
																 (set-actor-direction pacman :right)
																 (swap! pacman assoc :parked nil)
																 (draw-pacman g gr pacman 3 thick)
																
																 (set-actor-position blinky (+ (* (/ (@g :w) 5) (- es 5)) (+ (* (/ (- (@g :w) pacx) (@g :w)) ghostdist) (@g :actorsize)))
																                            (+ (/ (@g :h) 2) (@g :actorsize)))
																 (set-actor-direction blinky :right)
									               (set-ghost-mode g blinky :frightened)
	                               (swap! g assoc :blueclock (now))
																 (draw-ghost g gr blinky thick))
															
															 (draw-levels g gr))
				         :default (do (swap! g assoc :paused :none)
						                  (reset-actors g pacman mspacman blinky pinky inky clyde))))
		       (do
         
         ;; draw maze
         (let [size (@g :tilesize)
							 dotsize (round (/ size 4))
		           ds (/ dotsize 2)
							 doublesize (* size 2)
							 half (@g :halftile)
							 e125 (every g 125)
							 e250 (every g 250)
	             thelevel (nth levels (dec (@g :level)))
               mazecolor (cond (and e250 (= (@g :paused) :level) (> (elapsed g :pauseclock) 1)) (:white game-colors) ; flash white at end of level
                               (= (:mazecolor thelevel) :psychedelic) (let [cr (rand-int 255) cg (rand-int 255) cb (rand-int 255)] (new Color cr cg cb)) ; psychedelic
                               :default ((:mazecolor thelevel) game-colors))
               solidcolor (cond (and e250 (= (@g :paused) :level) (> (elapsed g :pauseclock) 1)) Color/BLACK ; blink at end of level
                                (= (:solidcolor thelevel) :psychedelic) (let [cr (rand-int 255) cg (rand-int 255) cb (rand-int 255)] (new Color cr cg cb)) ; psychedelic
                                :default ((:solidcolor thelevel) game-colors))
               solid (not= solidcolor Color/BLACK)]
           (doseq [[rowindex row] (map vector (iterate inc 0) (@g :maze))]
             (doseq [[colindex ch] (map vector (iterate inc 0) row)]
               (let [c (str ch)
                    ox (coltox g colindex) ;center of tile
                    oy (rowtoy g rowindex) ;center of tile
                    x (- ox half) ;actual drawing location
                    y (+ oy half)] ;actual drawing location
                (cond (some #(= c %) [" " "X" "x" "#" "^" "<"]) nil ; empty maze path
                      ;; draw maze part
                      :default (let [overleft (- ox size)
																		upone (- oy size)
																		halfright (+ ox half)
																		halfup (- oy half)]
                                (.setStroke gr thick)
                                (cond (or (= c "·") (= c "*")) (when (some {[rowindex colindex] []} (@g :dots)) ; only show uneaten dots
	                                                               (.setColor gr ((:dotcolor (nth levels (dec (@g :level)))) game-colors))
                                                                 (.fillRect gr (- ox ds), (- oy ds) dotsize, dotsize))
                                      (= c "─") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, half))
                                                    (.setColor gr mazecolor)
							                                      (.drawLine gr x, oy halfright, oy))
                                      (= c "═") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, oy size, half))
                                                    (.setColor gr mazecolor)
							                                      (.drawLine gr x, oy halfright, oy))
                                      (= c "│") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup half, size))
                                                    (.setColor gr mazecolor)
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "║") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr ox, halfup half, size))
                                                    (.setColor gr mazecolor)
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "┌") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr ox, oy size, size 90, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, oy size, size 90, 90)) 
                                      (= c "┐") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr overleft, oy size, size 0, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, oy size, size 0, 90))
                                      (= c "└") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr ox, upone size, size 180, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, upone size, size 180, 90))
                                      (= c "┘") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr overleft, upone size, size 270, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, upone size, size 270, 90))
                                      (= c "┼") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr ox, oy size, size 90, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, oy size, size 90, 90)) 
                                      (= c "╬") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr overleft, upone size, size 270, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, upone size, size 270, 90))
                                      (= c "╖") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr overleft, oy size, size 0, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, oy size, size 0, 90))
                                      (= c "╘") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr ox, upone size, size 180, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, upone size, size 180, 90))
                                      (= c "╕") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
						                                                    (.fillArc gr overleft, oy size, size 0, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, oy size, size 0, 90)
                                                    (.drawLine gr x, halfup halfright, halfup))
                                      (= c "╒") (do (when solid (.setColor gr solidcolor)
						                                                    (.fillRect gr x, halfup size, size)
	                                                              (.setColor gr Color/BLACK)
						                                                    (.fillArc gr ox, oy size, size 90, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, oy size, size 90, 90)
                                                    (.drawLine gr x, halfup halfright, halfup))
                                      (= c "╛") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
						                                                    (.fillArc gr overleft, upone size, size 270, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, upone size, size 270, 90)
                                                    (.drawLine gr x, y halfright, y))
                                      (= c "╧") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
						                                                    (.fillArc gr ox, upone size, size 180, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, upone size, size 180, 90)
                                                    (.drawLine gr x, y halfright, y))
                                      (= c "╢") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
						                                                    (.fillArc gr overleft, oy size, size 0, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, oy size, size 0, 90)
	                                                  (.drawLine gr halfright, y halfright, halfup))
                                      (= c "╜") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
						                                                    (.fillArc gr overleft, upone size, size 270, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, upone size, size 270, 90)
	                                                  (.drawLine gr halfright, y halfright, halfup))
                                      (= c "╙") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
						                                                    (.fillArc gr ox, upone size, size 180, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, upone size, size 180, 90)
	                                                  (.drawLine gr x, y x, halfup))
                                      (= c "╟") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
						                                                    (.fillArc gr ox, oy size, size 90, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, oy size, size 90, 90)
	                                                  (.drawLine gr x, y x, halfup))
                                      (= c "▄") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup size, half))
                                                    (.setColor gr mazecolor)
                                                    (.drawLine gr x, halfup halfright, halfup)
	                                                  (.drawLine gr x, oy halfright, oy))
                                      (= c "▀") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, oy size, half))
                                                    (.setColor gr mazecolor)
                                                    (.drawLine gr x, y halfright, y)
	                                                  (.drawLine gr x, oy halfright, oy))
                                      (= c "▐") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, halfup half, size))
                                                    (.setColor gr mazecolor)
                                                    (.drawLine gr x, halfup x, y)
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "▌") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr ox, halfup half, size))
                                                    (.setColor gr mazecolor)
                                                    (.drawLine gr halfright, halfup halfright, y)
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "╔") (do (when solid (.setColor gr solidcolor)
					                                                      (.fillArc gr x, halfup doublesize, doublesize 90, 90)
		                                                            (.setColor gr Color/BLACK)
				                                                        (.fillArc gr ox, oy size, size 90, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr ox, oy size, size 90, 90)
	                                                  (.drawArc gr x, halfup doublesize, doublesize 90, 90))
                                      (= c "╗") (do (when solid (.setColor gr solidcolor)
																																(.fillArc gr (- overleft half), halfup doublesize, doublesize 0, 90)                                                               
		                                                            (.setColor gr Color/BLACK)
					                                                      (.fillArc gr overleft, oy size, size 0, 90))
                                                    (.setColor gr mazecolor)
                                                    (.drawArc gr overleft, oy size, size 0, 90)
	                                                  (.drawArc gr (- overleft half), halfup doublesize, doublesize 0, 90))
                                      (= c "╚") (do (when solid (.setColor gr solidcolor)
																																(.fillArc gr x, (- oy size half) doublesize, doublesize 180, 90)                                                               
		                                                            (.setColor gr Color/BLACK)
					                                                      (.fillArc gr ox, upone size, size 180, 90))
                                                    (.setColor gr mazecolor)
	                                                  (.drawArc gr ox, upone size, size 180, 90)
	                                                  (.drawArc gr x, (- oy size half) doublesize, doublesize 180, 90))
                                      (= c "╝") (do (when solid (.setColor gr solidcolor)
																																(.fillArc gr (- ox size half), (- oy size half) doublesize, doublesize 270, 90)                                                             
		                                                            (.setColor gr Color/BLACK)
					                                                      (.fillArc gr overleft, upone size, size 270, 90))
                                                    (.setColor gr mazecolor)
	                                                  (.drawArc gr overleft, upone size, size 270, 90)
	                                                  (.drawArc gr (- ox size half), (- oy size half) doublesize, doublesize 270, 90))
                                      (= c "●") (when (and (@g :started) ; don't show energizers until game starts
                                                         (some {[rowindex colindex] []} (@g :dots))) ; only show uneaten energizers
		                                              (.setColor gr (if e250 Color/BLACK ((:dotcolor (nth levels (dec (@g :level)))) game-colors))) ; make energizer dots blink
                                                  (.fillOval gr x, halfup size, size))
                                      (= c "█") (do (when solid (.setColor gr solidcolor)
						                                                    (.fillRect gr x, halfup size, size)))
                                      (= c "-") (do (.setColor gr (if (and (> (elapsed g :pauseclock) 1) (= (@g :paused) :level))
                                                                    (if (= (@g :style) :pacman) Color/BLACK solidcolor)
                                                                    (if (= (@g :style) :jrpacman) solidcolor (:pinkypink game-colors))))
                                                    (.fillRect gr (+ x 2), (+ oy (round (/ half 4))) size, (round (/ half 2)))) ;pen
                                      (= c "►") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, oy size, half))
                                                    (.setColor gr mazecolor)
                                                    (.drawLine gr x, y halfright, y)
	                                                  (.drawLine gr x, oy halfright, oy)
                                                    (.drawLine gr halfright,y halfright,oy))
                                      (= c "◄") (do (when solid (.setColor gr solidcolor)
                                                                (.fillRect gr x, oy size, half))
                                                    (.setColor gr mazecolor)
                                                    (.drawLine gr x, y halfright, y)
	                                                  (.drawLine gr x, oy halfright, oy)
                                                    (.drawLine gr x,y x,oy))
                                      :default (.drawString gr c x y))))
               
               ;; debug grid
               (when (@g :telemetry)
                 (.setStroke gr (new BasicStroke 1))

		             (.setColor gr (:darkgray game-colors))
		             (.drawRect gr (- ox half), (- oy half) (@g :tilesize), (@g :tilesize)))

          
               ;; End of maze loop
           ))))
         
         ;; screen quadrants
         #_(when (@g :telemetry)
           (.setColor gr Color/GREEN)
	         (.drawRect gr (@g :midx), 0 1, (@g :h))
	         (.drawRect gr 0, (/ (@g :h) 2) (@g :w), 1))
               
         (let [size (@g :actorsize)
               half (@g :halfactor)
               halftile (@g :halftile)
               l (@g :level)
               mid (@g :midc)
               score1 (str (@g :score1))
               score2 (str (@g :score2))
               highscore (str (@g :highscore))]
           ;; draw scoreboard
           (.setColor gr (:white game-colors))
           ;; player 1 score
           (.drawString gr score1 (- (coltox g (- (- mid 7) (count score1))) halftile) (+ (rowtoy g 1) half))
					 (when (or (not (@g :started)) (every g 500))
             (.drawString gr "1UP" (- (coltox g (- mid 11)) halftile) (+ (rowtoy g 0) half)))
           ;; player 2 score
           (when (= (@g :players) 2)
             (when (every g 500) (.drawString gr "2UP" (- (coltox g (+ mid 8)) halftile) (+ (rowtoy g 0) half)))
             (.drawString gr score2 (- (coltox g (- (+ mid 12) (count score2))) halftile) (+ (rowtoy g 1) half)))
           ;; high score
           (.drawString gr "HIGH" (- (coltox g (- mid 5)) halftile) (+ (rowtoy g 0) half))
           (.drawString gr "SCORE" (- (coltox g mid) halftile) (+ (rowtoy g 0) half))
           (when (> (@g :highscore) 0)
	           (.drawString gr highscore (- (coltox g (- (+ mid 3) (count highscore))) halftile) (+ (rowtoy g 1) half)))
          
           ;; level
           (draw-levels g gr)

           ;; Draw players and objects on board
           (if (@g :started)
             (let [board-seconds (elapsed g :boardclock)
                   game-seconds (elapsed g :clock)
                   just-happened (<= (elapsed g :pauseclock) 1)
									 e125 (every g 125)
				           dl (@g :dotsleft)
                   level (nth levels (dec l))]

								 ;; draw "Ready!"
                 (when (< game-seconds 2)
                   (.setColor gr Color/CYAN)
									 (.drawString gr "PLAYER" (- (coltox g (- mid 5)) halftile) (+ (rowtoy g 14) halftile))
									 (.drawString gr "ONE" (- (coltox g (+ mid 2)) halftile) (+ (rowtoy g 14) halftile)))
                 (when (< board-seconds 3)
                   (.setColor gr Color/YELLOW)
									 (.drawString gr "READY!" (- (coltox g (- mid 3)) halftile) (+ (rowtoy g 20) halftile)))

                 ;; remaining lives
                 (.setColor gr Color/YELLOW)
                 (dotimes [n (dec (@g :lives))]
                  (let [small (* size 0.75)]
                   (.fillArc gr (+ (coltox g (+ n (- (@g :midc) 12))) (* n half)), (rowtoy g (- (@g :mazerows) 2)) small, small 195 330)))
                 
                 ;; draw Fruit
                 (when (and (or (= (@g :fruit) :none) (= (@g :fruit) :eaten)) (or (= dl 74) (= dl 174))) ; initiate fruit prize twice per level based on dots eaten
                   (swap! g assoc :fruitclock (now))
                   (swap! g assoc :fruit :uneaten))
                 (when (and (= (@g :fruit) :uneaten) (> (elapsed g :bonusclock) 11) (< (elapsed g :fruitclock) 11) (<= dl 174)) ; show uneaten fruit for 10 seconds
                   (.drawImage gr ((:fruit level) images) (- (@g :midx) half), (- (rowtoy g 20) half) size, size nil))
                 
                 ;; draw Bonus points for fruit eaten
                 (when (and (<= dl 174) (< (elapsed g :bonusclock) 2)) ; show points for 2 seconds
                   (.setColor gr (:pinkypink game-colors))
									 (.setFont gr (Font. "Consolas" Font/BOLD (round (/ size 2))))
									 (.drawString gr (str (:fruitpoints level)) (coltox g (dec (@g :midc))), (- (rowtoy g 21) half)))
                 
		             ;; draw frightened ghosts
                 (when (and (or just-happened (not= (@g :paused) :death))
                            (or just-happened (not= (@g :paused) :level)))
                  (doseq [a [clyde inky pinky blinky]]
				            (when (and (= (@a :mode) :frightened) (not= (@g :paused) (@a :eatpause))) (draw-ghost g gr a thick))))

                 (let [cg (chomping-ghosts? g)]
                   ;; draw Pac-Man
                   (when (not cg)
	                   (draw-pacman g gr pacman 1 thick))
                   (when (and (not cg) (= (@g :players) 2))
	                   (draw-pacman g gr mspacman 1 thick))

			             ;; draw normal ghosts
	                 (when (and (>= board-seconds 2) ; don't show ghosts until level is displayed for 2 seconds
	                            (or just-happened (not= (@g :paused) :death)) ; make ghosts disappear 1 second after Pac-Man dies
	                            (or just-happened (not= (@g :paused) :level))) ; make ghosts disappear 1 second after level complete
										 (doseq [a [clyde inky pinky blinky]]
											 (when (not= (@a :mode) :frightened) (draw-ghost g gr a thick))))
	
									 ;; show points for ghost eaten
	                 (when cg
                     (let [a ((@g :paused) {:eat-clyde clyde :eat-inky inky :eat-pinky pinky :eat-blinky blinky})] ; get the ghost just eaten
											 (.setColor gr Color/CYAN)
	                     (.setFont gr (Font. "Consolas" Font/BOLD (round (/ size 2))))
	                     (.drawString gr (str (@g :ghostpoints)) (- (round (@a :x)) half), (round (@a :y))))))
                 
                 ;; draw targets
                 (when (@g :telemetry)
                   (doseq [a [blinky pinky inky clyde]]
	                   (draw-target g gr a)))

                 ;; blank out tunnel area
                 (.setColor gr Color/BLACK)
                 (.fillRect gr (- (coltox g -5) halftile 2),0 (* (@g :tilesize) 5),(@g :h))
                 (.fillRect gr (+ (- (coltox g (@g :mazecolumns)) halftile) 3),0 (* (@g :tilesize) 5),(@g :h))
                 
                 ;; draw paused
                 (when (= (@g :paused) :paused)
                   (.setColor gr Color/GREEN)
                   (.drawString gr "PAUSED" (- (coltox g (- (@g :midc) 3)) halftile), (+ (rowtoy g 20) halftile)))
                 
                 ;; eyes returning to pen background noise
                 (let [ghosts-reincarnating? (some #(= (@% :mode) :reincarnate) [blinky pinky inky clyde])]
	                 (when (and ghosts-reincarnating? ; only play eyes when ghost(s) reincarnating
															(not= (@g :paused) :level) (not= (@g :paused) :death)
															(>= (elapsed g :sirenclock) (if (= (@g :style) :jrpacman) 0.361 0.265))) ; play eyes every 265ms
	                   (swap! g assoc :sirenclock (now))
			               (play-sound g :eyes))
	
		               ;; energizer background noise
		               (when (and (not ghosts-reincarnating?)
	                            (<= (elapsed g :blueclock) (:frighttime (nth levels (dec (@g :level))))) ; only play energizer during blue time
							                (>= (elapsed g :sirenclock) (if (= (@g :style) :pacman) 0.136 1.541))) ; play energizer every 136ms
		                 (swap! g assoc :sirenclock (now))
		                 (when (= (@g :paused) :none)
		                   (play-sound g :energizer))))

                 ;; siren background noise
                 (when (and (> (elapsed g :blueclock) (:frighttime (nth levels (dec (@g :level))))) ; only play siren if energizer worn off
						                (>= (elapsed g :sirenclock) (if (= (@g :style) :pacman) 0.435 0.394))) ; play siren every 435ms
                   (swap! g assoc :sirenclock (now))
                   (when (= (@g :paused) :none) 
                     (play-sound g :siren))))
             ;;else draw Game Over mode
             (doto gr
		           ;; marquis art 
		           (.drawImage (:monster-art images) 10,10 200,200 nil)
		           (.drawImage (:pac-art images) (- (@g :w) 210),10 200,200 nil)
             
	             (.setColor Color/RED)
							 (.drawString "GAME" (- (coltox g (- (@g :midc) 5)) halftile) (+ (rowtoy g 20) halftile))
							 (.drawString "OVER" (- (coltox g (+ (@g :midc) 1)) halftile) (+ (rowtoy g 20) halftile))
       
			         ;; credits
               (.setColor (:white game-colors))
							 (.drawString "CREDIT" (- (coltox g (- (@g :midc) 12)) halftile) (+ (rowtoy g (dec (@g :mazerows))) halftile))
							 (.drawString (format "% 2d" (@g :credits)) (- (coltox g (- (@g :midc) 5)) halftile) (+ (rowtoy g (dec (@g :mazerows))) halftile)))))))

         (. gr dispose) 
         (. bs show))))
   
    (mouseMoved [#^MouseEvent e]
      (let [#^JFrame me this] (.repaint me)))

    (mouseDragged [e])
    
    (keyPressed [#^KeyEvent e]
      (when (or (= (. e getKeyChar) \c)
                (= (. e getKeyChar) \5))
        (play-sound g :credit)
        (swap! g update-in [:credits] inc)
				(let [#^JFrame me this] 
		       (.repaint me)))
      (when (and (not (@g :started)) (> (@g :credits) 0) (= (. e getKeyChar) \1))
        (swap! g update-in [:credits] dec)
        (reset-game! g pacman mspacman blinky pinky inky clyde)
        (start-game! g 1))
      (when (and (not (@g :started)) (> (@g :credits) 1) (= (. e getKeyChar) \2))
        (swap! g update-in [:credits] - 2)
        (reset-game! g pacman mspacman blinky pinky inky clyde)
        (start-game! g 2))
      (when (= (. e getKeyCode) KeyEvent/VK_UP)
        (set-game-joystick g 1 :up)
        (set-pacman-direction? g pacman :up))
      (when (= (. e getKeyChar) \w)
        (set-game-joystick g 2 :up)
        (set-pacman-direction? g mspacman :up))
      (when (= (. e getKeyCode) KeyEvent/VK_DOWN)
        (set-game-joystick g 1 :down)
        (set-pacman-direction? g pacman :down))
      (when (= (. e getKeyChar) \s)
        (set-game-joystick g 2 :down)
        (set-pacman-direction? g mspacman :down))
      (when (= (. e getKeyCode) KeyEvent/VK_LEFT)
        (set-game-joystick g 1 :left)
        (set-pacman-direction? g pacman :left))
      (when (= (. e getKeyChar) \a)
        (set-game-joystick g 2 :left)
        (set-pacman-direction? g mspacman :left))
      (when (= (. e getKeyCode) KeyEvent/VK_RIGHT)
        (set-game-joystick g 1 :right)
        (set-pacman-direction? g pacman :right))
      (when (= (. e getKeyChar) \d)
        (set-game-joystick g 2 :right)
        (set-pacman-direction? g mspacman :right))
      (when (or (= (. e getKeyChar) \n)
                (= (. e getKeyCode) KeyEvent/VK_F1))
        (when (and (@g :started) (not= (@g :paused) :intro))
          (let [oldpause (@g :paused)]
	          (swap! g assoc :paused :level)
	          (if (= oldpause :intermission)
              (reset-actors g pacman mspacman blinky pinky inky clyde) ; skip intermission
		          (reset-board g pacman mspacman blinky pinky inky clyde))))) ; skip level
      (when (= (. e getKeyCode) KeyEvent/VK_F2)
        (when (and (@g :started) (not= (@g :paused) :intermission))
          (stop-game! g)
          (reset-game! g pacman mspacman blinky pinky inky clyde) ; reset Pac-Man machine
          (let [#^JFrame me this] 
		       (.repaint me))))
      (when (= (. e getKeyCode) KeyEvent/VK_F3)
				(let [robot (new Robot)
			        bufferedImage (.createScreenCapture robot (new Rectangle 0,0 (@g :w),(@g :h)))
							#^JFrame me this] 
		      (.repaint me)
					(javax.imageio.ImageIO/write bufferedImage "png" (clojure.java.io/file (str (get-current-directory) "screencapture.png")))))
      (when (= (. e getKeyChar) \l)
        (swap! g update-in [:antialias] not))
      (when (= (. e getKeyChar) \u)
        (swap! g update-in [:sound] not))
      (when (= (. e getKeyChar) \t)
        (swap! g update-in [:telemetry] not))
      (when (= (. e getKeyChar) \r)
        (swap! g update-in [:demo] not))
      (when (= (. e getKeyChar) \p)
	      (if (= (@g :paused) :none)
	        (swap! g assoc :paused :paused)
	        (when (= (@g :paused) :paused)
	          (swap! g assoc :paused :none))))
      (when (or (= (. e getKeyCode) KeyEvent/VK_ESCAPE)
								(= (. e getKeyChar) \q))
        (println "Player quit the game")
        (shutdown-agents)
        (System/exit 0)))

    (keyReleased [e])

    (keyTyped [e])

    (actionPerformed [e]
			(when (= (@g :paused) :none) ; normal game play-- all actors are moving
			 (let [players (if (= (@g :players) 1) [pacman] [pacman mspacman])]
     
				;; Autonomously move Pac-Man in demo mode
	      (when (@g :demo)
          (doseq [a players]
						(let [pr (ytorow g (@a :y))
			            pc (xtocol g (@a :x))]
		          (when (or (@a :parked) (not= (@a :lastrow) pr) (not= (@a :lastcol) pc))
				        (swap! a assoc :lastrow pr :lastcol pc) 
							  (swap! g assoc (@a :joystick) (let [rd (rand-int (if (@a :parked) 2 4))] (cond (= rd 0) (translate-direction a :left) (= rd 1) (translate-direction a :right) :default (translate-direction a :forward))))))))

        ;; move in direction of joystick
        (doseq [a players]
	        (when (not= (@a :d) (@g (@a :joystick)))
	          (set-pacman-direction? g a (@g (@a :joystick)))))

	      ;; check Pac-Man eaten by ghosts (redundant check to prevent collision detection bug!)
        (capture-pacman g pacman mspacman blinky pinky inky clyde)
     
        (doseq [a players]
	        (chomp-ghosts g a blinky pinky inky clyde) ; check ghosts eaten by Pac-Man (redundant check to prevent collision detection bug!)
          (update-pacman-position? g a) ; update Pac-Man position
          (eat-dots! g a blinky pinky inky clyde) ; eat dots
          (chomp-ghosts g a blinky pinky inky clyde)) ; check ghosts eaten by Pac-Man

        (when (not (chomping-ghosts? g))  
          ;; update ghost mode
          (doseq [a [blinky pinky inky clyde]]
             (set-ghost-mode g a (mode-interval g a (elapsed g :modeclock))))

        ;; update ghost positions
				(doseq [a [blinky pinky inky clyde]]
	         (update-ghost-position! g a pacman blinky)))))

		  ;; Pause of some kind is in effect
			(when (and (not= (@g :paused) :level) (not= (@g :paused) :death))
        (doseq [a [blinky pinky inky clyde]]
          (when (and (= (@a :mode) :reincarnate) (not (in-pen? g a))) ; allow reincarnating ghost eyes to fly back to pen
            (update-ghost-position! g a pacman blinky))))
	     
      ;; release pause after first few seconds of life
      (when (and (= (@g :paused) :start) (> (elapsed g :pauseclock) 5))
        (swap! g assoc :paused :none))
       
      ;; check for bonus life
      (when (and (= (@g :bonuslife) :unearned) (>= (@g :score1) 10000))
        (swap! g assoc :bonuslife :earned)
        (play-sound g :bonus)
        (swap! g update-in [:lives] inc))
       
      ;; check Pac-Man eaten by ghosts
      (when (not= (@g :paused) :level)
	      (capture-pacman g pacman mspacman blinky pinky inky clyde))

      ;; reincarnate eaten ghosts
      (when (>= (elapsed g :pauseclock) 1)
				(doseq [a [blinky pinky inky clyde]]
					(when (= (@g :paused) (@a :eatpause)) (swap! g assoc :paused :none) (set-ghost-mode g a :reincarnate))))

      ;; check ghost reincarnation
      (doseq [a [blinky pinky inky clyde]]
				(when (reincarnated? g a) (reset-ghost! g a)))
       
      ;; check game over
      (when (= (@g :lives) 0) (stop-game! g))

      ;; reset at end of level
      (when (and (= (@g :paused) :level) (>= (elapsed g :pauseclock) 3))  ; Pause for 3 seconds at end of level
        (reset-board g pacman mspacman blinky pinky inky clyde))
      
	    (let [#^JFrame me this] (.repaint me)))))

(defn -main
  "Initializes the primary game objects, sets the display mode, and kicks things off"
  []
  (let [tk (. Toolkit getDefaultToolkit)
        ge (GraphicsEnvironment/getLocalGraphicsEnvironment)
        gd (. ge getDefaultScreenDevice)
        height (.. tk getScreenSize height)
        width (.. tk getScreenSize width)
        mazerows (count (maze 1))
        mazecolumns (count (first (maze 1)))
        tilesize (round (/ height mazerows))
        halftile (round (/ tilesize 2))
        actorsize (round (* tilesize 1.6))
        halfactor (round (/ actorsize 2))
        midx (/ width 2)
        midc (round (/ mazecolumns 2))
        credits 0
        highscore 0
        thegame (new-game (maze 1) (dots 1) mazerows mazecolumns midx midc :left :right height width tilesize halftile actorsize halfactor credits highscore true (now))
        thepacman (new-pacman :pacman)
        mspacman (new-pacman :mspacman)
        blinky (new-ghost :shadow (:red game-colors) 0)
        pinky (new-ghost :speedy (:pinkypink game-colors) 7)
        inky (new-ghost :bashful (:cyan game-colors) 17)
        clyde (new-ghost :pokey (:clydeorange game-colors) 32)
        #^JFrame screen (gameworld-frame thegame thepacman mspacman blinky pinky inky clyde)]

    ;; Initialize graphics mode
    (swap! thegame assoc :timer (Timer. 20 screen))
    (when (not (. screen isDisplayable)) (. screen setUndecorated true))
    (.setVisible screen true)
    (. (.getContentPane screen) setBackground Color/BLACK)
    (. (.getContentPane screen) setIgnoreRepaint true)
    (doto screen
      (.setResizable false) 
      (.setBackground Color/BLACK) (.setIgnoreRepaint true)
      (.addMouseMotionListener screen) (.addKeyListener screen))
    (. gd setFullScreenWindow screen)
    (. screen createBufferStrategy 2) ; double buffer
    
    ;; Power up Pac-Man machine!
    (reset-game! thegame thepacman mspacman blinky pinky inky clyde)))

(-main)