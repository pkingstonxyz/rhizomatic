(ns gamestate)

(def constants
  {:MAXCAMERAMOVE 100
   :MAXTEXTWIDTH 400})

(def game-state 
  {;time
   :dt 0
   :lastUpdate 0
   ;input
   :mousedown false
   :mouseX 0 
   :mouseY 0
   ;camera positioning
   :cameraX 0 
   :cameraY 0 
   :targetnode 0
   ;text rendering
   :textwidth 400 
   ;text state
   :activeword nil 
   :texts []
   :strokes []
   ;markov
   :transitiontable (js/Map.)
   ;mapping of word->line#s
   :wordindices (js/Map.)})
