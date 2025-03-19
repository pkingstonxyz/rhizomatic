(ns texts
  (:require [gamestate :as gs]))

(defn word-width [word-measurement]
  (+ (.-actualBoundingBoxLeft word-measurement)
     (.-actualBoundingBoxRight word-measurement)))

(defn set-text-info! []
  (let [ctx (.-context gs/game-state)]
    (set! ctx.textAlign "start")
    (set! ctx.textBaseline "top")
    (set! ctx.font "18px serif")
    (set! ctx.fillStyle "black")))
    

(defn create-text-measurement [text]
  (let [data {:drawdata {:width 0
                         :height 0
                         :words []}
              :linenum 0     
              :workinglen 0}
        lineheight 20
        ctx (.-context gs/game-state)]
    (set-text-info!)
    (doseq [word (.split text " ")]
      (let [measurement (.measureText ctx word)
            width (+ 2 (word-width measurement))
            potential-next-width (+ width (.-workinglen data))]
        (if (> potential-next-width (.-textwidth gs/game-state)) ;if it's going to be too big
          (do ;wrap
            (set! (.-linenum data) (inc (.-linenum data)))
            (.push (.-words (.-drawdata data))
                   {:word word
                    :xpos 0
                    :ypos (* lineheight (.-linenum data))
                    :width width
                    :height 20})
            (set! (.-height (.-drawdata data)) (* lineheight (inc (.-linenum data))))
            (set! (.-width (.-drawdata data))
                  (if (> (.-width (.-drawdata data)) (.-workinglen data))
                      (.-width (.-drawdata data))
                      (.-workinglen data)))
            (set! (.-workinglen data) width))
          (do ;else don't wrap
            (.push (.-words (.-drawdata data)) 
                   {:word word
                    :xpos (.-workinglen data)
                    :ypos (* lineheight (.-linenum data))
                    :width width
                    :height 20}) 
            (set! (.-workinglen data) potential-next-width)))))
    ;adjust based on bounding box height and stuff
    (doseq [word (.-words (.-drawdata data))]
      (let [adjustx (/ (.-width (.-drawdata data)) 2)
            adjusty (/ (.-height (.-drawdata data)) 2)]
        (set! (.-xpos word) (- (.-xpos word) adjustx))
        (set! (.-ypos word) (- (.-ypos word) adjusty))))
      
    (.-drawdata data)))

(defn create-text-node [value xpos ypos]
  (let [node {:text value
              :xpos xpos
              :ypos ypos
              :drawdata (create-text-measurement value)}]
    (println node)
    node)) 

(defn remeasure-text-nodes! []
  (doseq [node (.-texts gs/game-state)]
    (let [text (.-text node)
          remeasured (create-text-measurement text)]
      (set! (.-drawdata node) remeasured)))) 
