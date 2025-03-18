(ns gamestate)

(def constants
  {:MAXCAMERAMOVE 100
   :MAXTEXTWIDTH 400})

(def game-state 
  {:dt 0 ;time
   :lastUpdate 0
   :mousedown false
   :mouseX 0 ;input
   :mouseY 0
   :cameraX 0
   :cameraY 0 
   :textwidth 400
   :activeword "foo"
   :texts []
   :strokes []
   :targetnode 0})
