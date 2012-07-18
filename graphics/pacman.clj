;;;; 
;;;; Pac-Man
;;;;

(ns arcade.pacman
  (:require clojure.java.io)
  (:import (java.awt Color Toolkit Font GraphicsEnvironment Graphics2D RenderingHints BasicStroke Cursor)
           (java.awt.image BufferStrategy BufferedImage)
           (java.awt.event ActionListener MouseMotionListener KeyListener
                           MouseEvent KeyEvent)
           (javax.imageio ImageIO)
           (javax.swing JFrame Timer)
           (java.applet Applet)
           (java.io File)
           (java.net URL)
           ;(javax.sound.sampled AudioSystem DataLine$Info SourceDataLine
           ;                   AudioFormat AudioFormat$Encoding)
           (java.util Date))
)

(def maxspeed 6.5) ;; 15 for slow machines, 4 for fast machines

(def mazes [[   ; X,x = in pen, ^ = forbidden up for ghosts, * = forbidden with dot, < = horizontal tunnel exits, # = slow areas
		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄▄▄╗"
		"▐············││············▌"
		"▐·┌──┐·┌───┐·││·┌───┐·┌──┐·▌"
		"▐●│  │·│   │·││·│   │·│  │●▌"
		"▐·└──┘·└───┘·└┘·└───┘·└──┘·▌"
		"▐··························▌"
		"▐·┌──┐·┌┐·┌──────┐·┌┐·┌──┐·▌"
		"▐·└──┘·││·└──┐┌──┘·││·└──┘·▌"
		"▐······││····││····││······▌"
		"╚▀▀▀▀┐·│└──┐ ││ ┌──┘│·┌▀▀▀▀╝"
		"     ▐·│┌──┘ └┘ └──┐│·▌     "
		"     ▐·││   ^  ^   ││·▌     "
		"     ▐·││ ┌▀▀--▀▀┐ ││·▌     "
		"▄▄▄▄▄┘·└┘ ▌xxXXxx▐ └┘·└▄▄▄▄▄"
		"<#####·   ▌XXXXXX▐   ·#####<"
		"▀▀▀▀▀┐·┌┐ ▌xxxxxx▐ ┌┐·┌▀▀▀▀▀"
		"     ▐·││ └▄▄▄▄▄▄┘ ││·▌     "
		"     ▐·││          ││·▌     "
		"     ▐·││ ┌──────┐ ││·▌     "
		"╔▄▄▄▄┘·└┘ └──┐┌──┘ └┘·└▄▄▄▄╗"
		"▐············││············▌"
		"▐·┌──┐·┌───┐·││·┌───┐·┌──┐·▌"
		"▐·└─┐│·└───┘·└┘·└───┘·│┌─┘·▌"
		"▐●··││······*  *······││··●▌"
		"╙─┐·││·┌┐·┌──────┐·┌┐·││·┌─╜"
		"╓─┘·└┘·││·└──┐┌──┘·││·└┘·└─╖"
		"▐······││····││····││······▌"
		"▐·┌────┘└──┐·││·┌──┘└────┐·▌"
		"▐·└────────┘·└┘·└────────┘·▌"
		"▐··························▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄╗"
		"▐······░▓··········░▓······▌"
		"▐●┌▲▲┐·░▓·┌▲▲▲▲▲▲┐·░▓·┌▲▲┐●▌"
		"▐·└▼▼┘·└┘·└▼▼▼▼▼▼┘·└┘·└▼▼┘·▌"
		"▐··························▌"
		"╚▀┐·┌┐·┌▲▲▲┐·┌┐·┌▲▲▲┐·┌┐·┌▀╝"
		"  ▐·░▓·░███▓·░▓·░███▓·░▓·▌  "
		"▄▄┘·░▓·└▼▼▼┘·░▓·└▼▼▼┘·░▓·└▄▄"
		"<##·░▓·······░▓·······░▓·##<"
		"▀▀┐·░❶▲▲┐ ┌▲▲❷❶▲▲┐ ┌▲▲❷▓·┌▀▀"
		"  ▐·└▼▼▼┘ └▼▼▼▼▼▼┘ └▼▼▼┘·▌  "
		"  ▐·                    ·▌  "
		"  ▐·┌▲▲▲┐ ┌▀▀--▀▀┐ ┌▲▲▲┐·▌  "
		"  ▐·░❸▼▼┘ ▌xxXXxx▐ └▼▼❹▓·▌  "
		"  ▐·░▓    ▌XXXXXX▐    ░▓·▌  "
		"  ▐·░▓ ┌┐ ▌xxxxxx▐ ┌┐ ░▓·▌  "
		"▄▄┘·└┘ ░▓ └▄▄▄▄▄▄┘ ░▓ └┘·└▄▄"
		"<##·   ░▓          ░▓   ·##<"
		"▀▀┐·┌▲▲❷❶▲▲┐ ┌┐ ┌▲▲❷❶▲▲┐·┌▀▀"
		"  ▐·└▼▼▼▼▼▼┘ ░▓ └▼▼▼▼▼▼┘·▌  "
		"  ▐·······   ░▓   ·······▌  "
		"  ▐·┌▲▲▲┐·┌▲▲❷❶▲▲┐·┌▲▲▲┐·▌  "
		"╔▄┘·└▼▼▼┘·└▼▼▼▼▼▼┘·└▼▼▼┘·└▄╗"
		"▐············  ············▌"
		"▐·┌▲▲┐·┌▲▲▲┐·┌┐·┌▲▲▲┐·┌▲▲┐·▌"
		"▐·░██▓·░❸▼▼┘·░▓·└▼▼❹▓·░██▓·▌"
		"▐·░██▓·░▓····░▓····░▓·░██▓·▌"
		"▐●░██▓·░▓·┌▲▲❷❶▲▲┐·░▓·░██▓●▌"
		"▐·└▼▼┘·└┘·└▼▼▼▼▼▼┘·└┘·└▼▼┘·▌"
		"▐··························▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄▄▄▄"
		"<######░▓··········░▓######<"
		"▲▲▲▲▲┐ ░▓·┌▲▲▲▲▲▲┐·░▓ ┌▲▲▲▲▲"
		"╓▼▼▼▼┘ └┘·└▼▼❹❸▼▼┘·└┘ └▼▼▼▼╖"
		"▐●···········░▓···········●▌"
		"▐·┌▲▲▲▲▲┐·┌┐·░▓·┌┐·┌▲▲▲▲▲┐·▌"
		"▐·░❸▼▼▼▼┘·░▓·░▓·░▓·└▼▼▼▼❹▓·▌"
		"▐·░▓······░▓·└┘·░▓······░▓·▌"
		"▐·░▓·┌▲▲┐ ░▓····░▓ ┌▲▲┐·░▓·▌"
		"▐·└┘·└▼❹▓ ░❶▲▲▲▲❷▓ ░❸▼┘·└┘·▌"
		"▐······░▓ └▼▼▼▼▼▼┘ ░▓······▌"
		"╙▲▲▲▲┐·░▓          ░▓·┌▲▲▲▲╜"
		"╓▼▼▼▼┘·░▓ ┌▀▀--▀▀┐ ░▓·└▼▼▼▼╖"
		"▐······└┘ ▌xxXXxx▐ └┘······▌"
		"▐·┌▲▲┐·   ▌XXXXXX▐   ·┌▲▲┐·▌"
		"▐·└▼❹▓·┌┐ ▌xxxxxx▐ ┌┐·░❸▼┘·▌"
		"▐···░▓·░▓ └▄▄▄▄▄▄┘ ░▓·░▓···▌"
		"╚▀┐·░▓·░▓          ░▓·░▓·┌▀╝"
		"  ▐·░▓·░❶▲┐ ┌▲▲┐ ┌▲❷▓·░▓·▌  "
		"  ▐·└┘·└▼▼┘ ░██▓ └▼▼┘·└┘·▌  "
		"  ▐·········░██▓·········▌  "
		"  ▐·┌▲▲▲▲▲┐·░██▓·┌▲▲▲▲▲┐·▌  "
		"▄▄┘·└▼▼❹❸▼┘·└▼▼┘·└▼❹❸▼▼┘·└▄▄"
		"<##····░▓···    ···░▓····##<"
		"▲▲┐·┌┐·░▓·┌▲▲▲▲▲▲┐·░▓·┌┐·┌▲▲"
		"╓▼┘·░▓·└┘·└▼▼❹❸▼▼┘·└┘·░▓·└▼╖"
		"▐●··░▓·······░▓·······░▓··●▌"
		"▐·┌▲❷▓·┌▲▲▲┐·░▓·┌▲▲▲┐·░❶▲┐·▌"
		"▐·└▼▼┘·└▼▼▼┘·└┘·└▼▼▼┘·└▼▼┘·▌"
		"▐··························▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄▄▄▄╕╒▄▄▄▄╕╒▄▄▄▄▄▄▄▄▄╗"
		"▐·········░▓····░▓·········▌"
		"▐·┌▲▲▲▲▲┐·░▓·┌┐·░▓·┌▲▲▲▲▲┐·▌"
		"▐●░❸▼▼▼▼┘·└┘·░▓·└┘·└▼▼▼▼❹▓●▌"
		"▐·░▓·········░▓·········░▓·▌"
		"▐·└┘·┌┐·┌▲▲┐·░▓·┌▲▲┐·┌┐·└┘·▌"
		"▐····░▓·░██▓·░▓·░██▓·░▓····▌"
		"╙▲▲┐·░▓·└▼▼┘·└┘·└▼▼┘·░▓·┌▲▲╜"
		"▼▼▼┘·░▓··············░▓·└▼▼▼"
		"<····░❶▲┐ ┌▲▲▲▲▲▲┐ ┌▲❷▓····<"
		"┐·┌┐ └▼▼┘ └▼▼▼▼▼▼┘ └▼▼┘ ┌┐·┌"
		"▐·░▓                    ░▓·▌"
		"▐·░❶▲┐ ┌┐ ┌▀▀--▀▀┐ ┌┐ ┌▲❷▓·▌"
		"▐·└▼▼┘ ░▓ ▌xxXXxx▐ ░▓ └▼▼┘·▌"
		"▐·     ░▓ ▌XXXXXX▐ ░▓     ·▌"
		"▐·┌┐ ┌▲❷▓ ▌xxxxxx▐ ░❶▲┐ ┌┐·▌"
		"▐·░▓ └▼▼┘ └▄▄▄▄▄▄┘ └▼▼┘ ░▓·▌"
		"▐·░▓                    ░▓·▌"
		"▐·░❶▲┐ ┌▲▲▲┐ ┌┐ ┌▲▲▲┐ ┌▲❷▓·▌"
		"▐·└▼▼┘ ░❸▼▼┘ ░▓ └▼▼❹▓ └▼▼┘·▌"
		"▐······░▓····░▓····░▓······▌"
		"╙▲┐·┌┐·░▓·┌▲▲❷❶▲▲┐·░▓·┌┐·┌▲╜"
		"╓▼┘·░▓·└┘·└▼▼▼▼▼▼┘·└┘·░▓·└▼╖"
		"▐●··░▓·······  ·······░▓··●▌"
		"▐·┌▲❷▓·┌▲▲▲┐·┌┐·┌▲▲▲┐·░❶▲┐·▌"
		"▐·└▼▼┘·░❸▼▼┘·░▓·└▼▼❹▓·└▼▼┘·▌"
		"▐······░▓····░▓····░▓······▌"
		"▐·┌▲▲┐·░▓·┌▲▲❷❶▲▲┐·░▓·┌▲▲┐·▌"
		"▐·└▼▼┘·░▓·└▼▼▼▼▼▼┘·░▓·└▼▼┘·▌"
		"▐······░▓··········░▓······▌"
		"╚▀▀▀▀▀▀╛╘▀▀▀▀▀▀▀▀▀▀╛╘▀▀▀▀▀▀╝"
		"                            "
		"                            "]

[		"                            "
		"                            "
		"                            "
		"╔▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄╗"
		"▐··························▌"
		"▐·┌┐·┌▲▲┐·┌▲▲▲▲▲▲┐·┌▲▲┐·┌┐·▌"
		"▐●░▓·░██▓·░❸▼▼▼▼❹▓·░██▓·░▓●▌"
		"▐·░▓·└▼▼┘·░▓····░▓·└▼▼┘·░▓·▌"
		"▐·░▓······░▓·┌┐·░▓······░▓·▌"
		"▐·░❶▲┐·┌┐·░▓·░▓·░▓·┌┐·┌▲❷▓·▌"
		"▐·└▼▼┘·░▓·└┘·░▓·└┘·░▓·└▼▼┘·▌"
		"▐······░▓····░▓····░▓······▌"
		"╚▀┐·┌▲▲❷❶▲▲┐ ░▓ ┌▲▲❷❶▲▲┐·┌▀╝"
		"  ▐·└▼▼❹❸▼▼┘ └┘ └▼▼❹❸▼▼┘·▌  "
		"  ▐····░▓   ^  ^   ░▓····▌  "
		"▄▄┘ ┌┐·░▓ ┌▀▀--▀▀┐ ░▓·┌┐ └▄▄"
		"<###░▓·└┘ ▌xxXXxx▐ └┘·░▓###<"
		"▲▲▲▲❷▓·   ▌XXXXXX▐   ·░❶▲▲▲▲"
		"▼▼▼▼❹▓·┌┐ ▌xxxxxx▐ ┌┐·░❸▼▼▼▼"
		"<###░▓·░▓ └▄▄▄▄▄▄┘ ░▓·░▓###<"
		"▀▀┐ └┘·░▓          ░▓·└┘ ┌▀▀"
		"  ▐····░❶▲▲┐ ┌┐ ┌▲▲❷▓····▌  "
		"  ▐·┌┐·└▼▼▼┘ ░▓ └▼▼▼┘·┌┐·▌  "
		"  ▐·░▓······ ░▓ ······░▓·▌  "
		"  ▐·░❶▲▲┐·┌┐ ░▓ ┌┐·┌▲▲❷▓·▌  "
		"╔▄┘·└▼▼▼┘·░▓ └┘ ░▓·└▼▼▼┘·└▄╗"
		"▐·········░▓    ░▓·········▌"
		"▐·┌▲▲┐·┌┐·░❶▲▲▲▲❷▓·┌┐·┌▲▲┐·▌"
		"▐·░❸▼┘·░▓·└▼▼▼▼▼▼┘·░▓·└▼❹▓·▌"
		"▐·░▓···░▓··········░▓···░▓·▌"
		"▐●░▓·┌▲❷❶▲▲┐·┌┐·┌▲▲❷❶▲┐·░▓●▌"
		"▐·└┘·└▼▼▼▼▼┘·░▓·└▼▼▼▼▼┘·└┘·▌"
		"▐············░▓············▌"
		"╚▀▀▀▀▀▀▀▀▀▀▀▀╛╘▀▀▀▀▀▀▀▀▀▀▀▀╝"
		"                            "
		"                            "]])


 ;; Images for fruits and artwork
(defn get-current-directory []
  (str (. (java.io.File. ".") getCanonicalPath) "/"))

(defn load-image [f]
	(javax.imageio.ImageIO/read (clojure.java.io/file (str (get-current-directory) f))))

(def pac-art (load-image "pacman.jpg"))
(def monster-art (load-image "monster.jpg"))
(def cherry (load-image "cherryhd.png"))
(def strawberry (load-image "strawberryhd.png"))
(def orange (load-image "orangehd.png"))
(def apple (load-image "applehd.png"))
(def greenapple (load-image "greenapplehd.png"))
(def lime (load-image "limehd.png"))
(def lemon (load-image "lemonhd.png"))
(def kiwi (load-image "kiwihd.png"))
(def donut (load-image "donuthd.png"))
(def coffee (load-image "coffeehd.png"))
(def galaxian (load-image "galaxianhd.png"))
(def bell (load-image "bellhd.png"))
(def fruitkey (load-image "keyhd.png"))
(def pretzel (load-image "pretzelhd.png"))
(def pear (load-image "pearhd.png"))
(def banana (load-image "bananahd.png"))

;; Custom colors
(def mazeblue (new Color 33 32 222))
(def ghostblue (new Color 33 32 222))
(def peach (new Color 255 186 148))
(def brightpink (new Color 255 184 255))
(def mspacpink (new Color 255 186 148))
(def mspacblue (new Color 66 186 222))
(def mspacbrown (new Color 222 150 66))
(def mspacindigo (new Color 33 32 222))
(def graydots (new Color 222 223 222))
(def clydeorange (new Color 255 186 66))
(def purple (new Color 110 0 222))
(def darkgray (new Color 31 31 31))
(def brown (new Color 123 65 27))
(def kelly (new Color 38 203 13))
(def woody (new Color 162 68 4))

(def levels [ ;;{:level  0, :maze  2, :mazecolor brightpink,       :solidcolor mspacblue,  :dotcolor peach,         :style :solid, :fruit coffee,     :fruitpoints  100, :fruitcolor Color/RED,     :pacspeed 1.00, :pacdotspeed 1.00, :ghostspeed 0.75, :ghosttunnelspeed 0.40, :elroy1dots  20 :elroy1speed 1.00 :elroy2dots 10 :elroy2speed 1.00 :frightpacspeed 1.00 :frightpacdotspeed 1.00 :frightghostspeed 0.50 :frighttime 15}
             {:level  1, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit cherry,     :fruitpoints  100, :fruitcolor Color/RED,     :pacspeed 0.80, :pacdotspeed 0.71, :ghostspeed 0.75, :ghosttunnelspeed 0.40, :elroy1dots  20 :elroy1speed 0.80 :elroy2dots 10 :elroy2speed 0.85 :frightpacspeed 0.90 :frightpacdotspeed 0.79 :frightghostspeed 0.50 :frighttime 6 :intermission nil}
             {:level  2, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit strawberry, :fruitpoints  300, :fruitcolor Color/MAGENTA, :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  30 :elroy1speed 0.90 :elroy2dots 15 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 5 :intermission nil}
             {:level  3, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit orange,     :fruitpoints  500, :fruitcolor Color/ORANGE,  :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 4 :intermission 1}
             {:level  4, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit orange,     :fruitpoints  500, :fruitcolor Color/ORANGE,  :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 3 :intermission nil}
             {:level  5, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit apple,      :fruitpoints  700, :fruitcolor Color/GREEN,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil}

             {:level  2, :maze  1, :mazecolor Color/RED,     :solidcolor mspacpink,    :dotcolor graydots,      :style :solid,  :fruit cherry,      :fruitpoints  100, :fruitcolor Color/MAGENTA, :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  30 :elroy1speed 0.90 :elroy2dots 15 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 5 :intermission nil}
             {:level  2, :maze  1, :mazecolor Color/RED,     :solidcolor mspacpink,    :dotcolor graydots,      :style :solid,  :fruit strawberry,  :fruitpoints  200, :fruitcolor Color/MAGENTA, :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  30 :elroy1speed 0.90 :elroy2dots 15 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 5 :intermission nil}
             {:level  3, :maze  2, :mazecolor Color/WHITE,   :solidcolor mspacblue,    :dotcolor Color/YELLOW,  :style :solid,  :fruit orange,      :fruitpoints  500, :fruitcolor Color/ORANGE,  :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 4 :intermission 1}
             {:level  3, :maze  2, :mazecolor Color/WHITE,   :solidcolor mspacblue,    :dotcolor Color/YELLOW,  :style :solid,  :fruit pretzel,     :fruitpoints  700, :fruitcolor Color/ORANGE,  :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 4 :intermission nil}
             {:level  3, :maze  2, :mazecolor Color/WHITE,   :solidcolor mspacblue,    :dotcolor Color/YELLOW,  :style :solid,  :fruit greenapple,  :fruitpoints  1000, :fruitcolor Color/ORANGE,  :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 4 :intermission nil}
             {:level  4, :maze  3, :mazecolor Color/WHITE,   :solidcolor mspacbrown,   :dotcolor Color/RED,     :style :solid,  :fruit pear,        :fruitpoints  2000, :fruitcolor Color/ORANGE,  :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 3 :intermission 2}
             {:level  4, :maze  3, :mazecolor Color/WHITE,   :solidcolor mspacbrown,   :dotcolor Color/RED,     :style :solid,  :fruit banana,      :fruitpoints  5000, :fruitcolor Color/ORANGE,  :pacspeed 0.90, :pacdotspeed 0.79, :ghostspeed 0.85, :ghosttunnelspeed 0.45, :elroy1dots  40 :elroy1speed 0.90 :elroy2dots 20 :elroy2speed 0.95 :frightpacspeed 0.95 :frightpacdotspeed 0.83 :frightghostspeed 0.55 :frighttime 3 :intermission nil}
             {:level  5, :maze  4, :mazecolor clydeorange,   :solidcolor mspacindigo,  :dotcolor graydots,      :style :solid,  :fruit banana,      :fruitpoints  5000, :fruitcolor Color/GREEN,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission 3}
             {:level  6, :maze  4, :mazecolor mazeblue,      :solidcolor Color/BLACK,  :dotcolor peach,         :style :solid,  :fruit lemon,       :fruitpoints  5000, :fruitcolor Color/GREEN,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission nil}
             {:level  7, :maze  4, :mazecolor purple,        :solidcolor Color/BLACK,  :dotcolor Color/GREEN,   :style :solid,  :fruit kiwi,        :fruitpoints  5000, :fruitcolor Color/GREEN,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission nil}
             {:level  8, :maze  1, :mazecolor brown,         :solidcolor Color/BLACK,  :dotcolor peach,         :style :solid,  :fruit coffee,      :fruitpoints  5000, :fruitcolor Color/GREEN,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission nil}
             {:level  9, :maze  1, :mazecolor kelly,         :solidcolor woody,        :dotcolor peach,         :style :solid,  :fruit donut,       :fruitpoints  5000, :fruitcolor Color/GREEN,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  40 :elroy1speed 1.00 :elroy2dots 20 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission nil}

             {:level  6, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit apple,      :fruitpoints  700, :fruitcolor Color/GREEN,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  50 :elroy1speed 1.00 :elroy2dots 25 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission 2}
             {:level  7, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit lime,       :fruitpoints 1000, :fruitcolor Color/WHITE,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  50 :elroy1speed 1.00 :elroy2dots 25 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil}
             {:level  8, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit lime,       :fruitpoints 1000, :fruitcolor Color/WHITE,   :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  50 :elroy1speed 1.00 :elroy2dots 25 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil}
             {:level  9, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit galaxian,   :fruitpoints 2000, :fruitcolor Color/CYAN,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  60 :elroy1speed 1.00 :elroy2dots 30 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil}
             {:level 10, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit galaxian,   :fruitpoints 2000, :fruitcolor Color/CYAN,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  60 :elroy1speed 1.00 :elroy2dots 30 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 5 :intermission 3}
             {:level 11, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit bell,       :fruitpoints 3000, :fruitcolor Color/YELLOW,  :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  60 :elroy1speed 1.00 :elroy2dots 30 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 2 :intermission nil}
             {:level 12, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit bell,       :fruitpoints 3000, :fruitcolor Color/YELLOW,  :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  80 :elroy1speed 1.00 :elroy2dots 40 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil}
             {:level 13, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  80 :elroy1speed 1.00 :elroy2dots 40 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil}
             {:level 14, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots  80 :elroy1speed 1.00 :elroy2dots 40 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 3 :intermission 3}
             {:level 15, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil}
             {:level 16, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission nil}
             {:level 17, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil}
             {:level 18, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 100 :elroy1speed 1.00 :elroy2dots 50 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 1 :intermission 3}
             {:level 19, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 120 :elroy1speed 1.00 :elroy2dots 60 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil}
             {:level 20, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 1.00, :pacdotspeed 0.87, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 120 :elroy1speed 1.00 :elroy2dots 60 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil}
             {:level 21, :maze  0, :mazecolor mazeblue,       :solidcolor Color/BLACK,  :dotcolor peach,         :style :hollow, :fruit fruitkey,   :fruitpoints 5000, :fruitcolor Color/BLUE,    :pacspeed 0.95, :pacdotspeed 0.79, :ghostspeed 0.95, :ghosttunnelspeed 0.50, :elroy1dots 120 :elroy1speed 1.00 :elroy2dots 60 :elroy2speed 1.05 :frightpacspeed 1.00 :frightpacdotspeed 0.87 :frightghostspeed 0.60 :frighttime 0 :intermission nil}
             ])

;; Basic math functions
(defn round [i] (int (+ i 1/2)))
(defn bround [bi] (bigint (+ bi 1/2)))
(defn lesser [x y] (if (< x y) x y))
(defn greater [x y] (if (> x y) x y))

;; Sound functions
(defn play-url [url-string]
  (.play (Applet/newAudioClip (URL. url-string))))

(defn play-file [file-name]
  (let [absolute-name (.getAbsolutePath (File. file-name))
        url-string (str "file://" absolute-name)]
    (play-url url-string)))

(defn dots [l]
  "Constructs a list of row,column vectors for the dots on the maze of the given level.
	The game keeps a copy of the list that is updated as dots are eaten."
  (for [[rowindex row] (map vector (iterate inc 0) (nth mazes (:maze (nth levels (dec l)))))
        [colindex ch] (map vector (iterate inc 0) row)
        :when (let [c (str ch)] (or (= c "·") (= c "*") (= c "●")))]
    [rowindex colindex]))

(defn maze [l]
  "Returns the maze for a given level"
  (nth mazes (:maze (nth levels (dec l)))))

(def mazerows (count (maze 1)))
(def mazecolumns (count (first (maze 1))))

;; Time functions
(defn now [] (. (new Date) getTime))

(defn every [g milliseconds]
  "Returns true every given milliseconds"
  (= (mod (bround (/ (- (@g :clock) (now)) milliseconds)) 2) 0))

(defn elapsed [g c]
  "Returns the elapsed seconds on the given clock"
  (/ (- (now) (@g c)) 1000))

(defn mode-interval [g a es]
  (let [level (nth levels (dec (@g :level)))]
	  (cond (and (<= (elapsed g :blueclock) (:frighttime level)) (= (@a :mode) :frightened)) :frightened
	        (= (@a :mode) :reincarnate) :reincarnate
	        (and (= (@g :level) 1)
               (or (< es 10)
			             (and (>= es 30) (< es 37))
			             (and (>= es 57) (< es 64))
			             (and (>= es 84) (< es 89)))) :scatter
	        (and (>= (@g :level) 1)
               (<= (@g :level) 4)
               (or (< es 10)
			             (and (>= es 30) (< es 37))
			             (and (>= es 57) (< es 62))
			             (and (>= es 1095) (< es 1096)))) :scatter
	        (and (>= (@g :level) 5)
               (or (< es 8)
			             (and (>= es 28) (< es 35))
			             (and (>= es 53) (< es 60))
			             (and (>= es 1097) (< es 1098)))) :scatter
	        :default :chase)))

(defn mode-string [m]
  "Debug purposes only"
  (cond (= m :scatter)     "Scatter"
        (= m :chase)       "Chase"
        (= m :frightened)  "Frightened"
        (= m :reincarnate) "Reincarnate"
        :default           "Unknown"))

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

(defn coltox [g colindex]
  "Convert column index to screen coordinate x.
  Returns center coordinate of tile."
  (let [size (@g :tilesize)
        half (round (/ size 2))
        offset (- (/ (@g :w) 2) (* size (round (/ mazecolumns 2))))]
    (+ (* colindex size) half offset)))

(defn rowtoy [g rowindex]
  "Convert row index to screen coordinate y.
  Returns center coordinate of tile."
  (let [size (@g :tilesize)
        half (round (/ size 2))]
    (+ (* rowindex size) half)))

(defn xtocol [g x]
  "Convert screen coordinate x to column index"
  (let [size (@g :tilesize)
        offset (- (/ (@g :w) 2) (* size (round (/ mazecolumns 2))))]
    (int (/ (- x offset) size))))

(defn ytorow [g y]
  "Convert screen coordinate y to row index"
  (let [size (@g :tilesize)]
    (int (/ y size))))

(defn opposite-direction [d]
  "Returns the opposite direction"
  (cond
    (= d :left) :right
    (= d :right) :left
    (= d :up) :down
    (= d :down) :up))

(defn opposite-direction? [d1 d2]
  "Returns whether the given directions are opposite"
    (= d1 (opposite-direction d2)))

(defn path-tile? [t]
  "Returns whether the given tile value is a navigable part of the maze"
  (some #(= t %) ["·" " " "X" "#" "●" "^" "*" "<" "-"]))

(defn mazetile [m r c]
  "Returns the contents of the maze tile at row, col"
  (if (and (>= r 0) (>= c 0) (< r mazerows) (< c (count (first m))))
    (str (nth (nth m r) c))
	  "~"))

(defn in-pen? [g a]
  "Returns whether actor is in pen"
  (let [r (ytorow g (@a :y))
        c (xtocol g (@a :x))
        t (mazetile (@g :maze) r c)]
	  (or (= t "X") (= t "x") (= t "-"))))

(defn entering-pen? [d r c]
  (and (= d :down) (= r 14) (or (= c 14) (= c 13)))) ; TODO: Fix hard-coded pen location

(defn forbidden? [m d r c]
  "Returns whether ghost is forbidden to turn this way"
  (let [t (mazetile m r c)]
    (and (= d :up) (or (= t "^") (= t "*")))))

;; Counters used for determining when to exit pen
(defn set-game-dots [g d] (swap! g assoc :dots d))
(defn set-game-pinkydots [g d] (swap! g assoc :pinkydots d))
(defn set-game-inkydots [g d] (swap! g assoc :inkydots d))
(defn set-game-clydedots [g d] (swap! g assoc :clydedots d))
(defn set-game-globaldots [g d] (swap! g assoc :globaldots d))

(defn eat-dot! [g r c]
  "Removes dot from board"
  (let [newdots (replace {[r c] []} (@g :dots))]
    ;; Keep track of dots eaten for pen exit
    (set-game-pinkydots g (dec (@g :pinkydots)))
    (set-game-inkydots g (dec (@g :inkydots)))
    (set-game-clydedots g (dec (@g :clydedots)))
    (set-game-globaldots g (inc (@g :globaldots)))
    ;; Replace gameboard dots
    (set-game-dots g newdots)))

(defn dots-left [g]
  (count (filter #(not= [] %) (@g :dots))))

(defn chomping-ghosts? [g]
	(some #(= (@g :paused) %) [:eat-blinky :eat-pinky :eat-inky :eat-clyde]))

(defn elroy-level [g]
  "Returns the Cruise Elroy speed for Blinky, based on board level and dots eaten.
  0 - Not active, 1 - Faster, 2 - Fastest"
  (let [d (dots-left g)
        level (nth levels (dec (@g :level)))]
    (cond (< d (:elroy1dots level)) 1
          (< d (:elroy2dots level)) 2
          :default 0)))

(defn translate-direction [a d]
  "Translates an actor's relative direction to a physical maze direction"
  (let [ad (@a :d)] ; actor direction
	  (cond (= d :forward) ad ; translated direction
	                 (= d :left) (cond (= ad :up) :left
	                                   (= ad :down) :right
	                                   (= ad :left) :down
	                                   (= ad :right) :up)
	                 (= d :right) (cond (= ad :up) :right
	                                    (= ad :down) :left
	                                    (= ad :left) :up
	                                    (= ad :right) :down))))

(defn my-nexttile [g a d]
  "Returns the contents of the next adjacent tile in the actor's given translated direction"
  (let [c (xtocol g (@a :x))
        r (ytorow g (@a :y))
        td (translate-direction a d)
        nc (+ c (xoffset td 1))
        nr (+ r (yoffset td 1))]
    (if (and (>= nc 0) (>= nr 0) (< nc mazecolumns) (< nr mazerows))
      (mazetile (@g :maze) nr nc)
      "~")))

(defn nexttile [g a d]
  "Returns the contents of the next adjacent tile in the actor's given direction"
  (let [c (xtocol g (@a :x))
        r (ytorow g (@a :y))
        nc (+ c (xoffset d 1))
        nr (+ r (yoffset d 1))]
    (if (and (>= nc 0) (>= nr 0) (< nc mazecolumns) (< nr mazerows))
      (mazetile (@g :maze) nr nc)
      "~")))

(defn center-of-tile-x [g x]
  "Returns the center x pixel of the tile at x"
  (coltox g (xtocol g x)))

(defn center-of-tile-y [g y]
  "Returns the center y pixel of the tile at y"
  (rowtoy g (ytorow g y)))

(defn center-of-tile? [g a]
  "Returns whether given actor is currently at the center of its tile"
  (let [d (@a :d)
        x (@a :x)
        y (@a :y)]
		(or (and (or (= d :up)   (= d :down))  (= y (center-of-tile-y g y)))
		    (and (or (= d :left) (= d :right)) (= x (center-of-tile-x g x))))))

(defn near-center-of-tile? [g a n]
  "Returns whether given actor is currently within n pixels of the center of its tile"
  (let [d (@a :d)
        x (@a :x)
        y (@a :y)
        cy (center-of-tile-y g y)
        cx (center-of-tile-x g x)]
		(or (and (or (= d :up)   (= d :down))  (and (< y (+ cy n))
	                                              (> y (- cy n))))
		    (and (or (= d :left) (= d :right)) (and (< x (+ cx n))
                                                (> x (- cx n)))))))

(defn my-openpath? [g a d]
  "Returns whether path open for actor's given translated direction"
  (let [n (my-nexttile g a d)
        r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
		(and (path-tile? n) ; Only defined tiles
	       (not (entering-pen? d r c))))) ; Do not let Pac-Man enter pen

(defn ghost-my-openpath? [g a d]
  "Returns whether path open for ghost's given translated direction"
  (let [n (my-nexttile g a d)
        r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
		(and (path-tile? n) ; Only defined tiles
         (or (= (@a :mode) :frightened) (= (@a :mode) :reincarnate) (not (forbidden? (@g :maze) d r c))) ; Not forbidden path for regular ghosts
	       (or (= (@a :mode) :reincarnate) (not (entering-pen? d r c)))))) ; Do not let ghosts re-enter pen

(defn openpath? [g a d]
  "Returns whether path open for actor's given direction"
  (let [n (nexttile g a d)
        r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
    (and (path-tile? n) ; Only defined tiles
         (or (= (@a :mode) :reincarnate) (not (entering-pen? d r c)))))) ; Do not re-enter pen

(defn ghost-openpath? [g a d]
  "Returns whether path open for actor's given direction"
  (let [n (nexttile g a d)
        r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
    (and (path-tile? n) ; Only defined tiles
         (or (= (@a :mode) :frightened) (= (@a :mode) :reincarnate) (not (forbidden? (@g :maze) d r c))) ; Not forbidden path for regular ghosts
         (or (= (@a :mode) :reincarnate) (not (entering-pen? d r c)))))) ; Do not re-enter pen

(defn set-actor-speed [a s] (swap! a assoc :s s))

(defn set-actor-color [a c] (swap! a assoc :c c))

(defn set-actor-position [a x y] (swap! a assoc :x x :y y))

(defn set-actor-direction [a d] (swap! a assoc :d d))

(defn center-pacman! [g a]
  "Centers actor within tile"
  (when (= (@g :paused) :none)
    (set-actor-position a (coltox g (xtocol g (@a :x))) (rowtoy g (ytorow g (@a :y))))))

(defn set-pacman-parked [a p] (swap! a assoc :parked p))

(defn park-pacman! [g a]
  "Parks Pac-Man and centers within tile."
  (center-pacman! g a)
  (set-pacman-parked a true))

(defn straighten? [g a d]
  "Centers actor within path"
	(let [x (@a :x)
			  y (@a :y)]
	  (when (and (= (@g :paused) :none) (not (in-pen? g a)))
		  (if (or (= d :left) (= d :right))
				(set-actor-position a x (rowtoy g (ytorow g y)))
	    ;else
	      (set-actor-position a (coltox g (xtocol g x)) y)))))

(defn set-ghost-direction? [g a d]
  "Tries to turn ghost in given direction if path open"
  (if (ghost-openpath? g a d)
    (do (set-actor-direction a d)
			  ;; Straighten out ghost relative to maze path
	      (straighten? g a d)
	      true)
    false))

(defn set-ghost-next-direction [a nd] (swap! a assoc :nd nd))

(defn set-pacman-direction? [g a d]
  "Tries to turn Pac-Man in given direction if path open and near center of tile"
  (let [r (ytorow g (@a :y))
        c (xtocol g (@a :x))]
	  (if (and (openpath? g a d) ; Path must be open to turn that way
			       (or (opposite-direction? d (@a :d)) (near-center-of-tile? g a (* (@a :s) 3)))) ; Must be near center of tile to turn, unless reversing
	    (do (set-actor-direction a d)
				  ;; Straighten out Pac-Man relative to maze path
		      (straighten? g a d)
				  true)
		  false)))

(defn remember-last-dot! [a r c] (swap! a assoc :dotr r :dotc c))

(defn remember-last-tile! [a r c] (swap! a assoc :lastrow r :lastcol c))

(defn reverse-ghost-direction? [g a]
   (set-ghost-direction? g a (opposite-direction (@a :d))))

(defn set-ghost-mode! [g a mode]
  "Valid modes are :scatter, :chase, :frightened, or :reincarnate"
  (let [current-mode (@a :mode)]
	  (when (or (and (= current-mode :scatter) (= mode :chase))
	            (and (= current-mode :chase) (= mode :scatter)))
	    (reverse-ghost-direction? g a)) ; reverse when switching between scatter/chase
	  (swap! a assoc :mode mode)))

(defn frighten-ghost! [g a]
  "Reverses ghost and puts into frightened mode"
  (when (not= (@a :mode) :reincarnate) ; leave reincarnating ghosts alone
    (reverse-ghost-direction? g a)
	  (set-ghost-mode! g a :frightened)))

(defn tile-distance [g a d x y]
  "Returns the distance in pixels to a tile at x,y from the tile in the ghost's given direction"
  (let [size (@g :tilesize)
        td (translate-direction a d)
        ax (coltox g (xtocol g (+ (@a :x) (xoffset td size)))) ; measure from center of tile
        ay (rowtoy g (ytorow g (+ (@a :y) (yoffset td size))))]
    (if (ghost-my-openpath? g a d)
      (Math/abs (Math/sqrt (+ (* (- ax x) (- ax x)) (* (- ay y) (- ay y))))) ; calculate hypotenuse
      999999))) ; else crazy distance for unviable tile

(defn distance-to-target [g a d p b]
  "Employs unique targeting modes relative to Pac-Man from the tile in the ghost's given direction"
  (let [size (@g :tilesize)
        td (translate-direction a d)
        px (coltox g (xtocol g (@p :x))) ; measure from center of tile
        py (rowtoy g (ytorow g (@p :y)))
        pd (@p :d)
        n (@a :nickname)
        m (@a :mode)
        tx (cond (= m :reincarnate) (coltox g 12) ; Return to pen
		             (and (= n :shadow) (= m :scatter) (= (elroy-level g) 0)) (coltox g 25) ; Go to home corner every so often
                 (and (= n :speedy) (= m :scatter)) (coltox g 2)
                 (and (= n :bashful) (= m :scatter)) (coltox g (dec mazecolumns))
                 (and (= n :pokey) (= m :scatter)) (coltox g 0)
		             (= n :speedy) (+ px (xoffset pd (* size 4))) ; Pinky targets tile 4 squares ahead of Pac-Man
                 (= n :bashful) (let [ahead2 (+ px (xoffset pd (* size 2)))] (- px (- (@b :x) ahead2))) ; Inky targets blinky's mirrored tile relative to 2 squares ahead of Pac-Man
                 (= n :pokey) (let [tiledist (/ (tile-distance g a d px py) size)]
                                 (if (< tiledist 8) (coltox g 0) px)) ; Clyde seeks his corner within radius of 8 tiles of Pac-Man
                 :default px)
        ty (cond (= m :reincarnate) (rowtoy g 17)
                 (and (= n :shadow) (= m :scatter) (= (elroy-level g) 0)) (rowtoy g 0)
                 (and (= n :speedy) (= m :scatter)) (rowtoy g 0)
                 (and (= n :bashful) (= m :scatter)) (rowtoy g 34)
                 (and (= n :pokey) (= m :scatter)) (rowtoy g 34)
                 (= n :speedy) (+ py (yoffset pd (* size 4)))
                 (= n :bashful) (let [ahead2 (+ py (yoffset pd (* size 2)))] (- py (- (@b :y) ahead2)))
                 (= n :pokey) (let [tiledist (/ (tile-distance g a d px py) size)]
                                 (if (< tiledist 8) (rowtoy g 34) py))
                 :default py)]
    (tile-distance g a d tx ty)))

(defn tunnel-exit-check [g a]
  "Checks for tunnel exit; moves actor to other end if needed"
  (let [x (@a :x)
        y (@a :y)
        r (ytorow g y)
        c (xtocol g x)
        d (@a :d)]
	  (when (= (mazetile (@g :maze) r c) "<")
	   (if (and (= d :left) (= (xtocol g x) 0))
	     (set-actor-position a (coltox g (dec mazecolumns)), y)
		   (when (and (= d :right) (= (xtocol g x) (dec mazecolumns)))
		     (set-actor-position a (coltox g 0), y))))))

(defn stuck-in-wall? [g a]
  "Determines if actor is stuck in wall"
  (not (path-tile? (mazetile (@g :maze) (ytorow g (@a :y)) (xtocol g (@a :x))))))

(defn random-direction [g a]
  "Returns a random, translated, open direction for the ghost: left, right, or forward.
  Used in frightened mode."
  (when (not (some #(ghost-openpath? g a (translate-direction a %)) [:left :right :forward]))
    (reverse-ghost-direction? g a))
  (let [r (rand-int 3)
        td (cond (= r 0) (translate-direction a :left)
				         (= r 1) (translate-direction a :right)
                 :default (@a :d))]
    (if (ghost-openpath? g a td) td (random-direction g a))))

(defn turn-ghost [g a p b]
  "Potentially turns ghost based on decision AI"
  (let [d (@a :d)
        x (@a :x)
        y (@a :y)
        r (ytorow g y)
        c (xtocol g x)]
    ;; Only turn if near center of path
    (when (near-center-of-tile? g a (* (@a :s) 2)) ; TODO: Improve this to real "look-ahead"
               ;(or (not= (@g :lastrow) r) (not= (@g :lastcol) c))) 
      ;(set-ghost-direction? g a (@a :nd))

    ;(when (or (not= (@g :lastrow) r) (not= (@g :lastcol) c))
      
      
	    ;; Make ghost decide which way to turn
      (let [ld (distance-to-target g a :left p b)
            rd (distance-to-target g a :right p b)
            fd (distance-to-target g a :forward p b)
            new-direction (cond (and (= d :up) (= (ytorow g y) 14) (or (= (xtocol g x) 13) (= (xtocol g x) 14))) (translate-direction a :left) ; always turn left when leaving pen
		                            (= (@a :mode) :frightened) (random-direction g a)
																(and (<= ld rd) (<= ld fd)) (translate-direction a :left) ; turn left
																(and (<= rd ld) (<= rd fd)) (translate-direction a :right) ; turn right
																(and (<= fd ld) (<= fd rd)) d ; keep going straight
																:default :up)]
        (when (not= d new-direction)
		      (remember-last-tile! a r c)
          (set-ghost-next-direction a new-direction)
          (set-ghost-direction? g a new-direction))))))
        ;))))

(defn ghost-dot-count [g a]
  "Returns the dot count for the given ghost.
  Used for determining pen exit."
  (let [n (@a :nickname)]
	  (cond (= n :speedy)  (@g :pinkydots)
	        (= n :bashful) (@g :inkydots)
	        (= n :pokey)   (@g :clydedots)
          :default 0)))

(defn update-ghost-position! [g a p b]
  "Either turns ghost or moves ghost in current direction and speed."
  ;; Evaluate forced turns or potential turns at intersection
  (when (and (= (@a :nd) "") ; only turn at next tile
						 (or (not (ghost-my-openpath? g a :forward))
								 (and (not (in-pen? g a)) (or (ghost-my-openpath? g a :left) (ghost-my-openpath? g a :right)))))
    (turn-ghost g a p b))

  ;; Move ghost
  (let [c (xtocol g (@a :x))
        r (ytorow g (@a :y))
        level (nth levels (dec (@g :level)))
        t (mazetile (@g :maze) r c)
        mid (round (/ (@g :w) 2))
        gs (cond (= (@a :mode) :reincarnate) (* maxspeed 1.5) ; speed up in reincarnate mode
								 (= (@a :mode) :frightened) (* maxspeed (:frightghostspeed level)) ; 50% slow down in frightened mode
                 (some #(= t %) ["#" "<" "X" "x" "-"]) (* maxspeed (:ghosttunnelspeed level))  ; 40% slow down in tunnels and pen
                 (and (= (@a :nickname) :shadow) (= (elroy-level g) 1)) (* maxspeed (:elroy1speed level)) ; speed-up in Cruise Elroy 1 mode
                 (and (= (@a :nickname) :shadow) (= (elroy-level g) 2)) (* maxspeed (:elroy2speed level)) ; speed-up in Cruise Elroy 2 mode
                 :default (@a :s))]
	  
    (when (or (> (@g :globaldots) 7) (<= (ghost-dot-count g a) 0)) ; Don't leave pen until Pac-Man has eaten enough dots
	    (set-actor-position a
												  (+ (@a :x) (actor-xoffset a gs))
												  (+ (@a :y) (actor-yoffset a gs)))
      ;(set-game-globaldots g 0)

      ;; Clear next direction if ghost landed in a new tile
	    (when (or (not= (ytorow g (@a :y)) r) (not= (xtocol g (@a :x)) c))
	      (set-ghost-next-direction a ""))

		  ;; Check for tunnel exit  
		  (tunnel-exit-check g a))

  ;; If in pen, get out
  (when (and (not= (@a :mode) :reincarnate) (in-pen? g a) (> (@a :x) (- mid (@a :s))) (< (@a :x) (+ mid (@a :s))))
    (set-actor-direction a :up))))

(defn update-pacman-position? [g a]
  "Move Pac-Man in current direction and speed, if possible"
  (let [x (@a :x)
        y (@a :y)
        r (ytorow g y)
        c (xtocol g x)
        dr (@a :dotr) ; last eaten dot location
        dc (@a :dotc)
        d (@a :d)
        bs (elapsed g :blueclock)
        level (nth levels (dec (@g :level)))
        s (cond (and (= r dr) (= c dc)) (* maxspeed (:pacdotspeed level))  ; slow down when eating dots
                (and (= r dr) (= c dc) (< bs 6)) (* maxspeed (:frightpacdotspeed level)) ; slow down less when eating dots during blue time
                (< bs 6) (* maxspeed (:frightpacspeed level))  ; speed up during blue time
                :default (@a :s))]  ; normal speed
	   (if (or (my-openpath? g a :forward) (not (near-center-of-tile? g a 3))) ; Only stop if near center of tile
       (do
         ;; Move forward
		     (set-actor-position a
														 (+ x (actor-xoffset a s))
														 (+ y (actor-yoffset a s)))
         ;; Wall check
         (when (stuck-in-wall? g a)
           (while (stuck-in-wall? g a) ; Back up if embedded in wall
             (set-actor-position a
                                 (- (@a :x) (actor-xoffset a 1))
                                 (- (@a :y) (actor-yoffset a 1))))
			     (park-pacman! g a))
         ;; Tunnel check
	       (tunnel-exit-check g a)
	       (set-pacman-parked a nil)
				 true)
       (park-pacman! g a))))

(defn collide? [g pacman a]
  "Determines if Pac-Man has collided with ghost"
  (and (= (xtocol g (@pacman :x)) (xtocol g (@a :x))) (= (ytorow g (@pacman :y)) (ytorow g (@a :y)))))

(defn eaten? [g pacman a]
  "Determines if Pac-Man has been eaten"
  (and (not= (@a :mode) :frightened) (not= (@a :mode) :reincarnate) (collide? g pacman a)))

(defn eat-ghost? [g pacman a]
  "Determines if Pac-Man has eaten a ghost"
  (and (= (@a :mode) :frightened) (collide? g pacman a)))

(defn reincarnated? [g a]
  "Determines if ghost achieved reincarnation (i.e. eyes returned to pen)"
  (and (= (@a :mode) :reincarnate) (= (xtocol g (@a :x)) 12) (= (ytorow g (@a :y)) 17)))

(defstruct ghost :nickname :c :x :y :lastrow :lastcol :d :nd :s :mode)
(defn new-ghost [& [n c x y lastrow lastcol d nd s mode]] (atom (struct ghost n c x y lastrow lastcol d nd s mode)))

(defstruct pacman :x :y :d :s :dotr :dotc :parked)
(defn new-pacman [& [x y d s dotr dotc parked]] (atom (struct pacman x y d s dotr dotc parked)))

(defstruct game :maze :dots :joystick :h :w :tilesize :actorsize :credits :highscore :clock :boardclock :modeclock :blueclock :pauseclock :paused :fruitclock :fruit :bonusclock :timer :level :ghostpoints :score :lives :bonuslife :pinkydots :inkydots :clydedots :globaldots :antialias :started)
(defn new-game [& [maze dots joystick h w tilesize actorsize credits highscore clock boardclock modeclock blueclock pauseclock paused fruitclock fruit bonusclock timer level ghostpoints score lives bonuslife pinkydots inkydots clydedots globaldots antialias started]] 
  (atom (struct game maze dots joystick h w tilesize actorsize credits highscore clock boardclock modeclock blueclock pauseclock paused fruitclock fruit bonusclock timer level ghostpoints score lives bonuslife pinkydots inkydots clydedots globaldots antialias started)))

(defn set-game-maze [g m] (swap! g assoc :maze m))
(defn set-game-joystick [g j] (swap! g assoc :joystick j))
(defn set-game-size [g h w] (swap! g assoc :h h :w w))
(defn set-game-timer [g t] (swap! g assoc :timer t))
(defn set-game-clock [g c] (swap! g assoc :clock c))
(defn set-game-boardclock [g bc] (swap! g assoc :boardclock bc))
(defn set-game-modeclock [g mc] (swap! g assoc :modeclock mc))
(defn set-game-blueclock [g bc] (swap! g assoc :blueclock bc))
(defn set-game-pauseclock [g pc] (swap! g assoc :pauseclock pc))
(defn set-game-paused [g p] (swap! g assoc :paused p))
(defn set-game-fruitclock [g fc] (swap! g assoc :fruitclock fc))
(defn set-game-bonusclock [g bc] (swap! g assoc :bonusclock bc))
(defn set-game-fruit [g f] (swap! g assoc :fruit f))
(defn set-game-level [g l] (swap! g assoc :level l))
(defn set-game-ghostpoints [g p] (swap! g assoc :ghostpoints p))
(defn set-game-highscore [g h] (swap! g assoc :highscore h))
(defn set-game-score [g s] (swap! g assoc :score s) (when (> (@g :score) (@g :highscore)) (set-game-highscore g (@g :score))))
(defn set-game-lives [g l] (swap! g assoc :lives l))
(defn set-game-bonuslife [g b] (swap! g assoc :bonuslife b))
(defn set-game-antialias [g a] (swap! g assoc :antialias a))
(defn set-credits [g c] (swap! g assoc :credits c))

(defn initialize-all-clocks [g]
	"Initializes the various timers used in the game"
  (let [ct (now)]
		(set-game-clock g ct)
		(set-game-boardclock g ct)
		(set-game-modeclock g ct)
		(set-game-blueclock g ct)
		(set-game-pauseclock g ct)
		(set-game-bonusclock g ct)
		(set-game-fruitclock g ct)))

(defn stop-game [g]
  (swap! g assoc :started false) (let [#^Timer t (@g :timer)] (.stop t)))

(defn start-game [g]
  (initialize-all-clocks g)
  ;(play-file "intro.wav")
  (swap! g assoc :started true) (let [#^Timer t (@g :timer)] (.start t)))

(defn reset-ghost! [g a]
  (set-actor-position a (/ (@g :w) 2), (rowtoy g 17))
  (set-actor-direction a :up)
  (set-ghost-mode! g a :scatter))

(defn reset-dot-counters [g]
  "Sets pen exit times for ghosts based on level"
	(set-game-globaldots g -999999)
	(set-game-pinkydots g 0)
	(set-game-inkydots g (if (= (@g :level) 1) 30 0))
	(set-game-clydedots g (cond (= (@g :level) 1) 60
	                            (= (@g :level) 2) 50
	                            :default 0)))

(defn reset-actors [g pacman blinky pinky inky clyde]
  "Resets actors to default positions, speeds, and direction.
  Also resets the game clock and sets pause mode to start."
  (let [half (round (/ (@g :tilesize) 2))
        py (rowtoy g 17)
        level (nth levels (dec (@g :level)))]
    (set-actor-position pacman (+ (coltox g 13) half), (rowtoy g 26)) ;TODO Make initialize-actor-position that takes r,c ?
	  (set-actor-speed pacman (* maxspeed (:pacspeed level)))
	  (set-actor-direction pacman :left)
	  
    (set-actor-position blinky (/ (@g :w) 2), (rowtoy g 14))
	  (set-actor-position pinky  (/ (@g :w) 2), py)
	  (set-actor-position inky   (+ (coltox g 11) half), py)
	  (set-actor-position clyde  (- (coltox g 16) half), py)
	  
    (set-actor-speed blinky (* maxspeed (:ghostspeed level)))
	  (set-actor-speed pinky  (* maxspeed (:ghostspeed level)))
	  (set-actor-speed inky   (* maxspeed (:ghostspeed level)))
	  (set-actor-speed clyde  (* maxspeed (:ghostspeed level)))
	  
    (set-actor-direction blinky :up) (set-ghost-next-direction blinky :up)
	  (set-actor-direction pinky  :up) (set-ghost-next-direction pinky :up)
	  (set-actor-direction inky   :up) (set-ghost-next-direction inky :up)
	  (set-actor-direction clyde  :up) (set-ghost-next-direction clyde :up)
	  
    (set-ghost-mode! g blinky :scatter)
    (set-ghost-mode! g pinky  :scatter)
    (set-ghost-mode! g inky   :scatter)
    (set-ghost-mode! g clyde  :scatter))

    (set-ghost-next-direction blinky "")
    ;(set-ghost-next-direction pinky "")
    ;(set-ghost-next-direction inky "")
    ;(set-ghost-next-direction clyde ""))
  
    (set-game-joystick g :left)
  	(set-game-fruit g :none) ; clear any uneaten fruit from board
    (when (not= (@g :paused) :intermission)
	    (set-game-paused g :start)
	    (set-game-pauseclock g (now)))
    (set-game-modeclock g (now))
    (set-game-boardclock g (now)))

(defn reset-life [g pacman blinky pinky inky clyde]
  "Called each time Pac-Man starts a new life (including first)"
  (reset-actors g pacman blinky pinky inky clyde)
  (reset-dot-counters g) ; temporary
  (set-game-globaldots g 0)
	(set-game-lives g (dec (@g :lives))))

(defn reset-board [g pacman blinky pinky inky clyde]
  "Should be called at the start of each new level (except first) to initialize"
  (let [maxlevel (count levels)
        inclevel (inc (@g :level))
        newlevel (if (> inclevel maxlevel) maxlevel inclevel)] ; make last level repeat forever

    (set-game-level g newlevel)
    (when (:intermission (nth levels (dec (@g :level))))
      (set-game-paused g :intermission)
      (set-game-pauseclock g (now)))
    (reset-dot-counters g)
	  (reset-actors g pacman blinky pinky inky clyde)
	  (set-game-maze g (maze (@g :level)))
	  (set-game-dots g (dots (@g :level)))))

(defn reset-game [g pacman blinky pinky inky clyde]
  "Called once at the start of a game"
	(set-game-level g 1)
  (set-game-lives g 4)
  (set-game-bonuslife g :unearned)
	(reset-life g pacman blinky pinky inky clyde)
  (reset-dot-counters g)

  (set-game-antialias g true)
  (set-game-maze g (maze (@g :level)))
  (set-game-dots g (dots (@g :level)))
  (set-game-score g 0))

(defn draw-pacman [g gr pacman size half thick]
  (let [x (- (@pacman :x) half)
	      y (- (@pacman :y) half)
        d (@pacman :d)
			  e125 (every g 125)
        parked (@pacman :parked)
        board-seconds (elapsed g :boardclock)
	      mouth-angle (cond (and parked (= d :right)) 15
	                        (and parked (= d :up)) 105
	                        (= d :right) (if (and (not parked) e125) 15  45)  ; 15
	                        (= d :up)    (if (and (not parked) e125) 105 135)  ;105
	                        (= d :left)  (if (and (not parked) e125) 195 225)  ;195
	                        (= d :down)  (if (and (not parked) e125) 285 315)) ;285)
	      ;; make mouth chomp every 1/16 second if Pac-Man is moving
	      mouth-wide (cond (or (and (not= (@g :paused) :intermission) (< board-seconds 3)) ; wait a few seconds at the start of each life
	                           (= (@g :paused) :death)
	                           (= (@g :paused) :level)
	                           (and (not parked)
	                                (every g 62))) 360
	                       (or (and (not parked) e125)
	                           (and parked (or (= d :right) (= d :up)))) 330
	                       :default 270)]
	  (.setColor gr Color/YELLOW)
		(cond (and (= (@g :paused) :death) (>= (elapsed g :pauseclock) 3))
		        (do (.setStroke gr thick)
		            (dotimes [n 8]
		              (.fillArc gr x, y size, size (- (* n 45) 5), 10))
		            (.setColor gr Color/BLACK)
		            (.fillOval gr (+ x (/ half 2)), (+ y (/ half 2)), half half)) ;draw "poof" effect
		      (and (= (@g :paused) :death) (>= (elapsed g :pauseclock) 1))
		        (let [n (round (/ (- (now) (@g :pauseclock)) 16))]
		          (.fillArc gr x, y size, size (+ 90 (* n 0.9)), (- 360 (* n 1.8)))) ; draw "withering" effect
		        
		      :default (.fillArc gr x, y size, size mouth-angle, mouth-wide)))) ; draw normal chomping mouth

(defn draw-ghost [g gr ghost size half thick]
  "Draws ghost. Eye locations should be at 1/3 and 2/3 across"
  (let [x (- (@ghost :x) half)
        y (- (@ghost :y) half)
        level (nth levels (dec (@g :level)))
        flashtime (if (< (:frighttime level) 3) (:frighttime level) (round (/ (:frighttime level) 3)))
        color (cond (and (> (elapsed g :blueclock) (- (:frighttime level) flashtime)) (= (@ghost :mode) :frightened)) (if (every g 250) Color/WHITE ghostblue)
		                (= (@ghost :mode) :frightened) ghostblue
                    :default (@ghost :c))
        eyecolor (cond (and (> (elapsed g :blueclock) (- (:frighttime level) flashtime)) (= (@ghost :mode) :frightened)) (if (every g 250) Color/RED peach)
                    (= (@ghost :mode) :frightened) peach
                   :default Color/WHITE)
        thirdsize (round (* size 1/3))
        eye (round (/ size 3.3)) ;3.3
        halfeye (round (/ eye 2))
        ;ball (round (/ size 10))
        ball (/ (@g :tilesize) 4.0) ; same as dot size 4.0, or 4.5?
        halfball (/ ball 2)
        ballxo (actor-xoffset ghost halfball)
        ballyo (actor-yoffset ghost halfball)
        gown (round (/ size 7)) ; gown "length"
        gy (dec (- (+ y size) gown)) ; gown top coordinate
        offset (if (every g 125) (round (/ gown 5)) (- (round (/ gown 5))))]
		(when (and (not (and (= (@g :paused) :death) (> (elapsed g :pauseclock) 1))) (not= (@ghost :mode) :reincarnate))
			 ;; body
			 (.setStroke gr (new BasicStroke 1))
			 (.setColor gr color)
			 (.fillRect gr x, (dec (+ y half)) size, (- half gown))  ;size, half) ; no gown
	     (.fillArc gr x, y size, size 0, 180)
	     ;; gown effect
       (let [halfgown (round (/ gown 2))
             fourthsize (round (* size 4/9))]
	       (dotimes [n gown]
          (let [factor (* n 0.75)
                gw (- gown factor)
                gxo (+ x (/ factor 2))
                gyn (+ gy n)]
				     ;(.fillRect gr x,                                             (+ gy n) gw, 1) ; leftmost part
		         ;(.fillRect gr (- (+ gxo thirdsize) halfgown offset 1),       (+ gy n) gw, 1) ; left moving part
				     ;(.fillRect gr (- (+ x offset thirdsize thirdsize) halfgown), (+ gy n) gw, 1) ; right moving part
				     ;(.fillRect gr (- (+ gxo size) gown),                         (+ gy n) gw, 1))))) ; rightmost part
				     (.drawLine gr x,                                             gyn (+ gw x), gyn) ; leftmost part
		         (.drawLine gr (- (+ gxo thirdsize) halfgown offset 1),       gyn (+ gw (- (+ gxo thirdsize) halfgown offset 1)), gyn) ; left moving part
				     (.drawLine gr (- (+ x offset thirdsize thirdsize) halfgown), gyn (+ gw (- (+ x offset thirdsize thirdsize) halfgown)), gyn) ; right moving part
				     (.drawLine gr (- (+ gxo size) gown),                         gyn (- (+ x size) 1), gyn))))) ; rightmost part
		 (when (not (and (= (@g :paused) :death) (> (elapsed g :pauseclock) 1)))
	     ;; eyes
			 (.setColor gr eyecolor)
       (let [topeye (round (/ size 5))]
				 (if (not= (@ghost :mode) :frightened)
			     (do (.fillOval gr (- (+ x ballxo thirdsize) halfeye),           (+ y ballyo topeye) eye, eye)
							 (.fillOval gr (- (+ x ballxo thirdsize thirdsize) halfeye), (+ y ballyo topeye) eye, eye)
							 ;; eyeballs
							 (.setColor gr ghostblue)
					     ;(.fillRect gr (- (+ x thirdsize (* ballxo 2)) halfball),           (- (+ y ballyo halfeye topeye (* ballyo 1.5)) halfball) ball, ball)
							 ;(.fillRect gr (- (+ x thirdsize thirdsize (* ballxo 2)) halfball), (- (+ y ballyo halfeye topeye (* ballyo 1.5)) halfball) ball, ball))
					     (.fillOval gr (- (+ x thirdsize (* ballxo 2)) halfball),           (- (+ y ballyo halfeye topeye (* ballyo 1.5)) halfball) ball, ball)
							 (.fillOval gr (- (+ x thirdsize thirdsize (* ballxo 2)) halfball), (- (+ y ballyo halfeye topeye (* ballyo 1.5)) halfball) ball, ball))
			     ;; frightened ghost
	         (do (.fillRect gr (- (+ x 1 thirdsize) halfball),           (+ y (round (/ size 4))) ball, ball)
							 (.fillRect gr (- (+ x 1 thirdsize thirdsize) halfball), (+ y (round (/ size 4))) ball, ball)
		           ;; frown
		           (let [fx (+ x (round (/ size 8.8)))
		                 fy (+ y (* half 1.25))
		                 fsx (* size 0.28)
		                 fsy (* size 0.28)]
				         (.setStroke gr thick)
		             (.drawArc gr fx, fy fsx, fsy 30 120)
				         (.drawArc gr (+ fx fsx), fy fsx, fsy 30 120)
				         (.drawArc gr (+ fx fsx fsx), fy fsx, fsy 30 120))))))))

(defn chomp-ghost [g pacman ghost eat-ghost]
  "Checks to see if Pac-Man ate a ghost"
	(when (and (not (chomping-ghosts? g)) (not= (@g :paused) eat-ghost) (eat-ghost? g pacman ghost))
   (set-game-paused g eat-ghost)
   (set-game-ghostpoints g (* (@g :ghostpoints) 2))
   (set-game-score g (+ (@g :ghostpoints) (@g :score)))
   (set-game-blueclock g (+ (@g :blueclock) 1000))
   (when (= (@g :fruit) :uneaten)
	   (set-game-fruitclock g (+ (@g :fruitclock) 1000)))
   (set-game-pauseclock g (now))))

(defn chomp-ghosts [g pacman blinky pinky inky clyde]
  "Checks to see if Pac-Man ate any ghosts"
  (when (not= (@g :paused) :death) ;an eaten Pac-Man consumes no ghosts
	  (chomp-ghost g pacman blinky :eat-blinky)
	  (chomp-ghost g pacman pinky :eat-pinky)
	  (chomp-ghost g pacman inky :eat-inky)
	  (chomp-ghost g pacman clyde :eat-clyde)))

(defn capture-pacman [g pacman blinky pinky inky clyde]
  "Checks to see if a ghost ate Pac-Man"
	(when (and (not= (@g :paused) :death) ;not dead already
	           (some #(eaten? g pacman %) [blinky pinky inky clyde])) ;one of the ghosts ate him
	   (set-game-paused g :death)
	   (set-game-pauseclock g (now)))
	(when (and (= (@g :paused) :death) (>= (elapsed g :pauseclock) 4))
	   (reset-life g pacman blinky pinky inky clyde)))

(defn draw-levels [g gr]
  (let [size (@g :actorsize)
        half (round (/ size 2))
        l (@g :level)]
		(dotimes [n 7]
		  (let [fruitlevel (- l (- 7 n)) ; show most recent 7 levels
		        fruitoffset (if (< l 7) (* (- 7 l) (+ (@g :tilesize) half)) 0)]
		  (when (>= fruitlevel 0)
		    (.drawImage gr (:fruit (nth levels fruitlevel)) (+ fruitoffset (- (coltox g (- 24 n)) (* n half))),  (- (rowtoy g (- mazerows 2)) (/ (@g :tilesize) 4)) size, size nil))))))
           
(defn gameworld-frame [g pacman blinky pinky inky clyde]
  (proxy [JFrame ActionListener MouseMotionListener KeyListener] []
    (paint [grf]
     (let [#^JFrame me this
           #^BufferStrategy bs (.getBufferStrategy me)
           #^Graphics2D gr (if (not= nil bs) (. bs getDrawGraphics) nil)
           thick (new BasicStroke (round (greater 3 (/ (@g :tilesize) 10))))] ; Lines should be at least 3 pixels thick on low-res displays
       (when (not= nil gr)
         ;; clear screen
         (.setColor gr Color/BLACK)
         (.fillRect gr 0 0 (@g :w) (@g :h))
         (.setFont gr (Font. "Consolas" Font/BOLD (+ (@g :actorsize) 9)))

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
															 (set-pacman-parked pacman nil)
															 (draw-pacman g gr pacman (@g :actorsize) (/ (@g :actorsize) 2) thick)
															
															 (set-actor-position blinky (+ (- (@g :w) (* (/ (@g :w) 5) es)) (* (@g :actorsize) 2.2))
															                            (+ (/ (@g :h) 2) (@g :actorsize)))
															 (set-actor-direction blinky :left)
															 (draw-ghost g gr blinky (@g :actorsize) (/ (@g :actorsize) 2) thick)
															
															 (draw-levels g gr))
									(and (>= es 5) (< es 10)) (do 
	                             ;; Intermission I, part 2 - "Super" Pac-Man chases Blinky from left to right
	
															 (set-actor-position pacman (* (/ (@g :w) 5) (- es 5))
															                            (+ (/ (@g :h) 2) (@g :tilesize)))
															 (set-actor-direction pacman :right)
															 (set-pacman-parked pacman nil)
															 (draw-pacman g gr pacman (* (@g :actorsize) 2) (@g :actorsize) thick)
															
															 (set-actor-position blinky (+ (* (/ (@g :w) 5) (- es 5)) (* (@g :actorsize) 2.2))
															                            (+ (/ (@g :h) 2) (@g :actorsize)))
															 (set-actor-direction blinky :right)
															 (draw-ghost g gr blinky (@g :actorsize) (/ (@g :actorsize) 2) thick)
															
															 (draw-levels g gr))
				         :default (do (set-game-paused g :none)
						                  (reset-actors g pacman blinky pinky inky clyde))))
		       (do
         
         ;; draw maze
         (let [size (@g :tilesize)
							 dotsize (round (/ size 4))
		           ds (/ dotsize 2)
							 doublesize (* size 2)
							 half (round (/ size 2))
							 e125 (every g 125)
							 e250 (every g 250)
	             thelevel (nth levels (dec (@g :level)))
               mazecolor (if (and e250 (= (@g :paused) :level) (> (elapsed g :pauseclock) 1)) Color/WHITE (:mazecolor thelevel))
               solidcolor (:solidcolor thelevel)
               solid (or (not= solidcolor Color/BLACK) (= (:style thelevel) :solid))]
           (doseq [[rowindex row] (map vector (iterate inc 0) (@g :maze))]
             (doseq [[colindex ch] (map vector (iterate inc 0) row)]
               (let [c (str ch)
                    ox (coltox g colindex) ;center of tile
                    oy (rowtoy g rowindex) ;center of tile
                    x (- ox half) ;actual drawing location
                    y (+ oy half)] ;actual drawing location
                ;; set color
                (cond (< rowindex 3) (.setColor gr Color/WHITE)
                      (= c "·") (.setColor gr (:dotcolor (nth levels (dec (@g :level)))))
                      (= c "*") (.setColor gr (:dotcolor (nth levels (dec (@g :level)))))
                      (= c "●") (.setColor gr (if e250 Color/BLACK (:dotcolor (nth levels (dec (@g :level)))))) ; make energizer dots blink
                      (= c "-") (.setColor gr brightpink)
                      :default (.setColor gr mazecolor))
                (cond (some #(= c %) [" " "X" "x" "#" "^" "<"]) nil ; empty maze path
                      ;; draw maze part
                      :default (let [overleft (- ox size)
																		upone (- oy size)
																		halfright (+ ox half)
																		halfup (- oy half)]
                                (.setStroke gr thick)
                                (cond (or (= c "·") (= c "*")) (when (some {[rowindex colindex] []} (@g :dots)) ; only show uneaten dots
	                                                                (.fillRect gr (- ox ds), (- oy ds) dotsize, dotsize))
														                                      ;(.fillOval gr (- ox ds), (- oy ds) dotsize, dotsize))
                                    
                                      (= c "┌") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr ox, oy size, size 90, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr ox, oy size, size 90, 90)) ;(.drawLine gr ox,y halfright,oy) ; 
                                      (= c "❸") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr ox, oy size, size 90, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr ox, oy size, size 90, 90)) ;(.drawLine gr ox,y halfright,oy) ; 
                                      (= c "┘") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr overleft, upone size, size 270, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, upone size, size 270, 90)) ;(.drawLine gr x,oy ox,halfup) ;
                                      (= c "❷") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr overleft, upone size, size 270, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, upone size, size 270, 90)) ;(.drawLine gr x,oy ox,halfup) ;
                                      (= c "┐") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr overleft, oy size, size 0, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, oy size, size 0, 90)) ;(.drawLine gr ox,y x,oy) ;
                                      (= c "❹") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr overleft, oy size, size 0, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, oy size, size 0, 90)) ;(.drawLine gr ox,y x,oy) ;
                                      (= c "└") (do (when solid (.setColor gr solidcolor)
                                                                (.fillArc gr ox, upone size, size 180, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr ox, upone size, size 180, 90)) ;(.drawLine gr halfright,oy ox,halfup) ;
                                      (= c "❶") (do (when solid (.setColor gr solidcolor)
			                                                          (.fillRect gr x, halfup size, size)
                                                                (.setColor gr Color/BLACK)
                                                                (.fillArc gr ox, upone size, size 180, 90)
                                                                (.setColor gr mazecolor))
                                                    (.drawArc gr ox, upone size, size 180, 90)) ;(.drawLine gr halfright,oy ox,halfup) ;
                                      (= c "╕") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr overleft, oy size, size 0, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, oy size, size 0, 90)
                                                    (.drawLine gr x, halfup halfright, halfup))
                                      (= c "╒") (do (when solid (.setColor gr solidcolor)
								                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr ox, oy size, size 90, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr ox, oy size, size 90, 90)
                                                    (.drawLine gr x, halfup halfright, halfup))
                                      (= c "╛") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr overleft, upone size, size 270, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, upone size, size 270, 90)
                                                    (.drawLine gr x, y halfright, y))
                                      (= c "╘") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr ox, upone size, size 180, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr ox, upone size, size 180, 90)
                                                    (.drawLine gr x, y halfright, y))
                                      (= c "╖") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr overleft, oy size, size 0, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, oy size, size 0, 90)
	                                                  (.drawLine gr halfright, y halfright, halfup))
                                      (= c "╜") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr overleft, upone size, size 270, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr overleft, upone size, size 270, 90)
	                                                  (.drawLine gr halfright, y halfright, halfup))
                                      (= c "╙") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr ox, upone size, size 180, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr ox, upone size, size 180, 90)
	                                                  (.drawLine gr x, y x, halfup))
                                      (= c "╓") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, size)
                                                                    (.setColor gr Color/BLACK)
								                                                    (.fillArc gr ox, oy size, size 90, 90)
                                                                    (.setColor gr mazecolor))
                                                    (.drawArc gr ox, oy size, size 90, 90)
	                                                  (.drawLine gr x, y x, halfup))
                                      (= c "─") (.drawLine gr x, oy halfright, oy)
                                      (= c "-") (.fillRect gr x, (+ oy (round (/ half 4))) size, (round (/ half 2))) ;pen
                                      (= c "│") (.drawLine gr ox, halfup ox, y)
                                      (= c "▄") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, half)
                                                                    (.setColor gr mazecolor))
                                                    (.drawLine gr x, halfup halfright, halfup)
	                                                  (.drawLine gr x, oy halfright, oy))
                                      (= c "▼") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup size, half)
                                                                    (.setColor gr mazecolor))
	                                                  (.drawLine gr x, oy halfright, oy))
                                      (= c "▀") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, oy size, half)
                                                                    (.setColor gr mazecolor))
                                                    (.drawLine gr x, y halfright, y)
	                                                  (.drawLine gr x, oy halfright, oy))
                                      (= c "▲") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, oy size, half)
                                                                    (.setColor gr mazecolor))
	                                                  (.drawLine gr x, oy halfright, oy))
                                      (= c "▐") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup half, size)
                                                                    (.setColor gr mazecolor))
                                                    (.drawLine gr x, halfup x, y)
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "▓") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr x, halfup half, size)
                                                                    (.setColor gr mazecolor))
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "▌") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr ox, halfup half, size)
                                                                    (.setColor gr mazecolor))
                                                    (.drawLine gr halfright, halfup halfright, y)
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "░") (do (when solid (.setColor gr solidcolor)
                                                                    (.fillRect gr ox, halfup half, size)
                                                                    (.setColor gr mazecolor))
	                                                  (.drawLine gr ox, halfup ox, y))
                                      (= c "╔") (do (when solid (.setColor gr solidcolor)
					                                                      (.fillArc gr x, halfup doublesize, doublesize 90, 90)
		                                                            (.setColor gr Color/BLACK)
				                                                        (.fillArc gr ox, oy size, size 90, 90)
				                                                        (.setColor gr mazecolor))
                                                      (.drawArc gr ox, oy size, size 90, 90) ;(.drawLine gr ox, y halfright, oy) ;
	                                                  (.drawArc gr x, halfup doublesize, doublesize 90, 90)) ;(.drawLine gr halfright, halfup x, y)) ;
                                      (= c "╗") (do (when solid (.setColor gr solidcolor)
																																(.fillArc gr (- overleft half), halfup doublesize, doublesize 0, 90)                                                               
		                                                            (.setColor gr Color/BLACK)
					                                                      (.fillArc gr overleft, oy size, size 0, 90)
		                                                            (.setColor gr mazecolor))
                                                      (.drawArc gr overleft, oy size, size 0, 90) ;(.drawLine gr ox, y x, oy) ;
	                                                  (.drawArc gr (- overleft half), halfup doublesize, doublesize 0, 90)) ;(.drawLine gr (- ox half), halfup halfright, y)) ;
                                      (= c "╚") (do (when solid (.setColor gr solidcolor)
																																(.fillArc gr x, (- oy size half) doublesize, doublesize 180, 90)                                                               
		                                                            (.setColor gr Color/BLACK)
					                                                      (.fillArc gr ox, upone size, size 180, 90)
		                                                            (.setColor gr mazecolor))
                                                  (.drawArc gr ox, upone size, size 180, 90) ;(.drawLine gr halfright, oy (+ x half), halfup) ;
	                                                  (.drawArc gr x, (- oy size half) doublesize, doublesize 180, 90)) ;(.drawLine gr x, (- oy half), halfright, y)) ;
                                      (= c "╝") (do (when solid (.setColor gr solidcolor)
																																(.fillArc gr (- ox size half), (- oy size half) doublesize, doublesize 270, 90)                                                             
		                                                            (.setColor gr Color/BLACK)
					                                                      (.fillArc gr overleft, upone size, size 270, 90)
		                                                            (.setColor gr mazecolor))
                                                  (.drawArc gr overleft, upone size, size 270, 90) ;(.drawLine gr ox, halfup x, (- y half)) ;
	                                                  (.drawArc gr (- ox size half), (- oy size half) doublesize, doublesize 270, 90)) ;(.drawLine gr (- ox half), y halfright, halfup)) ;
                                      (= c "●") (if (and (@g :started) ; don't show energizers until game starts
                                                         (some {[rowindex colindex] []} (@g :dots))) ; only show uneaten energizers
                                                   (.fillOval gr x, halfup size, size))
                                      (= c "█") (do (.setColor gr solidcolor)
                                                    (.fillRect gr x, halfup size, size))
                                      :default (.drawString gr c x y))))
               
               ;; draw screen quadrants
               ;;(.setColor gr Color/GREEN)
               ;;(.drawRect gr (/ (@g :w) 2), 0 1, (@g :h))
               ;;(.drawRect gr 0, (/ (@g :h) 2) (@g :w), 1)
               
               ;; debug grid
               ;(.setStroke gr (new BasicStroke 1))
               ;(.setColor gr darkgray)
							 ;(.drawRect gr (- ox half), (- oy half) (@g :tilesize), (@g :tilesize))

	             ;;(.setColor gr Color/GREEN)
							 ;;(.drawRect gr ox, oy 1, 1)
          
               ;; End of maze loop
           ))))
         
         (let [size (@g :actorsize)
               half (round (/ size 2))
               l (@g :level)]
           ;; draw scoreboard
           (.setColor gr (if (or (not (@g :started)) (every g 500)) Color/WHITE Color/BLACK))
           (.drawString gr "1UP" (- (coltox g 3) half) (+ (rowtoy g 0) half))
           (.setColor gr Color/WHITE)
           (.drawString gr "2UP" (- (coltox g 23) half) (+ (rowtoy g 0) half))
           ;; player 1 score
           (.drawString gr (format "%6d" (@g :score)) (- (coltox g 1) half) (rowtoy g 2))
           ;; high score
           (.drawString gr "HIGH SCORE" (- (coltox g 9) half) (+ (rowtoy g 0) half))
           (.drawString gr (format "%6d" (@g :highscore)) (- (coltox g 11) half) (rowtoy g 2))
          
           ;; level
           (draw-levels g gr)

           ;; marquis art 
           (.drawImage gr monster-art 10,10 200,200 nil)
           (.drawImage gr pac-art (- (@g :w) 210),10 200,200 nil)

					 ;; Draw players on board
           (if (@g :started)
             (let [x (- (@pacman :x) half)
                   y (- (@pacman :y) half)
                   d (@pacman :d)
                   board-seconds (elapsed g :boardclock)
                   game-seconds (elapsed g :clock)
									 e125 (every g 125)
                   dl (dots-left g)
                   level (nth levels (dec l))
                   parked (@pacman :parked)
                   mouth-angle (cond (and parked (= d :right)) 15
                                     (and parked (= d :up)) 105
                                     (= d :right) (if (and (not parked) e125) 15  45)  ; 15
                                     (= d :up)    (if (and (not parked) e125) 105 135)  ;105
                                     (= d :left)  (if (and (not parked) e125) 195 225)  ;195
                                     (= d :down)  (if (and (not parked) e125) 285 315)) ;285)
                   ;; make mouth chomp every 1/16 second if Pac-Man is moving
                   mouth-wide (cond (or (< board-seconds 3) ; wait a few seconds at the start of each life
                                        (= (@g :paused) :death)
                                        (= (@g :paused) :level)
                                        (and (not parked) ;(my-openpath? g pacman :forward)
                                             (every g 62))) 360
                                    (or (and (not parked) e125)
                                        (and parked (or (= d :right) (= d :up)))) 330
                                    :default 270)]

								 ;; draw "Ready!"
                 (when (< game-seconds 2)
                   (.setColor gr Color/CYAN)
                   (.drawString gr "PLAYER ONE" (- (coltox g 9) half) (- (rowtoy g 15) half)))
                 (when (< board-seconds 3)
                   (.setColor gr Color/YELLOW)
                   (.drawString gr "READY" (- (coltox g 11) half) (- (rowtoy g 21) half))
									 (.setFont gr (Font. "Consolas" (bit-or Font/BOLD Font/ITALIC) (+ (@g :actorsize) 9)))
									 (.drawString gr "!" (- (coltox g 16) half) (- (rowtoy g 21) half)))
									 (.setFont gr (Font. "Consolas" Font/BOLD (+ (@g :actorsize) 9)))
                 ;; remaining lives
                 (.setColor gr Color/YELLOW)
                 (dotimes [n (dec (@g :lives))]
                  (let [small (* size 0.75)]
                   (.fillArc gr (+ (coltox g (+ n 2)) (* n half)), (rowtoy g (- mazerows 2)) small, small 195 330)))
                 
                 ;; draw Fruit
                 (when (and (or (= (@g :fruit) :none) (= (@g :fruit) :eaten)) (or (= dl 74) (= dl 174))) ; initiate fruit prize twice per level based on dots eaten
                   (set-game-fruitclock g (now))
                   (set-game-fruit g :uneaten))
                 (when (and (= (@g :fruit) :uneaten) (< (elapsed g :fruitclock) 11) (<= dl 174)) ; show uneaten fruit for 10 seconds
                   (.drawImage gr (:fruit level) (- (/ (@g :w) 2) half), (- (rowtoy g 20) half) size, size nil))
                 
                 ;; draw Bonus points for fruit eaten
                 (when (and (<= dl 174) (< (elapsed g :bonusclock) 2)) ; show points for 2 seconds
                   (.setColor gr brightpink)
									 (.setFont gr (Font. "Consolas" Font/BOLD (round (/ size 2))))
									 (.drawString gr (str (:fruitpoints level)) (coltox g 13), (- (rowtoy g 21) half)))
                 
		             ;; draw frightened ghosts
                 (when (and (or (not= (@g :paused) :death) (<= (elapsed g :pauseclock) 1))
                            (or (not= (@g :paused) :level) (<= (elapsed g :pauseclock) 1)))
								 (when (and (= (@clyde :mode) :frightened) (not= (@g :paused) :eat-clyde))  (draw-ghost g gr clyde size half thick))
								 (when (and (= (@inky :mode) :frightened) (not= (@g :paused) :eat-inky))   (draw-ghost g gr inky size half thick))
								 (when (and (= (@pinky :mode) :frightened) (not= (@g :paused) :eat-pinky))  (draw-ghost g gr pinky size half thick))
								 (when (and (= (@blinky :mode) :frightened) (not= (@g :paused) :eat-blinky)) (draw-ghost g gr blinky size half thick)))

                 ;; draw Pac-Man
                 (let [cg (chomping-ghosts? g)]
                   (when (not cg)
                       (draw-pacman g gr pacman size half thick))

		             ;; draw normal ghosts
                 (when (and (>= board-seconds 2)
                            (or (not= (@g :paused) :death) (<= (elapsed g :pauseclock) 1))
                            (or (not= (@g :paused) :level) (<= (elapsed g :pauseclock) 1)))
								 (when (and (not= (@clyde :mode) :frightened) (not= (@g :paused) :eat-clyde))  (draw-ghost g gr clyde size half thick))
								 (when (and (not= (@inky :mode) :frightened) (not= (@g :paused) :eat-inky))   (draw-ghost g gr inky size half thick))
								 (when (and (not= (@pinky :mode) :frightened) (not= (@g :paused) :eat-pinky))  (draw-ghost g gr pinky size half thick))
								 (when (and (not= (@blinky :mode) :frightened) (not= (@g :paused) :eat-blinky)) (draw-ghost g gr blinky size half thick)))

                 (when cg (.setColor gr Color/CYAN)
		                       (.setFont gr (Font. "Consolas" Font/BOLD (round (/ size 2))))
		                       (.drawString gr (str (@g :ghostpoints)) (round x), (+ half (round y))))) ; Show points for ghost eaten
                   
                 ;; draw paused
                 (when (= (@g :paused) :paused)
                   (.setColor gr Color/GREEN)
                   (.drawString gr "PAUSED" (coltox g 10), (- (rowtoy g 21) half)))

	               ;; debug panel  
                 ;(.setColor gr Color/WHITE)
                 ;(.drawString gr (if (= (@g :paused) :death) "death" "other") (coltox g 40), (rowtoy g 10))
                 ;(.drawString gr (str (round (elapsed g :pauseclock))) (coltox g 40), (rowtoy g 11))
                 ;(.setColor gr Color/YELLOW)
                 ;(.drawString gr (str (@pacman :s)) (coltox g 40), (rowtoy g 10))
                 ;(.setColor gr Color/RED)
                 ;(.drawString gr (str (@blinky :s)) (coltox g 40), (rowtoy g 12))
                 ;; show modes
                 ;(let [ms (elapsed g :modeclock)]
                 ;  (.setColor gr Color/RED)
                 ;  (.drawString gr (mode-string (mode-interval g blinky ms)) (coltox g 40), (rowtoy g 12))
                 ;  (.setColor gr Color/PINK)
                 ;  (.drawString gr (mode-string (mode-interval g pinky ms)) (coltox g 40), (rowtoy g 13))
                 ;  (.setColor gr Color/CYAN)
                 ;  (.drawString gr (mode-string (mode-interval g inky ms)) (coltox g 40), (rowtoy g 14))
                 ;  (.setColor gr Color/ORANGE)
                 ;  (.drawString gr (mode-string (mode-interval g clyde ms)) (coltox g 40), (rowtoy g 15)))
             )
             ;;else draw Game Over mode
             (doto gr
               (.setColor Color/RED)
               (.drawString "GAME  OVER" (- (coltox g 9) half) (- (rowtoy g 21) half))
               (.setColor Color/WHITE)
               (.drawString "CREDIT" (- (coltox g 2) half) (rowtoy g (dec mazerows)))
               (.drawString (format "%2d" (@g :credits)) (- (coltox g 9) half) (rowtoy g (dec mazerows))))))))

         (. gr dispose) 
         (. bs show))))
   
    (mouseMoved [#^MouseEvent e]
      (let [#^JFrame me this] (.repaint me)))
    
    (mouseDragged [e])

    (keyPressed [#^KeyEvent e]
      (when (or (= (. e getKeyChar) \c)
                (= (. e getKeyChar) \5))
				(set-credits g (inc (@g :credits)))
				(let [#^JFrame me this] 
		       (.repaint me)))
      (when (and (not (@g :started)) (> (@g :credits) 0) (= (. e getKeyChar) \1))
        (set-credits g (dec (@g :credits)))
        (reset-game g pacman blinky pinky inky clyde)
        (start-game g))
      (when (= (. e getKeyCode) KeyEvent/VK_UP)
        (set-game-joystick g :up)
        (set-pacman-direction? g pacman :up))
      (when (= (. e getKeyCode) KeyEvent/VK_DOWN)
        (set-game-joystick g :down)
        (set-pacman-direction? g pacman :down))
      (when (= (. e getKeyCode) KeyEvent/VK_LEFT)
        (set-game-joystick g :left)
        (set-pacman-direction? g pacman :left))
      (when (= (. e getKeyCode) KeyEvent/VK_RIGHT)
        (set-game-joystick g :right)
        (set-pacman-direction? g pacman :right))
      (when (or (= (. e getKeyChar) \n)
                (= (. e getKeyCode) KeyEvent/VK_F1))
        (when (and (@g :started) (= (@g :paused) :none)) (reset-board g pacman blinky pinky inky clyde))) ;(set-game-paused g :level)
										        ;(set-game-pauseclock g (now))))
      (when (= (. e getKeyChar) \a)
        (set-game-antialias g (not (@g :antialias))))
      (when (= (. e getKeyChar) \p)
	      (if (= (@g :paused) :none)
	        (set-game-paused g :paused)
	        (when (= (@g :paused) :paused)
	          (set-game-paused g :none))))
      (when (or (= (. e getKeyCode) KeyEvent/VK_ESCAPE)
								(= (. e getKeyChar) \q))
        (System/exit 0)))

    (keyReleased [e])

    (keyTyped [e])

    (actionPerformed [e]
       (let [dl (dots-left g)
             level (nth levels (dec (@g :level)))]
	       (when (= (@g :paused) :none) ; nobody moves during paused mode

		       ;; move in direction of joystick
           (set-pacman-direction? g pacman (@g :joystick))
         
		       ;; check Pac-Man eaten by ghosts (redundant check to prevent collision detection bug!)
           (capture-pacman g pacman blinky pinky inky clyde)
       
		       ;; check ghosts eaten by Pac-Man (redundant check to prevent collision detection bug!)
	         (chomp-ghosts g pacman blinky pinky inky clyde)

           ;; update Pac-Man position
			     (when (= (@g :paused) :none)
	           (update-pacman-position? g pacman))
			
		       ;; eat dots
		       (let [x (@pacman :x)
                 r (ytorow g (@pacman :y))
		             c (xtocol g x)
                 center (/ (@g :w) 2)
                 half (/ (@g :tilesize) 2)]
			       (when (and (not= (@g :paused) :death) (some {[r c] []} (@g :dots)))
                (let [t (mazetile (@g :maze) r c)]
								(set-game-score g (+ (if (not= t "●")
					                              10 ; 10 points per dot
					                              50) ; 50 points per energizer
		                            (@g :score)))
								(eat-dot! g r c)
								(remember-last-dot! pacman r c)
								(when (= t "●") ; turn ghosts blue and reverse when energizer is eaten
				          (set-game-ghostpoints g 100)
                  (set-game-blueclock g (now))
                  (doseq [a [blinky pinky inky clyde]]
                    (frighten-ghost! g a)))
        
				       ;; check level complete (redundant to prevent end of level bug)
				       (when (and (not= (@g :paused) :level)
				                  (= (dots-left g) 0)) 
				         (set-game-paused g :level)
				         (set-game-pauseclock g (now)))
				       (when (and (= (@g :paused) :level) (>= (elapsed g :pauseclock) 3)) ; Pause for 3 seconds
				         (reset-board g pacman blinky pinky inky clyde))))

						 ;; eat fruit
		         (when (and (< (elapsed g :fruitclock) 11) (<= dl 174) (= (@g :fruit) :uneaten) (= r 20) (>= x (- center half)) (<= x (+ center half))) ; TODO: fix hard-coded fruit location
               (set-game-score g (+ (:fruitpoints level) (@g :score)))
		           (set-game-fruit g :eaten)
               (set-game-bonusclock g (now))))
		       
		       ;; check ghosts eaten by Pac-Man
	         (chomp-ghosts g pacman blinky pinky inky clyde)

           (when (not (chomping-ghosts? g))  
	           ;; update ghost mode
             (doseq [a [blinky pinky inky clyde]]
               (set-ghost-mode! g a (mode-interval g a (elapsed g :modeclock))))

		         ;; update ghost positions
						(when (not= (@g :paused) :level)
							(doseq [a [blinky pinky inky clyde]]
							(update-ghost-position! g a pacman blinky))))))
	     
       ;; release pause after first few seconds of life
       (when (and (= (@g :paused) :start) (> (elapsed g :pauseclock) 3))
         (set-game-paused g :none))
       
       ;; check for bonus life
       (when (and (= (@g :bonuslife) :unearned) (> (@g :score) 10000))
         (set-game-bonuslife g :earned)
         (set-game-lives g (inc (@g :lives))))
       
       ;; check Pac-Man eaten by ghosts
       (capture-pacman g pacman blinky pinky inky clyde)

       ;; reincarnate eaten ghosts
       (when (and (= (@g :paused) :eat-blinky) (>= (elapsed g :pauseclock) 1)) (set-game-paused g :none) (set-ghost-mode! g blinky :reincarnate))
       (when (and (= (@g :paused) :eat-pinky)  (>= (elapsed g :pauseclock) 1)) (set-game-paused g :none) (set-ghost-mode! g pinky :reincarnate))
       (when (and (= (@g :paused) :eat-inky)   (>= (elapsed g :pauseclock) 1)) (set-game-paused g :none) (set-ghost-mode! g inky :reincarnate))
       (when (and (= (@g :paused) :eat-clyde)  (>= (elapsed g :pauseclock) 1)) (set-game-paused g :none) (set-ghost-mode! g clyde :reincarnate))

       ;; check ghost reincarnation
       (doseq [a [blinky pinky inky clyde]]
         (when (reincarnated? g a) (reset-ghost! g a)))
       
       ;; check game over
       (when (= (@g :lives) 0) (stop-game g))

       ;; check level complete
       (when (and (not= (@g :paused) :level)
                  (= (dots-left g) 0)) 
         (set-game-paused g :level)
         (set-game-pauseclock g (now)))
       (when (and (= (@g :paused) :level) (>= (elapsed g :pauseclock) 3)) ; Pause for 3 seconds
         (reset-board g pacman blinky pinky inky clyde))
      
	     (let [#^JFrame me this] (.repaint me)))))

(defn -main []
  (let [tk (. Toolkit getDefaultToolkit)
        ge (GraphicsEnvironment/getLocalGraphicsEnvironment)
        gd (. ge getDefaultScreenDevice)
        height (.. tk getScreenSize height)
        width (.. tk getScreenSize width)
        tilesize (round (/ height mazerows))
        actorsize (round (* tilesize 1.6)) ;1.5
        credits 0
        highscore 0
        thegame (new-game (maze 1) (dots 1) :left height width tilesize actorsize credits highscore (now))
        thepacman (new-pacman)
        blinky (new-ghost :shadow Color/RED)
        pinky (new-ghost :speedy brightpink)
        inky (new-ghost :bashful Color/CYAN)
        clyde (new-ghost :pokey clydeorange)
        #^JFrame screen (gameworld-frame thegame thepacman blinky pinky inky clyde)]

    ;; Initialize graphics mode
    (set-game-timer thegame (Timer. 20 screen))
    (when (not (. screen isDisplayable)) (. screen setUndecorated true))
    (.setVisible screen true)
    (. (.getContentPane screen) setBackground Color/BLACK)
    (. (.getContentPane screen) setIgnoreRepaint true)
    (doto screen
      (.setCursor Cursor/HAND_CURSOR)
      (.setResizable false) 
      (.setBackground Color/BLACK) (.setIgnoreRepaint true)
      (.addMouseMotionListener screen) (.addKeyListener screen))
    (. gd setFullScreenWindow screen)
    (. screen createBufferStrategy 2)
    
    ;; Power up Pac-Man machine!
    (reset-game thegame thepacman blinky pinky inky clyde)))


(-main)